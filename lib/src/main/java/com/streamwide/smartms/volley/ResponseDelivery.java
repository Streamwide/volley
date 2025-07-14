/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Tue, 4 Mar 2025 12:52:46 +0100
 * @copyright  Copyright (c) 2025 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2025 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Tue, 4 Mar 2025 12:46:40 +0100
 */

package com.streamwide.smartms.volley;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streamwide.smartms.volley.api.Request;
import com.streamwide.smartms.volley.api.Response;
import com.streamwide.smartms.volley.api.VolleyError;

public interface ResponseDelivery {

    /**
     * Parses a response from the network or cache and delivers it.
     */
    public void postResponse(@NonNull Request<?> request, @Nullable Response<?> response);

    /**
     * Parses a response from the network or cache and delivers it. The provided
     * Runnable will be executed after delivery.
     */
    public void postResponse(@NonNull Request<?> request, @Nullable Response<?> response, @Nullable Runnable runnable);

    /**
     * Posts an error for the given request.
     */
    public void postError(@NonNull Request<?> request, @NonNull VolleyError error);
}
