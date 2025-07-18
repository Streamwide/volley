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

package com.streamwide.smartms.volley.api;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.widget.ImageView.ScaleType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streamwide.smartms.volley.DefaultRetryPolicy;
import com.streamwide.smartms.volley.ParseError;
import com.streamwide.smartms.volley.VolleyLog;
import com.streamwide.smartms.volley.toolbox.HurlStack;

/**
 * A canned request for getting an image at a given URL and calling
 * back with a decoded Bitmap.
 */
public class ImageRequest extends Request<Bitmap> {

    /** Socket timeout in milliseconds for image requests */
    private static final int IMAGE_TIMEOUT_MS = 1000;

    /** Default number of retries for image requests */
    private static final int IMAGE_MAX_RETRIES = 2;

    /** Default backoff multiplier for image requests */
    private static final float IMAGE_BACKOFF_MULT = 2f;

    private final Response.Listener<Bitmap> mListener;
    private final Config mDecodeConfig;
    private final int mMaxWidth;
    private final int mMaxHeight;
    private ScaleType mScaleType;

    /**
     * Decoding lock so that we don't decode more than one image at a time (to
     * avoid OOM's)
     */
    private static final Object sDecodeLock = new Object();

    /**
     * Creates a new image request, decoding to a maximum specified width and
     * height. If both width and height are zero, the image will be decoded to
     * its natural size. If one of the two is nonzero, that dimension will be
     * clamped and the other one will be set to preserve the image's aspect
     * ratio. If both width and height are nonzero, the image will be decoded to
     * be fit in the rectangle of dimensions width x height while keeping its
     * aspect ratio.
     * 
     * @param url
     *            URL of the image
     * @param listener
     *            Listener to receive the decoded bitmap
     * @param maxWidth
     *            Maximum width to decode this bitmap to, or zero for none
     * @param maxHeight
     *            Maximum height to decode this bitmap to, or zero for
     *            none
     * @param scaleType
     *            The ImageViews ScaleType used to calculate the needed image
     *            size.
     * @param decodeConfig
     *            Format to decode the bitmap to
     * @param errorListener
     *            Error listener, or null to ignore errors
     */
    public ImageRequest(@Nullable String url, @NonNull Response.Listener<Bitmap> listener, int maxWidth, int maxHeight,
                        @NonNull ScaleType scaleType, @NonNull Config decodeConfig, @Nullable Response.ErrorListener errorListener)
    {
        super(HurlStack.HttpMethod.GET, url, errorListener);
        setRetryPolicy(new DefaultRetryPolicy(IMAGE_TIMEOUT_MS, IMAGE_MAX_RETRIES, IMAGE_BACKOFF_MULT));
        mListener = listener;
        mDecodeConfig = decodeConfig;
        mMaxWidth = maxWidth;
        mMaxHeight = maxHeight;
        mScaleType = scaleType;
    }

    /**
     * @deprecated
     *             For API compatibility with the pre-ScaleType variant of the
     *             constructor.
     *             Equivalent to
     *             the normal constructor with {@code ScaleType.CENTER_INSIDE}.
     */
    @Deprecated
    public ImageRequest(@Nullable String url, @NonNull Response.Listener<Bitmap> listener, int maxWidth, int maxHeight,
                        @NonNull Config decodeConfig, @Nullable Response.ErrorListener errorListener)
    {
        this(url, listener, maxWidth, maxHeight, ScaleType.CENTER_INSIDE, decodeConfig, errorListener);
    }

    @Override
    @NonNull
    public Priority getPriority()
    {
        return Priority.LOW;
    }

    /**
     * Scales one side of a rectangle to fit aspect ratio.
     * 
     * @param maxPrimary
     *            Maximum size of the primary dimension (i.e. width for
     *            max width), or zero to maintain aspect ratio with secondary
     *            dimension
     * @param maxSecondary
     *            Maximum size of the secondary dimension, or zero to
     *            maintain aspect ratio with primary dimension
     * @param actualPrimary
     *            Actual size of the primary dimension
     * @param actualSecondary
     *            Actual size of the secondary dimension
     * @param scaleType
     *            The ScaleType used to calculate the needed image size.
     */
    private static int getResizedDimension(int maxPrimary, int maxSecondary, int actualPrimary, int actualSecondary,
                                           ScaleType scaleType)
    {

        // If no dominant value at all, just return the actual.
        if ((maxPrimary == 0) && (maxSecondary == 0)) {
            return actualPrimary;
        }

        // If ScaleType.FIT_XY fill the whole rectangle, ignore ratio.
        if (scaleType == ScaleType.FIT_XY) {
            if (maxPrimary == 0) {
                return actualPrimary;
            }
            return maxPrimary;
        }

        // If primary is unspecified, scale primary to match secondary's scaling
        // ratio.
        if (maxPrimary == 0) {
            double ratio = (double) maxSecondary / (double) actualSecondary;
            return (int) (actualPrimary * ratio);
        }

        if (maxSecondary == 0) {
            return maxPrimary;
        }

        double ratio = (double) actualSecondary / (double) actualPrimary;
        int resized = maxPrimary;

        // If ScaleType.CENTER_CROP fill the whole rectangle, preserve aspect
        // ratio.
        if (scaleType == ScaleType.CENTER_CROP) {
            if ((resized * ratio) < maxSecondary) {
                resized = (int) (maxSecondary / ratio);
            }
            return resized;
        }

        if ((resized * ratio) > maxSecondary) {
            resized = (int) (maxSecondary / ratio);
        }
        return resized;
    }

    @Override
    @Nullable
    public Response<Bitmap> parseNetworkResponse(@NonNull NetworkResponse response)
    {
        // Serialize all decode on a global lock to reduce concurrent heap
        // usage.
        synchronized (sDecodeLock) {
            try {
                return doParse(response);
            } catch (OutOfMemoryError e) {
                VolleyLog.e(e, "Caught OOM for %d byte image, url=%s", response.data.length, getUrl());
                return Response.error(new ParseError(e));
            }
        }
    }

    /**
     * The real guts of parseNetworkResponse. Broken out for readability.
     */
    private Response<Bitmap> doParse(NetworkResponse response)
    {
        byte[] data = response.data;
        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        Bitmap bitmap = null;
        if (mMaxWidth == 0 && mMaxHeight == 0) {
            decodeOptions.inPreferredConfig = mDecodeConfig;
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, decodeOptions);
        } else {
            // If we have to resize this image, first get the natural bounds.
            decodeOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, decodeOptions);
            int actualWidth = decodeOptions.outWidth;
            int actualHeight = decodeOptions.outHeight;

            // Then compute the dimensions we would ideally like to decode to.
            int desiredWidth = getResizedDimension(mMaxWidth, mMaxHeight, actualWidth, actualHeight, mScaleType);
            int desiredHeight = getResizedDimension(mMaxHeight, mMaxWidth, actualHeight, actualWidth, mScaleType);

            // Decode to the nearest power of two scaling factor.
            decodeOptions.inJustDecodeBounds = false;

            decodeOptions.inSampleSize = findBestSampleSize(actualWidth, actualHeight, desiredWidth, desiredHeight);
            Bitmap tempBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, decodeOptions);

            // If necessary, scale down to the maximal acceptable size.
            if (tempBitmap != null
                && (tempBitmap.getWidth() > desiredWidth || tempBitmap.getHeight() > desiredHeight)) {
                bitmap = Bitmap.createScaledBitmap(tempBitmap, desiredWidth, desiredHeight, true);
                tempBitmap.recycle();
            } else {
                bitmap = tempBitmap;
            }
        }

        if (bitmap == null) {
            return Response.error(new ParseError(response));
        } else {
            return Response.success(bitmap, HttpHeaderParser.parseCacheHeaders(response));
        }
    }

    @Override
    public void deliverResponse(@Nullable Bitmap response)
    {
        mListener.onResponse(response);
    }

    /**
     * Returns the largest power-of-two divisor for use in downscaling a bitmap
     * that will not result in the scaling past the desired dimensions.
     * 
     * @param actualWidth
     *            Actual width of the bitmap
     * @param actualHeight
     *            Actual height of the bitmap
     * @param desiredWidth
     *            Desired width of the bitmap
     * @param desiredHeight
     *            Desired height of the bitmap
     */
    // Visible for testing.
    static int findBestSampleSize(int actualWidth, int actualHeight, int desiredWidth, int desiredHeight)
    {
        double wr = (double) actualWidth / desiredWidth;
        double hr = (double) actualHeight / desiredHeight;
        double ratio = Math.min(wr, hr);
        double n = 1.0d;
        while ((n * 2) <= ratio) {
            n *= 2;
        }

        return (int) n;
    }

}
