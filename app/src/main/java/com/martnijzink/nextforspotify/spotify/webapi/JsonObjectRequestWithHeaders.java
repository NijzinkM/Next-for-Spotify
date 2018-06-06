package com.martnijzink.nextforspotify.spotify.webapi;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.Map;

public class JsonObjectRequestWithHeaders extends JsonObjectRequest {

    private Map<String, String> keyValuePairs;

    public JsonObjectRequestWithHeaders(int method, String url, JSONObject jsonRequest,
                                        Response.Listener<JSONObject> listener,
                                        Response.ErrorListener errorListener,
                                        Map<String, String> keyValuePairs) {
        super(method, url, jsonRequest, listener, errorListener);
        this.keyValuePairs = keyValuePairs;
    }


    @Override
    public Map<String, String> getHeaders() {
        return keyValuePairs;
    }
}
