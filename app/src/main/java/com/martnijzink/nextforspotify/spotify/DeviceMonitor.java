package com.martnijzink.nextforspotify.spotify;

import com.martnijzink.nextforspotify.Consumer;
import com.martnijzink.nextforspotify.spotify.objects.Device;

public class DeviceMonitor {

    private SpotifyConnect connect;
    private Consumer<Device> onDevice;
    private Consumer<SpotifyConnectException> onError;
    private Runnable onNoDevice;
    private Device device;

    public DeviceMonitor(SpotifyConnect connect, Consumer<Device> onDevice, Consumer<SpotifyConnectException> onError, Runnable onNoDevice) {
        this.connect = connect;
        this.onDevice = onDevice;
        this.onError = onError;
        this.onNoDevice = onNoDevice;
    }

    public void searchDevice() {
        connect.getActiveDevice(new Consumer<Device>() {
            @Override
            public void accept(Device device) {
                DeviceMonitor.this.device = device;
                DeviceMonitor.this.onDevice.accept(device);
            }
        }, onError, onNoDevice);
    }

    public Device getDevice() {
        return device;
    }
}
