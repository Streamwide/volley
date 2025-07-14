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

import com.streamwide.smartms.volley.api.VolleyError;

/**
 * Indicates that the connection or the socket timed out.
 */
@SuppressWarnings("serial")
public class TimeoutError extends VolleyError {

    @Override
    public int getErrorType()
    {
        return TIMEOUT_ERROR;
    }
}
