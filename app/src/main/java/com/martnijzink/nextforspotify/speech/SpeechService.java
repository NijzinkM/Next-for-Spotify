package com.martnijzink.nextforspotify.speech;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.speech.SpeechRecognizer;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.martnijzink.nextforspotify.R;
import com.martnijzink.nextforspotify.notification.NotificationBuilder;
import com.martnijzink.nextforspotify.notification.NotificationChannelBuilder;

public class SpeechService extends Service implements KeywordListenerActor {

    private static final String LOG_TAG = SpeechService.class.getName();
    private static final int NOTIFICATION_ID = 8103;
    private static final String CHANNEL_ID = "channel_id";

    public static final String START_FOREGROUND = "com.martnijzink.nextforspotify.speech.SpeechService.startforeground";
    public static final String STOP_FOREGROUND = "com.martnijzink.nextforspotify.speech.SpeechService.stopforeground";
    public static final String KEYWORD_HEARD = "com.martnijzink.nextforspotify.speech.SpeechService.keywordheard";
    public static final String SPEECH_READY = "com.martnijzink.nextforspotify.speech.SpeechService.speachready";
    public static final String SPEECH_OFF = "com.martnijzink.nextforspotify.speech.SpeechService.speachoff";

    private final IBinder binder = new LocalBinder();
    private SpeechRecognizer speech;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        NotificationChannelBuilder.build(getString(R.string.channel_name), getString(R.string.channel_description), CHANNEL_ID, this);

        if (START_FOREGROUND.equals(intent.getAction())) {
            Log.d(LOG_TAG, "received start foreground intent ");

            showNotification();
            createListener();
        } else if (STOP_FOREGROUND.equals(intent.getAction())) {
            Log.d(LOG_TAG, "received stop foreground intent");

            sendIntent(SPEECH_OFF);

            stopForeground(true);
            stopSelf(); // calls onDestroy()
        }
        return START_STICKY;
    }

    private void sendIntent(String action) {
        Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void showNotification() {
        Notification notification = new NotificationBuilder(this, CHANNEL_ID).buildSpeechNotification();
        startForeground(NOTIFICATION_ID, notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    class LocalBinder extends Binder {
        SpeechService getService() {
            // Return this instance of LocalService so clients can call public methods
            return SpeechService.this;
        }
    }

    private void createListener() {
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        KeywordListener listener = new KeywordListener(speech, getString(R.string.keyword_next), this);
        listener.start();
    }

    @Override
    public void onKeywordHeard() {
        Log.d(LOG_TAG, "sending message from speech service to listen activity");
        sendIntent(KEYWORD_HEARD);
    }

    @Override
    public void onReadyForSpeech() {
        sendIntent(SPEECH_READY);
    }

    @Override
    public void onDestroy() {
        speech.destroy();
        super.onDestroy();
    }

}
