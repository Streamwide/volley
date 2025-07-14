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

package com.streamwide.smartms.volley.toolbox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streamwide.smartms.volley.api.Cache;

/**
 * A cache that doesn't.
 */
public class NoCache implements Cache {

    @Override
    public void clear()
    {
        // do nothing...
    }

    @Override
    @Nullable
    public Entry get(@NonNull String key)
    {
        return null;
    }

    @Override
    public void put(@NonNull String key, @NonNull Entry entry)
    {
        // do nothing...
    }

    @Override
    public void invalidate(@NonNull String key, boolean fullExpire)
    {
        // do nothing...
    }

    @Override
    public void remove(@NonNull String key)
    {
        // do nothing...
    }

    @Override
    public void initialize()
    {
        // do nothing...
    }
}
