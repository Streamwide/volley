/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Tue, 17 Jun 2025 11:32:22 +0100
 * @copyright  Copyright (c) 2025 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2025 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Thu, 6 Mar 2025 11:15:28 +0100
 */

package com.streamwide.smartms.volley.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streamwide.smartms.volley.AuthFailureError;
import com.streamwide.smartms.volley.ParseError;
import com.streamwide.smartms.volley.api.Response.ErrorListener;
import com.streamwide.smartms.volley.api.Response.Listener;
import com.streamwide.smartms.volley.toolbox.HurlStack;
import com.streamwide.smartms.volley.toolbox.JsonRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * A request for retrieving a {@link JSONObject} response body at a given URL,
 * allowing for an
 * optional {@link JSONObject} to be passed in as part of the request body.
 */
public class JsonObjectRequest extends JsonRequest<JSONObject> {

    /**
     * Creates a new request.
     * 
     * @param method
     *            the HTTP method to use
     * @param url
     *            URL to fetch the JSON from
     * @param jsonRequest
     *            A {@link JSONObject} to post with the request. Null is allowed
     *            and
     *            indicates no parameters will be posted along with request.
     * @param listener
     *            Listener to receive the JSON response
     * @param errorListener
     *            Error listener, or null to ignore errors.
     */
    public JsonObjectRequest(@NonNull String method, @Nullable String url, @Nullable JSONObject jsonRequest, @NonNull Listener<JSONObject> listener,
                             @Nullable ErrorListener errorListener)
    {
        super(method, url, (jsonRequest == null) ? null : jsonRequest.toString(), listener, errorListener);
    }

    /**
     * Constructor which defaults to <code>GET</code> if
     * <code>jsonRequest</code> is <code>null</code>, <code>POST</code>
     * otherwise.
     * 
     * @see #JsonObjectRequest(String, String, JSONObject, Listener, ErrorListener)
     */
    public JsonObjectRequest(@Nullable String url, @Nullable JSONObject jsonRequest, @Nullable Listener<JSONObject> listener,
                             @Nullable ErrorListener errorListener)
    {
        this(jsonRequest == null ? HurlStack.HttpMethod.GET : HurlStack.HttpMethod.POST, url, jsonRequest, listener,
                        errorListener);
    }

    @Override
    @Nullable
    public Response<JSONObject> parseNetworkResponse(@NonNull NetworkResponse response)
    {
        try {
            String jsonString =
                new String(response.data, HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
            return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }

    @NonNull
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json; charset=UTF-8");
        return headers;
    }
}
