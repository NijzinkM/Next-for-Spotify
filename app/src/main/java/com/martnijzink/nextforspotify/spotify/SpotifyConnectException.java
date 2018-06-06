package com.martnijzink.nextforspotify.spotify;

public class SpotifyConnectException extends Exception {

    public SpotifyConnectException(Throwable t) {
        super(t);
    }

    public SpotifyConnectException(String message) {
        super(message);
    }
}
