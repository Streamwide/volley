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
import com.streamwide.smartms.volley.api.Request;
import com.streamwide.smartms.volley.api.Response;
import com.streamwide.smartms.volley.api.Response.ErrorListener;
import com.streamwide.smartms.volley.api.Response.Listener;
import com.streamwide.smartms.volley.VolleyLog;

import java.io.UnsupportedEncodingException;

/**
 * A request for retrieving a T type response body at a given URL that also
 * optionally sends along a JSON body in the request specified.
 * 
 * @param <T>
 *            JSON type of response expected
 */
public abstract class JsonRequest<T> extends Request<T> {

    /** Default charset for JSON request. */
    protected static final String PROTOCOL_CHARSET = "utf-8";

    /** Content type for request. */
    private static final String PROTOCOL_CONTENT_TYPE = "application/json; charset="+PROTOCOL_CHARSET;

    private final Listener<T> mListener;
    private final String mRequestBody;

    /**
     * Deprecated constructor for a JsonRequest which defaults to GET unless
     * {@link #getPostBody()} or {@link #getPostParams()} is overridden (which
     * defaults to POST).
     * 
     * @deprecated Use
     *             {@link #JsonRequest(String, String, String, Listener, ErrorListener)}
     *             .
     */
    @Deprecated
    public JsonRequest(@Nullable String url, @Nullable String requestBody, @NonNull Listener<T> listener, @Nullable ErrorListener errorListener)
    {
        this(HurlStack.HttpMethod.GET, url, requestBody, listener, errorListener);
    }

    public JsonRequest(@NonNull String method, @Nullable String url, @Nullable String requestBody, @NonNull Listener<T> listener, @Nullable ErrorListener errorListener)
    {
        super(method, url, errorListener);
        mListener = listener;
        mRequestBody = requestBody;
    }

    @Override
    public void deliverResponse(@Nullable T response)
    {
        mListener.onResponse(response);
    }

    @Override
    @Nullable
    public abstract Response<T> parseNetworkResponse(@NonNull NetworkResponse response);

    /**
     * @deprecated Use {@link #getBodyContentType()}.
     */
    @Deprecated
    @Override
    @NonNull
    public String getPostBodyContentType()
    {
        return getBodyContentType();
    }

    /**
     * @deprecated Use {@link #getBody()}.
     */
    @Deprecated
    @Override
    @Nullable
    public byte[] getPostBody()
    {
        return getBody();
    }

    @Override
    @NonNull
    public String getBodyContentType()
    {
        return PROTOCOL_CONTENT_TYPE;
    }

    @Override
    @Nullable
    public byte[] getBody()
    {
        try {
            return mRequestBody == null ? null : mRequestBody.getBytes(PROTOCOL_CHARSET);
        } catch (UnsupportedEncodingException uee) {
            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody,
                            PROTOCOL_CHARSET);
            return null;
        }
    }


}
