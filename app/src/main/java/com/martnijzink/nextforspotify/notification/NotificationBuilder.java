package com.martnijzink.nextforspotify.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.martnijzink.nextforspotify.ListenActivity;
import com.martnijzink.nextforspotify.R;

public class NotificationBuilder {

    private Context context;
    private String channelId;

    public NotificationBuilder(Context context, String channelId) {
        this.context = context;
        this.channelId = channelId;
    }

    public Notification buildSpeechNotification() {
        Intent myIntent = new Intent(context, ListenActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                myIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_notification_speech_large);

        return new NotificationCompat.Builder(context, channelId)
                .setContentTitle(context.getString(R.string.app_name))
                .setTicker(context.getString(R.string.listening_for_keyword))
                .setContentText(context.getString(R.string.listening_for_keyword))
                .setSmallIcon(R.drawable.ic_notification_speech)
                .setLargeIcon(largeIcon)
                .setContentIntent(pendingIntent)
                .setOngoing(true).build();
    }

}
