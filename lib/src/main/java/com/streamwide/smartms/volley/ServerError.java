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

package com.streamwide.smartms.volley;

import androidx.annotation.Nullable;

import com.streamwide.smartms.volley.api.NetworkResponse;
import com.streamwide.smartms.volley.api.VolleyError;

/**
 * Indicates that the server responded with an error response.
 */
@SuppressWarnings("serial")
public class ServerError extends VolleyError {

    public ServerError(@Nullable NetworkResponse networkResponse)
    {
        super(networkResponse);
    }

    @Override
    public int getErrorType()
    {
        return SERVER_ERROR;
    }

    public ServerError()
    {
        super();
    }
}
