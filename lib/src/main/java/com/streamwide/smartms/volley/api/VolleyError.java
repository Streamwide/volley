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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Exception style class encapsulating Volley errors
 */
public class VolleyError extends Exception {

    public final NetworkResponse networkResponse;
    private long networkTimeMs;

    public static final int VOLLEY_ERROR = 0;
    public static final int AUTH_FAILURE_ERROR = 1;
    public static final int NETWORK_ERROR = 2;
    public static final int PARSE_ERROR = 3;
    public static final int SERVER_ERROR = 4;
    public static final int TIMEOUT_ERROR = 5;
    public static final int NO_CONNECTION_ERROR = 6;

    public VolleyError()
    {
        networkResponse = null;
    }

    public VolleyError(@Nullable NetworkResponse response)
    {
        networkResponse = response;
    }

    public VolleyError(@NonNull String exceptionMessage)
    {
        super(exceptionMessage);
        networkResponse = null;
    }

    public VolleyError(@NonNull String exceptionMessage, @NonNull Throwable reason)
    {
        super(exceptionMessage, reason);
        networkResponse = null;
    }

    public VolleyError(@NonNull Throwable cause)
    {
        super(cause);
        networkResponse = null;
    }

     public void setNetworkTimeMs(long networkTimeMs)
    {
        this.networkTimeMs = networkTimeMs;
    }

    public long getNetworkTimeMs()
    {
        return networkTimeMs;
    }

    public int getErrorType()
    {
        return VOLLEY_ERROR;
    }
}
