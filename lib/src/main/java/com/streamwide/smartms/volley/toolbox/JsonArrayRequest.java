/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Tue, 4 Mar 2025 12:46:23 +0100
 * @copyright  Copyright (c) 2025 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2025 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Tue, 4 Mar 2025 10:37:37 +0100
 */

package com.streamwide.smartms.volley.toolbox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streamwide.smartms.volley.api.NetworkResponse;
import com.streamwide.smartms.volley.ParseError;
import com.streamwide.smartms.volley.api.Response;
import com.streamwide.smartms.volley.api.Response.ErrorListener;
import com.streamwide.smartms.volley.api.Response.Listener;
import com.streamwide.smartms.volley.api.HttpHeaderParser;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;

/**
 * A request for retrieving a {@link JSONArray} response body at a given URL.
 */
public class JsonArrayRequest extends JsonRequest<JSONArray> {

    /**
     * Creates a new request.
     * 
     * @param url
     *            URL to fetch the JSON from
     * @param listener
     *            Listener to receive the JSON response
     * @param errorListener
     *            Error listener, or null to ignore errors.
     */
    public JsonArrayRequest(@Nullable String url, @NonNull Listener<JSONArray> listener, @Nullable ErrorListener errorListener)
    {
        super(HurlStack.HttpMethod.GET, url, null, listener, errorListener);
    }

    /**
     * Creates a new request.
     * 
     * @param method
     *            the HTTP method to use
     * @param url
     *            URL to fetch the JSON from
     * @param jsonRequest
     *            A {@link JSONArray} to post with the request. Null is allowed
     *            and
     *            indicates no parameters will be posted along with request.
     * @param listener
     *            Listener to receive the JSON response
     * @param errorListener
     *            Error listener, or null to ignore errors.
     */
    public JsonArrayRequest(@NonNull String method,@Nullable String url, @Nullable JSONArray jsonRequest, @NonNull Listener<JSONArray> listener,
                   @Nullable ErrorListener errorListener)
    {
        super(method, url, (jsonRequest == null) ? null : jsonRequest.toString(), listener, errorListener);
    }

    @Override
    @Nullable
    public Response<JSONArray> parseNetworkResponse(@NonNull NetworkResponse response)
    {
        try {
            String jsonString =
                new String(response.data, HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
            return Response.success(new JSONArray(jsonString), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }
}
