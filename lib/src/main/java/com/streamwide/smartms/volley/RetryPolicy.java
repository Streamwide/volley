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
 * Retry policy for a request.
 */
public interface RetryPolicy {

    /**
     * Returns the current timeout (used for logging).
     */
    public int getCurrentTimeout();

    /**
     * Returns the current retry count (used for logging).
     */
    public int getCurrentRetryCount();

    /**
     * Prepares for the next retry by applying a backoff to the timeout.
     * 
     * @param error
     *            The error code of the last attempt.
     * @throws VolleyError
     *             In the event that the retry could not be performed (for
     *             example if we
     *             ran out of attempts), the passed in error is thrown.
     */
    public void retry(@NonNull VolleyError error) throws VolleyError;
}
