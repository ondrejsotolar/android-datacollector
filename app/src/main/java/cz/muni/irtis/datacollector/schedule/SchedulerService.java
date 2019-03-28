package cz.muni.irtis.datacollector.schedule;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import cz.muni.irtis.datacollector.metrics.BatteryState;
import cz.muni.irtis.datacollector.metrics.Screenshot;

public class SchedulerService extends Service {
    public static final String EXTRA_RESULT_CODE = "resultCode";
    public static final String EXTRA_RESULT_INTENT = "resultIntent";

    private int testDelay = 10*1000;
    private static final int CHANNEL_ID = 1337;
    private static boolean isServiceRunning = false;
    private TaskScheduler taskScheduler;
    private NotificationBuilder notificationBuilder;
    private static Intent screenshotData;

    /**
     * Start the service. If version is high enough, starts in foreground.
     * @param context App context
     */
    public static void startMeUp(Context context, Intent screenshotIntent) {
        if (!isServiceRunning) {
            Intent i = new Intent(context, SchedulerService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                screenshotData = screenshotIntent;
                context.startForegroundService(i);
            } else {
                throw new IllegalStateException("Trying to start SchedulerService in the background!");
            }
            isServiceRunning = true;
        }
        else {
            Log.d("SchedulerService: ", "Already running.");
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        taskScheduler = new TaskScheduler(testDelay);
        initMetrics();
        notificationBuilder = new NotificationBuilder(
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.initNotificationChannels();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(CHANNEL_ID,
                notificationBuilder.buildForegroundNotification(getApplicationContext()));
        taskScheduler.startCapturingPeriodicaly();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        taskScheduler.onDestroy();
        isServiceRunning = false;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("SchedulerService", "onBind()");
        throw new IllegalStateException("Non-bindable service");
    }

    private void initMetrics() {
        taskScheduler.addMetric(new BatteryState(getApplicationContext()));

        int resultCode = screenshotData.getIntExtra(EXTRA_RESULT_CODE, 1337);
        Intent resultData = screenshotData.getParcelableExtra(EXTRA_RESULT_INTENT);
        taskScheduler.addMetric(new Screenshot(getApplicationContext(), resultCode, resultData));
    }


}