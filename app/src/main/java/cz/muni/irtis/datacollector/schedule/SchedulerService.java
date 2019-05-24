package cz.muni.irtis.datacollector.schedule;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import cz.muni.irtis.datacollector.metrics.BatteryState;
import cz.muni.irtis.datacollector.metrics.CallHistory;
import cz.muni.irtis.datacollector.metrics.ForegroundApplication;
import cz.muni.irtis.datacollector.metrics.Location;
import cz.muni.irtis.datacollector.metrics.PhysicalActivity;
import cz.muni.irtis.datacollector.metrics.SmsConversation;
import cz.muni.irtis.datacollector.metrics.report.Runtime;
import cz.muni.irtis.datacollector.metrics.Screenshot;
import cz.muni.irtis.datacollector.metrics.Wifi;

/**
 * Initialize & schedule metric execution
 */
public class SchedulerService extends Service {
    private static final String TAG = SchedulerService.class.getSimpleName();

    public static final String EXTRA_RESULT_CODE = "resultCode";
    public static final String EXTRA_RESULT_INTENT = "resultIntent";
    public static boolean IS_RUNNING = false;

    private final int testDelay = 1 * TaskScheduler.ONE_SECOND;
    private final int updateUiDelay = 1 * TaskScheduler.ONE_SECOND;
    private static final int CHANNEL_ID = 1337;

    private TaskScheduler taskScheduler;
    private NotificationBuilder notificationBuilder;
    private static Intent screenshotData;

    /**
     * Start the service. If version is high enough, starts in foreground.
     * @param context App context
     * @param screenshotIntent permission for MediaProjection
     */
    public static void startRunning(Context context, Intent screenshotIntent) {
        if (!IS_RUNNING) {
            Intent i = new Intent(context, SchedulerService.class);
            screenshotData = screenshotIntent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(i);
            } else {
                context.startService(i);
            }
            IS_RUNNING = true;
        }
        else {
            Log.d(TAG, "Already running.");
        }
    }

    /**
     * Stop the service. In subsequest onDestroy() all scheduled metrics are stopped also.
     * @param context
     * @param serviceName
     */
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
        taskScheduler = new TaskScheduler();
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

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        Intent intent = new Intent(this, SchedulerService.class);
        PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
        AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 60*1000, pintent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        throw new IllegalStateException("Non-bindable service");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        taskScheduler.onConfigurationChanged();
    }

    private void initMetrics() {
        taskScheduler.addMetric(new BatteryState(getApplicationContext()), testDelay);

        if (screenshotData != null) {
            int resultCode = screenshotData.getIntExtra(EXTRA_RESULT_CODE, 1337);
            Intent resultData = screenshotData.getParcelableExtra(EXTRA_RESULT_INTENT);
            taskScheduler.addMetric(new Screenshot(getApplicationContext(), resultCode, resultData,
                    testDelay), TaskScheduler.ONCE);
        }
        else {
            Log.e(TAG, "screenshot permission data is null");
        }

        taskScheduler.addMetric(new Location(getApplicationContext(), testDelay, 0),testDelay);

        taskScheduler.addMetric(new PhysicalActivity(getApplicationContext(), testDelay), TaskScheduler.ONCE);

        taskScheduler.addMetric(new Wifi(getApplicationContext()), testDelay);

        taskScheduler.addMetric(new CallHistory(getApplicationContext()), testDelay);

        taskScheduler.addMetric(new SmsConversation(getApplicationContext()), testDelay);

        taskScheduler.addMetric(new ForegroundApplication(getApplicationContext()), testDelay);

        taskScheduler.addMetric(new Runtime(getApplicationContext()), updateUiDelay);
    }
}
