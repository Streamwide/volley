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

import androidx.annotation.Nullable;

import com.streamwide.smartms.volley.Network;

import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.Map;

/**
 * Data and headers returned from {@link Network#performRequest(Request)}.
 */
public class NetworkResponse {

    /**
     * Creates a new network response.
     * 
     * @param statusCode
     *            the HTTP status code
     * @param data
     *            Response body
     * @param headers
     *            Headers returned with this response, or null for none
     * @param notModified
     *            True if the server returned a 304 and the data was already in
     *            cache
     * @param networkTimeMs
     *            Round-trip network time to receive network response
     */
    public NetworkResponse(int statusCode, @Nullable byte[] data, @Nullable Map<String, String> headers, boolean notModified,
                           long networkTimeMs)
    {
        this.statusCode = statusCode;
        this.data = data;
        this.headers = headers;
        this.notModified = notModified;
        this.networkTimeMs = networkTimeMs;
    }

    public NetworkResponse(int statusCode, @Nullable byte[] data, @Nullable Map<String, String> headers, boolean notModified)
    {
        this(statusCode, data, headers, notModified, 0);
    }

    public NetworkResponse(@Nullable byte[] data)
    {
        this(HttpURLConnection.HTTP_OK, data, Collections.<String, String> emptyMap(), false, 0);
    }

    public NetworkResponse(@Nullable byte[] data, @Nullable Map<String, String> headers)
    {
        this(HttpURLConnection.HTTP_OK, data, headers, false, 0);
    }

    /** The HTTP status code. */
    public final int statusCode;

    /** Raw data from this response. */
    public final byte[] data;

    /** Response headers. */
    public final Map<String, String> headers;

    /** True if the server returned a 304 (Not Modified). */
    public final boolean notModified;

    /** Network roundtrip time in milliseconds. */
    public final long networkTimeMs;
}
