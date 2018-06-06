package com.martnijzink.nextforspotify.spotify.webapi;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.martnijzink.nextforspotify.Consumer;
import com.martnijzink.nextforspotify.spotify.objects.Device;
import com.martnijzink.nextforspotify.spotify.objects.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WebAPI {

    private static final String LOG_TAG = WebAPI.class.getName();
    private static final String BASE_URL_API = "https://api.spotify.com/v1";
    // private static final String BASE_URL_AUTH = "https://accounts.spotify.com/api";

    private RequestQueue queue;
    private String accessToken;

    public WebAPI(@NonNull Context context, String accessToken) {
        this.queue = Volley.newRequestQueue(context);
        this.accessToken = accessToken;
    }

    public void requestUser(final @NonNull Consumer<User> onUser, final @NonNull Consumer<WebAPIException> onError) {
        Log.d(LOG_TAG, "requesting user data");
        
        final String url = BASE_URL_API + "/me";

        Log.d(LOG_TAG, "building request to " + url);

        JSONGetRequestBuilder requestBuilder = new PrivateJSONGetRequestBuilder(url, new Consumer<JSONObject>() {
            @Override
            public void accept(JSONObject data) {
                Log.d(LOG_TAG, "user data retrieved");

                try {
                    onUser.accept(JSONParser.parseUserJSON(data));
                } catch (JSONException e) {
                    onError.accept(new WebAPIException(e));
                }
            }
        }, new Consumer<VolleyError>() {
            @Override
            public void accept(VolleyError e) {
                Log.e(LOG_TAG, "failed to retrieve user data", e);
                onError.accept(new WebAPIException(e));
            }
        }, accessToken);

        queue.add(requestBuilder.getJsonObjectRequest());
    }

    public void requestDevices(final @NonNull Consumer<List<Device>> onDevices, final @NonNull Consumer<WebAPIException> onError) {
        Log.d(LOG_TAG, "requesting devices");

        final String url = BASE_URL_API + "/me/player/devices";

        Log.d(LOG_TAG, "building request to");

        JSONGetRequestBuilder requestBuilder = new PrivateJSONGetRequestBuilder(url, new Consumer<JSONObject>() {
            @Override
            public void accept(JSONObject data) {
                Log.d(LOG_TAG, "devices retrieved");

                try {
                    JSONArray devicesArray = data.getJSONArray("devices");

                    List<Device> devices = new ArrayList<>();

                    for (int i = 0; i < devicesArray.length(); i++) {
                        devices.add(JSONParser.parseDeviceJSON(devicesArray.getJSONObject(i)));
                    }

                    onDevices.accept(devices);
                } catch (JSONException e) {
                    onError.accept(new WebAPIException(e));
                }
            }
        }, new Consumer<VolleyError>() {
            @Override
            public void accept(VolleyError e) {
                Log.e(LOG_TAG, "failed to retrieve devices", e);
                onError.accept(new WebAPIException(e));
            }
        }, accessToken);

        queue.add(requestBuilder.getJsonObjectRequest());
    }

    public void nextTrack(@NonNull final Consumer<Integer> onResponse, @NonNull final Consumer<WebAPIException> onError, @NonNull final String deviceId) {
        Log.d(LOG_TAG, "sending request for going to next track");

        final String url = BASE_URL_API + "/me/player/next?device_id=" + deviceId;

        Log.d(LOG_TAG, "building request to " + url);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(LOG_TAG, "skipped to next track");

                        onResponse.accept(204);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(LOG_TAG, "failed to go to next track", volleyError);

                final NetworkResponse response = volleyError.networkResponse;

                if (response == null) {
                    onError.accept(new WebAPIException(volleyError));
                } else {
                    onResponse.accept(response.statusCode);
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                return PrivateJSONGetRequestBuilder.authorizationHeader(accessToken);
            }
        };

        queue.add(request);
    }

//    public void requestRefreshToken(String code, final Consumer<String> onToken, final @NonNull Consumer<WebAPIException> onError) {
//        Log.d(LOG_TAG, "requesting refresh token");
//
//        final String url = BASE_URL_AUTH + "/token";
//
//        JSONRequest request = new JSONRequest(url, new Consumer<JSONObject>() {
//            @Override
//            public void accept(JSONObject data) {
//                try {
//                    onToken.accept(data.getString("refresh_token"));
//                } catch (JSONException e) {
//                    onError.accept(new WebAPIException(e));
//                }
//            }
//        }, new Consumer<VolleyError>() {
//            @Override
//            public void accept(VolleyError e) {
//                onError.accept(new WebAPIException(e));
//            }
//        });
//
//        Map<String, String> headers = new HashMap<>();
//
//        queue.add(request.getJsonObjectRequest());
//    }
}
