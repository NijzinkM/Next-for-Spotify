package com.martnijzink.nextforspotify.speech;

import android.app.Service;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

public class SpeechServiceConnection implements ServiceConnection {

    private Service speechService;
    private boolean bound;

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        speechService = ((PocketSphinxService.LocalBinder) iBinder).getService();
        bound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        speechService = null;
        bound = false;
    }

    public Service getSpeechService() {
        return speechService;
    }

    public boolean isBound() {
        return bound;
    }
}
