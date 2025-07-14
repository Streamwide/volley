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
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.streamwide.smartms.volley.api.VolleyError;
import com.streamwide.smartms.volley.toolbox.ImageLoader.ImageContainer;
import com.streamwide.smartms.volley.toolbox.ImageLoader.ImageListener;

/**
 * Handles fetching an image from a URL as well as the life-cycle of the
 * associated request.
 */
public class NetworkImageView extends AppCompatImageView {

    /** The URL of the network image to load */
    private String mUrl;

    /**
     * BaseSdkResource ID of the image to be used as a placeholder until the network
     * image is loaded.
     */
    private int mDefaultImageId;

    /**
     * BaseSdkResource ID of the image to be used if the network response fails.
     */
    private int mErrorImageId;

    /** Local copy of the ImageLoader. */
    private ImageLoader mImageLoader;

    /** Current ImageContainer. (either in-flight or finished) */
    private ImageContainer mImageContainer;

    public NetworkImageView(@NonNull Context context)
    {
        this(context, null);
    }

    public NetworkImageView(@NonNull Context context, @Nullable AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public NetworkImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    int getErrorImageId() {
        return mErrorImageId;
    }

    int getDefaultImageId() {
        return mDefaultImageId;
    }

    /**
     * Sets URL of the image that should be loaded into this view. Note that
     * calling this will
     * immediately either set the cached image (if available) or the default
     * image specified by {@link NetworkImageView#setDefaultImageResId(int)} on
     * the view.
     * 
     * NOTE: If applicable, {@link NetworkImageView#setDefaultImageResId(int)}
     * and {@link NetworkImageView#setErrorImageResId(int)} should be called
     * prior to calling
     * this function.
     * 
     * @param url
     *            The URL that should be loaded into this ImageView.
     * @param imageLoader
     *            ImageLoader that will be used to make the request.
     */
    public void setImageUrl(@NonNull String url, @NonNull ImageLoader imageLoader)
    {
        mUrl = url;
        mImageLoader = imageLoader;
        // The URL has potentially changed. See if we need to load it.
        loadImageIfNecessary(false);
    }

    /**
     * Sets the default image resource ID to be used for this view until the
     * attempt to load it
     * completes.
     */
    public void setDefaultImageResId(int defaultImage)
    {
        mDefaultImageId = defaultImage;
    }

    /**
     * Sets the error image resource ID to be used for this view in the event
     * that the image
     * requested fails to load.
     */
    public void setErrorImageResId(int errorImage)
    {
        mErrorImageId = errorImage;
    }

    /**
     * Loads the image for the view if it isn't already loaded.
     * 
     * @param isInLayoutPass
     *            True if this was invoked from a layout pass, false otherwise.
     */
    void loadImageIfNecessary(final boolean isInLayoutPass)
    {
        int width = getWidth();
        int height = getHeight();
        ScaleType scaleType = getScaleType();

        boolean wrapWidth = false;
        boolean wrapHeight = false;
        if (getLayoutParams() != null) {
            wrapWidth = getLayoutParams().width == LayoutParams.WRAP_CONTENT;
            wrapHeight = getLayoutParams().height == LayoutParams.WRAP_CONTENT;
        }

        // if the view's bounds aren't known yet, and this is not a
        // wrap-content/wrap-content
        // view, hold off on loading the image.
        boolean isFullyWrapContent = wrapWidth && wrapHeight;
        if (width == 0 && height == 0 && !isFullyWrapContent) {
            return;
        }

        // if the URL to be loaded in this view is empty, cancel any old
        // requests and clear the
        // currently loaded image.
        if (TextUtils.isEmpty(mUrl)) {
            if (mImageContainer != null) {
                mImageContainer.cancelRequest();
                mImageContainer = null;
            }
            setDefaultImageOrNull();
            return;
        }

        // if there was an old request in this view, check if it needs to be
        // canceled.
        if (mImageContainer != null && mImageContainer.getRequestUrl() != null) {
            if (mImageContainer.getRequestUrl().equals(mUrl)) {
                // if the request is from the same URL, return.
                return;
            } else {
                // if there is a pre-existing request, cancel it if it's
                // fetching a different URL.
                mImageContainer.cancelRequest();
                setDefaultImageOrNull();
            }
        }

        // Calculate the max image width / height to use while ignoring
        // WRAP_CONTENT dimens.
        int maxWidth = wrapWidth ? 0 : width;
        int maxHeight = wrapHeight ? 0 : height;

        // The pre-existing content of this view didn't match the current URL.
        // Load the new image
        // from the network.
        ImageContainer newContainer = mImageLoader.get(mUrl, maxWidth, maxHeight, scaleType, new ImageListener() {

            @Override
            public void onErrorResponse(VolleyError error)
            {
                if (getErrorImageId() != 0) {
                    setImageResource(getErrorImageId());
                }
            }

            @Override
            public void onResponse(@NonNull final ImageContainer response, boolean isImmediate)
            {
                // If this was an immediate response that was delivered inside
                // of a layout
                // pass do not set the image immediately as it will trigger a
                // requestLayout
                // inside of a layout. Instead, defer setting the image by
                // posting back to
                // the main thread.
                if (isImmediate && isInLayoutPass) {
                    post(new Runnable() {

                        @Override
                        public void run()
                        {
                            onResponse(response, false);
                        }
                    });
                    return;
                }

                if (response.getBitmap() != null) {
                    setImageBitmap(response.getBitmap());
                } else if (getDefaultImageId() != 0) {
                    setImageResource(getDefaultImageId());
                }
            }
        });

        // update the ImageContainer to be the new bitmap container.
        mImageContainer = newContainer;
    }

    private void setDefaultImageOrNull()
    {
        if (mDefaultImageId != 0) {
            setImageResource(mDefaultImageId);
        } else {
            setImageBitmap(null);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        super.onLayout(changed, left, top, right, bottom);
        loadImageIfNecessary(true);
    }

    @Override
    protected void onDetachedFromWindow()
    {
        if (mImageContainer != null) {
            // If the view was bound to an image request, cancel it and clear
            // out the image from the view.
            mImageContainer.cancelRequest();
            setImageBitmap(null);
            // also clear out the container so we can reload the image if
            // necessary.
            mImageContainer = null;
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void drawableStateChanged()
    {
        super.drawableStateChanged();
        invalidate();
    }
}
