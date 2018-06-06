package com.martnijzink.nextforspotify.spotify;

public enum SpotifyConnectStatus {
    DEVICE_NOT_FOUND,
    USER_NOT_PREMIUM,
    DEVICE_TEMPORARILY_UNAVAILABLE,
    OTHER;

    public static SpotifyConnectStatus createWithStatusCode(int status) {
        switch (status) {
            case 202:
                return DEVICE_TEMPORARILY_UNAVAILABLE;
            case 404:
                return DEVICE_NOT_FOUND;
            case 403:
                return USER_NOT_PREMIUM;
            default:
                return OTHER;
        }
    }
}
