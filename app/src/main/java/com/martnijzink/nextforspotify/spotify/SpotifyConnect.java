package com.martnijzink.nextforspotify.spotify;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.martnijzink.nextforspotify.spotify.objects.Device;
import com.martnijzink.nextforspotify.Consumer;
import com.martnijzink.nextforspotify.spotify.webapi.WebAPI;
import com.martnijzink.nextforspotify.spotify.webapi.WebAPIException;

import java.util.List;

public class SpotifyConnect {

    private static final String LOG_TAG = SpotifyConnect.class.getName();

    private WebAPI webAPI;
    private Device activeDevice;

    public SpotifyConnect(String accessToken, Context context) {
        webAPI = new WebAPI(context, accessToken);
    }

    public void getActiveDevice(@NonNull final Consumer<Device> onActiveDeviceFound,
                                @NonNull final Consumer<SpotifyConnectException> onError,
                                @NonNull final Runnable onNoDevice) {
        final Consumer<List<Device>> devicesListConsumer = new Consumer<List<Device>>() {
            @Override
            public void accept(List<Device> devices) {
                Device activeDevice = null;

                for (Device device : devices) {
                    if (device.isActive() && !device.isRestricted()) {
                        activeDevice = device;
                    }
                }

                if (activeDevice != null) {
                    SpotifyConnect.this.activeDevice = activeDevice;
                    onActiveDeviceFound.accept(activeDevice);
                } else {
                    onNoDevice.run();
                }
            }
        };

        webAPI.requestDevices(devicesListConsumer, new Consumer<WebAPIException>() {
            @Override
            public void accept(WebAPIException e) {
                onError.accept(new SpotifyConnectException(e));
            }
        });
    }

    public void next(@NonNull final Runnable onSuccess, @NonNull final Consumer<SpotifyConnectStatus> onStatus, final Consumer<SpotifyConnectException> onError) throws SpotifyConnectException {
        if (activeDevice == null) {
            throw new SpotifyConnectException("No active device. Call getActiveDevice first");
        }

        webAPI.nextTrack(new Consumer<Integer>() {
            @Override
            public void accept(Integer statusCode) {
                Log.d(LOG_TAG, "status code " + statusCode);

                if (statusCode == 204) {
                    onSuccess.run();
                } else {
                    onStatus.accept(SpotifyConnectStatus.createWithStatusCode(statusCode));
                }
            }
        }, new Consumer<WebAPIException>() {
            @Override
            public void accept(WebAPIException e) {
                onError.accept(new SpotifyConnectException(e));
            }
        }, activeDevice.getId());
    }
}
