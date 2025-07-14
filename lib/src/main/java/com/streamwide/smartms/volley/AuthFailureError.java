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

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streamwide.smartms.volley.api.NetworkResponse;
import com.streamwide.smartms.volley.api.VolleyError;

/**
 * Error indicating that there was an authentication failure when performing a
 * Request.
 */
@SuppressWarnings("serial")
public class AuthFailureError extends VolleyError {

    /**
     * An intent that can be used to resolve this exception. (Brings up the
     * password dialog.)
     */
    private Intent mResolutionIntent;

    public AuthFailureError()
    {
    }

    public AuthFailureError(@Nullable Intent intent)
    {
        mResolutionIntent = intent;
    }

    public AuthFailureError(@Nullable NetworkResponse response)
    {
        super(response);
    }

    public AuthFailureError(@NonNull String message)
    {
        super(message);
    }

    @Override
    public int getErrorType()
    {
        return AUTH_FAILURE_ERROR;
    }

    public AuthFailureError(@NonNull String message, @NonNull Exception reason)
    {
        super(message, reason);
    }

    @Nullable
    public Intent getResolutionIntent()
    {
        return mResolutionIntent;
    }

    @Override
    @Nullable
    public String getMessage()
    {
        if (mResolutionIntent != null) {
            return "User needs to (re)enter credentials.";
        }
        return super.getMessage();
    }
}
