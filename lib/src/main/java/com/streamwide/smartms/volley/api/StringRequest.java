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

package com.streamwide.smartms.volley.api;

import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streamwide.smartms.volley.api.Response.ErrorListener;
import com.streamwide.smartms.volley.api.Response.Listener;
import com.streamwide.smartms.volley.toolbox.HurlStack;

import java.io.UnsupportedEncodingException;

/**
 * A canned request for retrieving the response body at a given URL as a String.
 */
public class StringRequest extends Request<String> {

    /**
     * Lock to guard mListener as it is cleared on cancel() and read on delivery.
     */
    private final Object mLock = new Object();

    @Nullable
    @GuardedBy("mLock")
    private Listener<String> mListener;

    /**
     * Creates a new request with the given method.
     * 
     * @param method
     *            the request
     *            to use
     * @param url
     *            URL to fetch the string at
     * @param listener
     *            Listener to receive the String response
     * @param errorListener
     *            Error listener, or null to ignore errors
     */
    public StringRequest(@NonNull String method, @Nullable String url, @Nullable Listener<String> listener, @Nullable ErrorListener errorListener)
    {
        super(method, url, errorListener);
        mListener = listener;
    }

    /**
     * Creates a new GET request.
     *
     * @param url
     *            URL to fetch the string at
     * @param listener
     *            Listener to receive the String response
     * @param errorListener
     *            Error listener, or null to ignore errors
     */
    public StringRequest(@Nullable String url, @Nullable Listener<String> listener, @Nullable ErrorListener errorListener)
    {
        this(HurlStack.HttpMethod.GET, url, listener, errorListener);
    }

    @Override
    public void cancel()
    {
        super.cancel();
        synchronized (mLock) {
            mListener = null;
        }
    }

    @Override
    public void deliverResponse(@Nullable String response)
    {
        Listener<String> listener;
        synchronized (mLock) {
            listener = mListener;
        }
        if (listener != null) {
            listener.onResponse(response);
        }
    }

    @Override
    @Nullable
    public Response<String> parseNetworkResponse(@NonNull NetworkResponse response)
    {
        String parsed;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }


}
