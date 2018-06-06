package com.martnijzink.nextforspotify.speech;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;

public class KeywordListener implements RecognitionListener {

    private static String LOG_TAG = KeywordListener.class.getName();

    private SpeechRecognizer speech;
    private String keyword;
    private KeywordListenerActor actor;
    private boolean readyForSpeechCalled;

    public KeywordListener(SpeechRecognizer speech, String keyword, KeywordListenerActor actor) {
        this.speech = speech;
        this.keyword = keyword;
        this.actor = actor;

        speech.setRecognitionListener(this);
    }

    public void start() {
        restartSR();
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {
        Log.d(LOG_TAG, "speech recognition ready");

        // Only call once
        if (!readyForSpeechCalled) {
            actor.onReadyForSpeech();
            readyForSpeechCalled = true;
        }
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d(LOG_TAG, "speech recognition beginning of speech");
    }

    @Override
    public void onRmsChanged(float v) {

    }

    @Override
    public void onBufferReceived(byte[] bytes) {

    }

    @Override
    public void onEndOfSpeech() {
        Log.d(LOG_TAG, "speech recognition end of speech");
    }

    @Override
    public void onError(int i) {
        Log.d(LOG_TAG, "speech recognition error " + i);
        restartSR();
    }

    @Override
    public void onResults(Bundle bundle) {
        Log.d(LOG_TAG, "speech recognition results received");

        ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        if (matches != null) {
            for (String result : matches) {
                Log.d(LOG_TAG, "result: " + result);
                if (result.equalsIgnoreCase(keyword)) {
                    actor.onKeywordHeard();
                    break;
                }
            }
        }

        restartSR();
    }

    private void restartSR() {
        Log.d(LOG_TAG, "(re)starting speech recognizer");

        final Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,"en-US");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 2);

        speech.startListening(recognizerIntent);
    }

    @Override
    public void onPartialResults(Bundle bundle) {

    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }

}
