package com.themabajogroup.sangawa.Utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;

import com.themabajogroup.sangawa.R;

public class NotificationSender {
    private String channelId;
    private Context context;
    private int idCounter = 1;
    private static NotificationSender instance;


    private NotificationSender(String channelId, Context context) {
        this.channelId = channelId;
        this.context = context;
        createNotificationChannel();
    }

    public static NotificationSender getInstance(String channelId, Context context) {
        if (instance == null) {
            instance = new NotificationSender(channelId, context);
        }

        return instance;
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                channelId,
                "SanGawa App Notifications",
                NotificationManager.IMPORTANCE_HIGH
        );

        channel.setDescription("Notifications for geofence transitions and task collaborations");

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    public void sendNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(idCounter, builder.build());
            idCounter++;
        }
    }
}
