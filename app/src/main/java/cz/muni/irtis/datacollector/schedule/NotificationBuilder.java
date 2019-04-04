package cz.muni.irtis.datacollector.schedule;


import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import cz.muni.irtis.datacollector.MainActivity;

public class NotificationBuilder {
    private static final String CHANNEL_MIN="channel_min";
    private static final String CHANNEL_LOW="channel_low";

    private NotificationManager notificationManager;

    public NotificationBuilder(NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    @TargetApi(Build.VERSION_CODES.O)
    void initNotificationChannels() {
        NotificationChannel channel =
                new NotificationChannel(CHANNEL_MIN, CHANNEL_MIN, NotificationManager.IMPORTANCE_MIN);
        notificationManager.createNotificationChannel(channel);

        channel =
                new NotificationChannel(CHANNEL_LOW, CHANNEL_LOW, NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(channel);
    }

    Notification buildForegroundNotification(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_LOW);
        builder.setOngoing(true)
                .setContentTitle("IRTIS DataCollector service running.")
                .setSmallIcon(android.R.drawable.stat_notify_sync_noanim);

        Intent notifIntent = new Intent(context, MainActivity.class);
        PendingIntent backToActivity = PendingIntent.getActivity(
                context, 0, notifIntent, PendingIntent.FLAG_NO_CREATE);
        builder.setContentIntent(backToActivity);

        return builder.build();
    }
}
