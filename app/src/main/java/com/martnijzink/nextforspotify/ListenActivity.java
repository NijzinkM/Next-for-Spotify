package com.martnijzink.nextforspotify;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.martnijzink.nextforspotify.speech.PocketSphinxService;
import com.martnijzink.nextforspotify.speech.SpeechServiceConnection;
import com.martnijzink.nextforspotify.spotify.DeviceMonitor;
import com.martnijzink.nextforspotify.spotify.SpotifyConnect;
import com.martnijzink.nextforspotify.spotify.SpotifyConnectException;
import com.martnijzink.nextforspotify.spotify.SpotifyConnectStatus;
import com.martnijzink.nextforspotify.spotify.objects.Device;

public class ListenActivity extends AppCompatActivity implements NoDeviceDialogFragment.Listener {

    private static final String LOG_TAG = ListenActivity.class.getName();
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private final String [] permissions = {Manifest.permission.RECORD_AUDIO};
    private TextView deviceText;
    private ImageButton speakButton;
    private boolean permissionToRecordAccepted = false;
    private String accessToken;
    private SpotifyConnect connect;
    private DeviceMonitor deviceMonitor;
    private SpeechServiceConnection connection;
    private BroadcastReceiver receiver;
    private boolean listening;
    private boolean bound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "activity created");
        setContentView(R.layout.activity_listen);

        retrieveExtras();

        deviceText = findViewById(R.id.text_device);
        speakButton = findViewById(R.id.button_speak);

        speakButton.setEnabled(false);

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        connect = new SpotifyConnect(accessToken, this);

        createServiceConnection();

        deviceMonitor = new DeviceMonitor(connect, new Consumer<Device>() {
            @Override
            public void accept(Device device) {
                Log.d(LOG_TAG, "active device found");
                updateDeviceText(getString(R.string.current_device, device.getName()));
                if (!listening) {
                    switchListeningOnOff();
                }
            }
        }, new Consumer<SpotifyConnectException>() {
            @Override
            public void accept(SpotifyConnectException e) {
                Log.e(LOG_TAG, "spotify connect error", e);
                Toast.makeText(ListenActivity.this, R.string.devices_search_failed, Toast.LENGTH_LONG).show();
                updateDeviceText(getString(R.string.no_active_device_found));
            }
        }, new Runnable() {
            @Override
            public void run() {
                Log.d(LOG_TAG, "no active non-restricted device found");
                updateDeviceText(getString(R.string.no_active_device_found));

                noDeviceDialog();
            }
        });
    }

    private void noDeviceDialog() {
        Log.d(LOG_TAG, "displaying no device dialog");
        new NoDeviceDialogFragment().show(getSupportFragmentManager(), "");
    }


    public void onClickListen(View view) {
        Log.d(LOG_TAG, "speech button clicked");
        switchListeningOnOff();
    }

    private void switchListeningOnOff() {
        speakButton.setEnabled(false);

        if (!listening) {
            sendStartServiceIntent();
        } else {
            sendStopServiceIntent();
        }
    }

    private void doBindService(Intent intent) {
        bindService(intent, connection, BIND_AUTO_CREATE);
        bound = true;
    }

    private void doUnbindService() {
        unbindService(connection);
        bound = false;
    }

    private void createServiceConnection() {
        Log.d(LOG_TAG, "creating service connection");

        connection = new SpeechServiceConnection();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    final String action = intent.getAction();

                    Log.d(LOG_TAG, "action received from speech service: " + action);

                    handleSpeechServiceAction(action);
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(PocketSphinxService.KEYWORD_HEARD);
        filter.addAction(PocketSphinxService.SPEECH_READY);
        filter.addAction(PocketSphinxService.SPEECH_OFF);

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    private void handleSpeechServiceAction(String action) {
        if (PocketSphinxService.KEYWORD_HEARD.equals(action)) {
            goToNextTrack();
        } else if (PocketSphinxService.SPEECH_READY.equals(action)) {
            listening = true;
            speakButton.setEnabled(true);
            speakButton.setImageResource(R.drawable.white_mic_disableable);
        } else if (PocketSphinxService.SPEECH_OFF.equals(action)) {
            listening = false;
            speakButton.setEnabled(true);
            speakButton.setImageResource(R.drawable.white_mic_off_disableable);
        }
    }

    @Override
    public void onClickSearchAgain() {
        updateDeviceText(getString(R.string.dots));

        deviceMonitor.searchDevice();
    }

    @Override
    public void onClickCancel() {
        finish();
    }

    private void retrieveExtras() {
        accessToken = getIntent().getStringExtra(LoginActivity.ACCESS_TOKEN_KEY);

        if (accessToken == null) {
            Log.w(LOG_TAG, "no accessToken received: 'skip' command for Spotify player will fail");
        } else {
            Log.d(LOG_TAG, "accessToken received");
        }
    }

    private void updateDeviceText(String text) {
        deviceText.setText(text);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION){
            Log.d(LOG_TAG, "record audio permission granted");
            permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }

        if (!permissionToRecordAccepted ) {
            Log.w(LOG_TAG, "record audio permission not granted!");
            Toast.makeText(this, R.string.permission_not_granted, Toast.LENGTH_LONG).show();
        } else {
            deviceMonitor.searchDevice();
        }
    }

    private void goToNextTrack() {
        Log.d(LOG_TAG, "going to next track");

        final Consumer<SpotifyConnectStatus> statusConsumer = new Consumer<SpotifyConnectStatus>() {
            @Override
            public void accept(SpotifyConnectStatus status) {
                showStatus(status);
            }
        };

        try {
            connect.next(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ListenActivity.this, R.string.next, Toast.LENGTH_SHORT).show();
                }
            }, statusConsumer, new Consumer<SpotifyConnectException>() {
                @Override
                public void accept(SpotifyConnectException e) {
                    Log.e(LOG_TAG, "spotify connection error", e);
                    Toast.makeText(ListenActivity.this, R.string.next_track_failed_internal_error, Toast.LENGTH_LONG).show();
                }
            });
        } catch (SpotifyConnectException e) {
            Log.e(LOG_TAG, "spotify connect exception", e);
        }
    }

    private void showStatus(SpotifyConnectStatus status) {
        String cause;
        switch (status) {
            case DEVICE_NOT_FOUND:
                deviceMonitor.searchDevice(new Runnable() {
                    @Override
                    public void run() {
                        goToNextTrack();
                    }
                });
                break;
            case USER_NOT_PREMIUM:
                cause = getString(R.string.cause_user_not_premium);
                Toast.makeText(ListenActivity.this, getString(R.string.next_track_failed_because, cause), Toast.LENGTH_LONG).show();
                break;
            case DEVICE_TEMPORARILY_UNAVAILABLE:
                cause = getString(R.string.cause_device_temporarily_unavailable);
                Toast.makeText(ListenActivity.this, getString(R.string.next_track_failed_because, cause), Toast.LENGTH_LONG).show();
                break;
            default:
                cause = getString(R.string.cause_other);
                Toast.makeText(ListenActivity.this, getString(R.string.next_track_failed_because, cause), Toast.LENGTH_LONG).show();
        }
    }

    private void sendStartServiceIntent() {
        Log.d(LOG_TAG, "sending start service intent");

        Intent intent = new Intent(this, PocketSphinxService.class);
        intent.setAction(PocketSphinxService.START_FOREGROUND);
        startService(intent);
        doBindService(intent);
}

    private void sendStopServiceIntent() {
        Log.d(LOG_TAG, "sending stop service intent");

        Intent intent = new Intent(this, PocketSphinxService.class);
        intent.setAction(PocketSphinxService.STOP_FOREGROUND);
        doUnbindService();
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);

        if (connection != null && bound) {
            sendStopServiceIntent();
        }

        super.onDestroy();
    }

}
