package com.martnijzink.nextforspotify.speech;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.martnijzink.nextforspotify.R;
import com.martnijzink.nextforspotify.common.AudioFilePlayer;
import com.martnijzink.nextforspotify.notification.NotificationBuilder;
import com.martnijzink.nextforspotify.notification.NotificationChannelBuilder;

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

public class PocketSphinxService extends Service implements KeywordListenerActor {

    private static final String LOG_TAG = PocketSphinxService.class.getName();
    private static final int NOTIFICATION_ID = 8103;
    private static final String CHANNEL_ID = "channel_id";

    public static final String START_FOREGROUND = "com.martnijzink.nextforspotify.speech.PocketSphinxService.startforeground";
    public static final String STOP_FOREGROUND = "com.martnijzink.nextforspotify.speech.PocketSphinxService.stopforeground";
    public static final String KEYWORD_HEARD = "com.martnijzink.nextforspotify.speech.PocketSphinxService.keywordheard";
    public static final String SPEECH_READY = "com.martnijzink.nextforspotify.speech.PocketSphinxService.speachready";
    public static final String SPEECH_OFF = "com.martnijzink.nextforspotify.speech.PocketSphinxService.speachoff";

    private final IBinder binder = new LocalBinder();
    private SpeechRecognizer recognizer;
    private AudioFilePlayer audioPlayer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        NotificationChannelBuilder.build(getString(R.string.channel_name), getString(R.string.channel_description), CHANNEL_ID, this);

        if (intent != null) {
            if (START_FOREGROUND.equals(intent.getAction())) {
                Log.d(LOG_TAG, "received start foreground intent ");

                audioPlayer = new AudioFilePlayer();

                showNotification();
                createRecognizer();
            } else if (STOP_FOREGROUND.equals(intent.getAction())) {
                Log.d(LOG_TAG, "received stop foreground intent");

                stopForeground(true);
                stopSelf(); // calls onDestroy()
            }
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
        PocketSphinxService getService() {
            // Return this instance of LocalService so clients can call public methods
            return PocketSphinxService.this;
        }
    }

    private void createRecognizer() {
        try {
            Assets assets = new Assets(this);
            File assetsDir = assets.syncAssets();
            recognizer = SpeechRecognizerSetup.defaultSetup()
                    .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                    .setDictionary(new File(assetsDir, "just-skip.dict"))
//                    .setRawLogDir(assetsDir) // To enable logging of raw audio, uncomment this call (takes a lot of space on the device)
                    .getRecognizer();

            new PocketSphinxRecognizer(recognizer, getString(R.string.keyword_next), this).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onKeywordHeard() {
        Log.d(LOG_TAG, "sending message from speech service to listen activity");
        sendIntent(KEYWORD_HEARD);

        beep();
    }

    private void beep() {
        if (audioPlayer != null) {
            audioPlayer.play(this, R.raw.beep);
        }
    }

    @Override
    public void onReadyForSpeech() {
        sendIntent(SPEECH_READY);
    }

    @Override
    public void onDestroy() {
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }

        sendIntent(SPEECH_OFF);

        super.onDestroy();
    }

}
