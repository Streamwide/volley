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
 * @lastModifiedOn Tue, 4 Mar 2025 10:40:02 +0100
 */

package com.streamwide.smartms.volley.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Encapsulates a parsed response for delivery.
 * 
 * @param <T>
 *            Parsed type of this response
 */
public class Response<T> {

    /** Callback interface for delivering parsed responses. */
    public interface Listener<T> {

        /** Called when a response is received. */
        public void onResponse(@Nullable T response);
    }

    /** Callback interface for delivering error responses. */
    public interface ErrorListener {

        /**
         * Callback method that an error has been occurred with the
         * provided error code and optional user-readable message.
         */
        public void onErrorResponse(@Nullable VolleyError error);
    }

    /** Returns a successful response containing the parsed result. */
    @NonNull
    public static <T> Response<T> success(@Nullable T result, @Nullable Cache.Entry cacheEntry)
    {
        return new Response<>(result, cacheEntry);
    }

    /**
     * Returns a failed response containing the given error code and an optional
     * localized message displayed to the user.
     */
    @NonNull
    public static <T> Response<T> error(@NonNull VolleyError error)
    {
        return new Response<>(error);
    }

    /** Parsed response, or null in the case of error. */
    public final T result;

    /** Cache metadata for this response, or null in the case of error. */
    public final Cache.Entry cacheEntry;

    /** Detailed error information if <code>errorCode != OK</code>. */
    public final VolleyError mError;

    /**
     * True if this response was a soft-expired one and a second one MAY be
     * coming.
     */
    private boolean intermediate = false;

    public boolean isIntermediate()
    {
        return intermediate;
    }

    void setIntermediate(boolean intermediate)
    {
        this.intermediate = intermediate;
    }

    /**
     * Returns whether this response is considered successful.
     */
    public boolean isSuccess()
    {
        return mError == null;
    }

    private Response(T result, Cache.Entry cacheEntry)
    {
        this.result = result;
        this.cacheEntry = cacheEntry;
        this.mError = null;
    }

    private Response(VolleyError error)
    {
        this.result = null;
        this.cacheEntry = null;
        this.mError = error;
    }
}
