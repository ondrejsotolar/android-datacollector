package cz.muni.irtis.datacollector.schedule;


import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

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
                .setContentTitle("Service working!")
                .setSmallIcon(android.R.drawable.stat_sys_download);

        return(builder.build());
    }
}
