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

package com.streamwide.smartms.volley.util;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;

import com.streamwide.smartms.volley.toolbox.ImageLoader.ImageCache;

public class LruBitmapCache extends LruCache<String, Bitmap> implements ImageCache {

    public static int getDefaultLruCacheSize()
    {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        return maxMemory / 8;
    }

    public LruBitmapCache()
    {
        this(getDefaultLruCacheSize());
    }

    public LruBitmapCache(int sizeInKiloBytes)
    {
        super(sizeInKiloBytes);
    }

    @Override
    protected int sizeOf(@NonNull String key, @NonNull Bitmap value)
    {
        return value.getRowBytes() * value.getHeight() / 1024;
    }

    @Override
    @Nullable
    public Bitmap getBitmap(@NonNull String url)
    {
        return get(url);
    }

    @Override
    public void putBitmap(@NonNull String url, @NonNull Bitmap bitmap)
    {
        put(url, bitmap);
    }
}
