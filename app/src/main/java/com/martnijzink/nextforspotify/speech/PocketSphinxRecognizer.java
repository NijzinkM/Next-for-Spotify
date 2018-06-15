package com.martnijzink.nextforspotify.speech;

import android.util.Log;

import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

public class PocketSphinxRecognizer implements RecognitionListener {

    private static final String LOG_TAG = PocketSphinxRecognizer.class.getName();
    private static final String KEYWORD_SEARCH = "keywordsearch";

    private SpeechRecognizer recognizer;
    private String keyword;
    private KeywordListenerActor actor;

    public PocketSphinxRecognizer(SpeechRecognizer recognizer, String keyword, KeywordListenerActor actor) {
        this.recognizer = recognizer;
        this.keyword = keyword;
        this.actor = actor;

        recognizer.addKeyphraseSearch(KEYWORD_SEARCH, keyword);
        recognizer.addListener(this);
    }

    public void start() {
        Log.d(LOG_TAG, "speech recognition starting");
        restart();
        actor.onReadyForSpeech();
    }

    private void restart() {
        Log.d(LOG_TAG, "speech recognition restarting");
        recognizer.stop();
        recognizer.startListening(KEYWORD_SEARCH);
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d(LOG_TAG, "speech recognition beginning of speech");
    }

    @Override
    public void onEndOfSpeech() {
        Log.d(LOG_TAG, "speech recognition end of speech");
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null) {
            return;
        }

        String text = hypothesis.getHypstr();
        Log.d(LOG_TAG, "partial result: " + text);

        if (text.equals(keyword)) { // always true
            actor.onKeywordHeard();
            restart();
        }
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        Log.d(LOG_TAG, "speech recognition result received: " + hypothesis.getHypstr());
    }

    @Override
    public void onError(Exception e) {
        Log.d(LOG_TAG, "speech recognition error: " + e.getMessage());
    }

    @Override
    public void onTimeout() {
        Log.d(LOG_TAG, "speech recognition timeout");
    }

}
