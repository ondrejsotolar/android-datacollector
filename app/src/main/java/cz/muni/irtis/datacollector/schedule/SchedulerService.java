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

public class SchedulerService extends Service
{
    private int testDelay = 10*1000;
    private static final int CHANNEL_ID = 1337;

    private TaskScheduler taskScheduler;
    private NotificationBuilder notificationBuilder;


    public static void startMeUp(Context context) {
        Intent i = new Intent(context, SchedulerService.class);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            context.startForegroundService(i);
        }
        else {
            context.startService(i);
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
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("SchedulerService", "onBind()");
        throw new IllegalStateException("Non-bindable service");
    }

    private void initMetrics() {
        taskScheduler.addMetric(new BatteryState(getApplicationContext(), testDelay/1000));
    }
}
