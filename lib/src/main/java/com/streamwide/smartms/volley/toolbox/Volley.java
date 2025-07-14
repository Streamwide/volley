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

package com.streamwide.smartms.volley.toolbox;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streamwide.smartms.volley.Network;
import com.streamwide.smartms.volley.RequestQueue;

public class Volley {

    /** Default on-disk cache directory. */
    private static final String DEFAULT_CACHE_DIR = "volley";

    /**
     * private constructor to hide the implicit public one.
     */
    private Volley()
    {
        // do nothing...
    }

    /**
     * Creates a default instance of the worker pool and calls
     *
     * @param context
     *            A {@link Context} to use for creating the cache dir.
     * @param stack
     *            An {@link HttpStack} to use for the network, or null for
     *            default.
     * @return A started {@link RequestQueue} instance.
     */
    @NonNull
    public static RequestQueue newRequestQueue(@NonNull Context context, @Nullable HttpStack stack)
    {
        if (stack == null) {
            stack = new HurlStack();
        }

        Network network = new BasicNetwork(stack);

        RequestQueue queue = new RequestQueue(network);
        queue.start();

        return queue;
    }

    /**
     * Creates a default instance of the worker pool and calls
     * {@link RequestQueue#start()} on it.
     * 
     * @param context
     *            A {@link Context} to use for creating the cache dir.
     * @return A started {@link RequestQueue} instance.
     */
    @NonNull
    public static RequestQueue newRequestQueue(@NonNull Context context)
    {
        return newRequestQueue(context, null);
    }
}
