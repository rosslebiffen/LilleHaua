package com.example.jonet.lillehaua.Helper;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import com.example.jonet.lillehaua.R;

/**
 * Created by Jone on 15.05.2018.
 */

public class NotificationHelper extends ContextWrapper{

    private static final String JONET_CHANNEL_ID = "example.jonet.lillehaua.EXAMPLEJonet";
    private static final String JONET_CHANNEL_NAME = "Lille Haua";

    private NotificationManager manager;

    public NotificationHelper(Context base) {
        super(base);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) //Only works if api is 26 or higher
            createChannel();
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel jonetChannel = new NotificationChannel(JONET_CHANNEL_ID,
                JONET_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);
        jonetChannel.enableLights(false);
        jonetChannel.enableVibration(true);
        jonetChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(jonetChannel);

    }

    public NotificationManager getManager() {
        if(manager == null)
            manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        return manager;

    }

    @TargetApi(Build.VERSION_CODES.O)
    public android.app.Notification.Builder getLilleHauaChannelNotification(String title, String body, PendingIntent contentIntent,
                                                                            Uri soundUri)
    {
        return new android.app.Notification.Builder(getApplicationContext(),JONET_CHANNEL_ID)
                .setContentIntent(contentIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(soundUri)
                .setAutoCancel(false);
    }
    @TargetApi(Build.VERSION_CODES.O)
    public android.app.Notification.Builder getLilleHauaChannelNotification(String title, String body,
                                                                            Uri soundUri)
    {
        return new android.app.Notification.Builder(getApplicationContext(),JONET_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(soundUri)
                .setAutoCancel(false);
    }
}
