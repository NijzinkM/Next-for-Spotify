package com.martnijzink.nextforspotify.spotify.webapi;

import android.support.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.martnijzink.nextforspotify.Consumer;

import org.json.JSONObject;

import java.util.Map;

public class JSONGetRequestBuilder {

    private String url;
    private Consumer<JSONObject> onJSONRetrieved;
    private Consumer<VolleyError> onError;

    public JSONGetRequestBuilder(@NonNull String url, Consumer<JSONObject> onJSONRetrieved, Consumer<VolleyError> onError) {
        this.url = url;
        this.onJSONRetrieved = onJSONRetrieved;
        this.onError = onError;
    }

    protected JsonObjectRequest getJsonObjectRequest(Map<String, String> headers) {
        return new JsonObjectRequestWithHeaders(Request.Method.GET, url,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (onJSONRetrieved != null) {
                    onJSONRetrieved.accept(response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onError.accept(error);
            }
        }, headers);
    }

    public JsonObjectRequest getJsonObjectRequest() {
        return getJsonObjectRequest(null);
    }
}
