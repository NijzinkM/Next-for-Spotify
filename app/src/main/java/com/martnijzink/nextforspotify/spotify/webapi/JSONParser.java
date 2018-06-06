package com.martnijzink.nextforspotify.spotify.webapi;

import android.util.Log;

import com.martnijzink.nextforspotify.spotify.objects.Device;
import com.martnijzink.nextforspotify.spotify.objects.User;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONParser {
    private static final String LOG_TAG = JSONParser.class.getName();

    public static User parseUserJSON(JSONObject data) throws JSONException {
        Log.d(LOG_TAG, "parsing user JSON");

        User user = new User();

        user.setDisplayName(data.getString("display_name"));
        user.setId(data.getString("id"));

        return user;
    }

    public static Device parseDeviceJSON(JSONObject data) throws JSONException {
        Log.d(LOG_TAG, "parsing device JSON");

        Device device = new Device();

        device.setId(data.getString("id"));
        device.setActive(data.getBoolean("is_active"));
        device.setRestricted(data.getBoolean("is_restricted"));
        device.setName(data.getString("name"));
        device.setType(data.getString("type"));

        return device;
    }
}
