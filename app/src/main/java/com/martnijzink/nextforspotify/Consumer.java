package com.martnijzink.nextforspotify;

/**
 * Not available in API 16
 *
 * @param <T> type of object to consume
 */
public interface Consumer<T> {

    void accept(T obj);
}
