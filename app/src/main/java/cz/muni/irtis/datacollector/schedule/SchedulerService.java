package cz.muni.irtis.datacollector.schedule;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

import androidx.annotation.Nullable;
import cz.muni.irtis.datacollector.metrics.BatteryState;
import cz.muni.irtis.datacollector.metrics.CallHistory;
import cz.muni.irtis.datacollector.metrics.ForegroundApplication;
import cz.muni.irtis.datacollector.metrics.InstalledApplication;
import cz.muni.irtis.datacollector.metrics.Location;
import cz.muni.irtis.datacollector.metrics.PhysicalActivity;
import cz.muni.irtis.datacollector.metrics.SmsConversation;
import cz.muni.irtis.datacollector.metrics.report.Runtime;
import cz.muni.irtis.datacollector.metrics.Screenshot;
import cz.muni.irtis.datacollector.metrics.Wifi;

public class SchedulerService extends Service {
    private static final String TAG = SchedulerService.class.getSimpleName();

    public static final String EXTRA_RESULT_CODE = "resultCode";
    public static final String EXTRA_RESULT_INTENT = "resultIntent";
    public static boolean IS_RUNNING = false;

    private int testDelay = 10 * 1000;
    private static final int CHANNEL_ID = 1337;

    private TaskScheduler taskScheduler;
    private NotificationBuilder notificationBuilder;
    private static Intent screenshotData;

    /**
     * Start the service. If version is high enough, starts in foreground.
     * @param context App context
     */
    public static void startRunning(Context context, Intent screenshotIntent) {
        if (!IS_RUNNING) {
            Intent i = new Intent(context, SchedulerService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                screenshotData = screenshotIntent;
                context.startForegroundService(i);
            } else {
                throw new IllegalStateException("Trying to start SchedulerService in the background!");
            }
            IS_RUNNING = true;
        }
        else {
            Log.d(TAG, "Already running.");
        }
    }

    public static void stopRunning(Context context, Intent serviceName) {
        if (IS_RUNNING) {
            context.stopService(serviceName);
        }
        else {
            Log.d(TAG, "Trying to stop not running service.");
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
        IS_RUNNING = false;
        stopForeground(false);
        Log.w(TAG, "service exiting.");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        throw new IllegalStateException("Non-bindable service");
    }

    public List<String> getCollectedMetricNames() {
        return taskScheduler.get10sMetricNames();
    }

    private void initMetrics() {
        taskScheduler.addMetric(new BatteryState(getApplicationContext()), 10);

        int resultCode = screenshotData.getIntExtra(EXTRA_RESULT_CODE, 1337);
        Intent resultData = screenshotData.getParcelableExtra(EXTRA_RESULT_INTENT);
        taskScheduler.addMetric(new Screenshot(getApplicationContext(), resultCode, resultData), 10);

        taskScheduler.addMetric(new Location(getApplicationContext(), testDelay, 0), 10);

        taskScheduler.addMetric(new PhysicalActivity(getApplicationContext(), testDelay), 10);

        taskScheduler.addMetric(new Wifi(getApplicationContext()), 10);

        taskScheduler.addMetric(new CallHistory(getApplicationContext()), 10);

        taskScheduler.addMetric(new SmsConversation(getApplicationContext()), 10);

        taskScheduler.addMetric(new InstalledApplication(getApplicationContext()), 10);

        taskScheduler.addMetric(new ForegroundApplication(getApplicationContext()), 10);

        taskScheduler.addMetric(new Runtime(getApplicationContext()), 1);
    }
}
