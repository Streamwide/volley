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

import com.streamwide.smartms.volley.api.VolleyError;

/**
 * Error indicating that no connection could be established when performing a
 * Volley request.
 */
@SuppressWarnings("serial")
public class NoConnectionError extends VolleyError {

    public NoConnectionError()
    {
        super();
    }

    public NoConnectionError(@NonNull Throwable reason)
    {
        super(reason);
    }

    @Override
    public int getErrorType()
    {
        return NO_CONNECTION_ERROR;
    }
}
