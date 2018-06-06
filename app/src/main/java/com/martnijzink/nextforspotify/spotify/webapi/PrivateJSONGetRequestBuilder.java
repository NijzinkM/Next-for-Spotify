package com.martnijzink.nextforspotify.spotify.webapi;

import android.support.annotation.NonNull;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.martnijzink.nextforspotify.Consumer;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PrivateJSONGetRequestBuilder extends JSONGetRequestBuilder {

    private String accessToken;

    public PrivateJSONGetRequestBuilder(@NonNull String url, Consumer<JSONObject> onJSONRetrieved,
                                        @NonNull Consumer<VolleyError> onError,
                                        @NonNull String accessToken){
        super(url, onJSONRetrieved, onError);
        this.accessToken = accessToken;
    }

    @Override
    public JsonObjectRequest getJsonObjectRequest() {
        return super.getJsonObjectRequest(authorizationHeader(accessToken));
    }

    public static Map<String, String> authorizationHeader(String accessToken) {
        Map<String, String> headers = new HashMap<>();

        headers.put("Authorization", "Bearer " + accessToken);

        return headers;
    }
}
