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

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streamwide.smartms.volley.api.Request;
import com.streamwide.smartms.volley.RequestQueue;
import com.streamwide.smartms.volley.api.Response.ErrorListener;
import com.streamwide.smartms.volley.api.Response.Listener;
import com.streamwide.smartms.volley.api.ImageRequest;
import com.streamwide.smartms.volley.api.VolleyError;
import com.streamwide.smartms.volley.util.CollectionUtil;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Helper that handles loading and caching images from remote URLs.
 * 
 * The simple way to use this class is to call
 * {@link ImageLoader#get(String, ImageListener)} and to pass in the default
 * image listener provided by
 * {@link ImageLoader#getImageListener(ImageView, int, int)}. Note that all
 * function calls to
 * this class must be made from the main thead, and all responses will be
 * delivered to the main
 * thread as well.
 */
public class ImageLoader {

    /** RequestQueue for dispatching ImageRequests onto. */
    private final RequestQueue mRequestQueue;

    /**
     * Amount of time to wait after first response arrives before delivering all
     * responses.
     */
    private int mBatchResponseDelayMs = 100;

    /**
     * The cache implementation to be used as an L1 cache before calling into
     * volley.
     */
    private final ImageCache mCache;

    /**
     * HashMap of Cache keys -> BatchedImageRequest used to track in-flight
     * requests so
     * that we can coalesce multiple requests to the same URL into a single
     * network request.
     */
    private final HashMap<String, BatchedImageRequest> mInFlightRequests = new HashMap<>();

    /** HashMap of the currently pending responses (waiting to be delivered). */
    private final HashMap<String, BatchedImageRequest> mBatchedResponses = new HashMap<>();

    /** Handler to the main thread. */
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    /** Runnable for in-flight response delivery. */
    private Runnable mRunnable;

    /**
     * Simple cache adapter interface. If provided to the ImageLoader, it
     * will be used as an L1 cache before dispatch to Volley. Implementations
     * must not block. Implementation with an LruCache is recommended.
     */
    public interface ImageCache {

        @Nullable
        public Bitmap getBitmap(@NonNull String url);

        public void putBitmap(@NonNull String url, @NonNull Bitmap bitmap);
    }

    /**
     * Constructs a new ImageLoader.
     * 
     * @param queue
     *            The RequestQueue to use for making image requests.
     * @param imageCache
     *            The cache to use as an L1 cache.
     */
    public ImageLoader(@NonNull RequestQueue queue, @NonNull ImageCache imageCache)
    {
        mRequestQueue = queue;
        mCache = imageCache;
    }

    /**
     * The default implementation of ImageListener which handles basic
     * functionality
     * of showing a default image until the network response is received, at
     * which point
     * it will switch to either the actual image or the error image.
     * 
     * @param view
     *            The imageView that the listener is associated with.
     * @param defaultImageResId
     *            Default image resource ID to use, or 0 if it doesn't exist.
     * @param errorImageResId
     *            Error image resource ID to use, or 0 if it doesn't exist.
     */
    @NonNull
    public static ImageListener getImageListener(@NonNull final ImageView view, final int defaultImageResId,
                                                 final int errorImageResId)
    {
        return new ImageListener() {

            @Override
            public void onErrorResponse(VolleyError error)
            {
                if (errorImageResId != 0) {
                    view.setImageResource(errorImageResId);
                }
            }

            @Override
            public void onResponse(@NonNull ImageContainer response, boolean isImmediate)
            {
                if (response.getBitmap() != null) {
                    view.setImageBitmap(response.getBitmap());
                } else if (defaultImageResId != 0) {
                    view.setImageResource(defaultImageResId);
                }
            }
        };
    }

    /**
     * Interface for the response handlers on image requests.
     * 
     * The call flow is this:
     * 1. Upon being attached to a request, onResponse(response, true) will
     * be invoked to reflect any cached data that was already available. If the
     * data was available, response.getBitmap() will be non-null.
     * 
     * 2. After a network response returns, only one of the following cases will
     * happen:
     * - onResponse(response, false) will be called if the image was loaded.
     * or
     * - onErrorResponse will be called if there was an error loading the image.
     */
    public interface ImageListener extends ErrorListener {

        /**
         * Listens for non-error changes to the loading of the image request.
         * 
         * @param response
         *            Holds all information pertaining to the request, as well
         *            as the bitmap (if it is loaded).
         * @param isImmediate
         *            True if this was called during ImageLoader.get() variants.
         *            This can be used to differentiate between a cached image
         *            loading and a network
         *            image loading in order to, for example, run an animation
         *            to fade in network loaded
         *            images.
         */
        public void onResponse(@NonNull ImageContainer response, boolean isImmediate);
    }

    /**
     * Checks if the item is available in the cache.
     * 
     * @param requestUrl
     *            The url of the remote image
     * @param maxWidth
     *            The maximum width of the returned image.
     * @param maxHeight
     *            The maximum height of the returned image.
     * @return True if the item exists in cache, false otherwise.
     */
    public boolean isCached(@NonNull String requestUrl, int maxWidth, int maxHeight)
    {
        return isCached(requestUrl, maxWidth, maxHeight, ScaleType.CENTER_INSIDE);
    }

    /**
     * Checks if the item is available in the cache.
     * 
     * @param requestUrl
     *            The url of the remote image
     * @param maxWidth
     *            The maximum width of the returned image.
     * @param maxHeight
     *            The maximum height of the returned image.
     * @param scaleType
     *            The scaleType of the imageView.
     * @return True if the item exists in cache, false otherwise.
     */
    public boolean isCached(@NonNull String requestUrl, int maxWidth, int maxHeight, @NonNull ScaleType scaleType)
    {
        throwIfNotOnMainThread();

        String cacheKey = getCacheKey(requestUrl, maxWidth, maxHeight, scaleType);
        return mCache.getBitmap(cacheKey) != null;
    }

    /**
     * Returns an ImageContainer for the requested URL.
     * 
     * The ImageContainer will contain either the specified default bitmap or
     * the loaded bitmap.
     * If the default was returned, the {@link ImageLoader} will be invoked when
     * the
     * request is fulfilled.
     * 
     * @param requestUrl
     *            The URL of the image to be loaded.
     */
    @NonNull
    public ImageContainer get(@NonNull String requestUrl, @NonNull final ImageListener listener)
    {
        return get(requestUrl, 0, 0, listener);
    }

    /**
     * Equivalent to calling
     * {@link #get(String, int, int, ScaleType, ImageListener)} with
     * {@code Scaletype == ScaleType.CENTER_INSIDE}.
     */
    @NonNull
    public ImageContainer get(@NonNull String requestUrl, int maxWidth, int maxHeight, @NonNull ImageListener imageListener)
    {
        return get(requestUrl, maxWidth, maxHeight, ScaleType.CENTER_INSIDE, imageListener);
    }

    /**
     * Issues a bitmap request with the given URL if that image is not available
     * in the cache, and returns a bitmap container that contains all of the
     * data
     * relating to the request (as well as the default image if the requested
     * image is not available).
     * 
     * @param requestUrl
     *            The url of the remote image
     * @param maxWidth
     *            The maximum width of the returned image.
     * @param maxHeight
     *            The maximum height of the returned image.
     * @param scaleType
     *            The ImageViews ScaleType used to calculate the needed image
     *            size.
     * @param imageListener
     *            The listener to call when the remote image is loaded
     * @return A container object that contains all of the properties of the
     *         request, as well as
     *         the currently available image (default if remote is not loaded).
     */
    @NonNull
    public ImageContainer get(@NonNull String requestUrl, int maxWidth, int maxHeight, @NonNull ScaleType scaleType, @NonNull ImageListener imageListener)
    {

        // only fulfill requests that were initiated from the main thread.
        throwIfNotOnMainThread();

        final String cacheKey = getCacheKey(requestUrl, maxWidth, maxHeight, scaleType);

        // Try to look up the request in the cache of remote images.
        Bitmap cachedBitmap = mCache.getBitmap(cacheKey);
        if (cachedBitmap != null) {
            // Return the cached bitmap.
            ImageContainer container = new ImageContainer(cachedBitmap, requestUrl, null, null);
            imageListener.onResponse(container, true);
            return container;
        }

        // The bitmap did not exist in the cache, fetch it!
        ImageContainer imageContainer = new ImageContainer(null, requestUrl, cacheKey, imageListener);

        // Update the caller to let them know that they should use the default
        // bitmap.
        imageListener.onResponse(imageContainer, true);

        // Check to see if a request is already in-flight.
        BatchedImageRequest request = mInFlightRequests.get(cacheKey);
        if (request != null) {
            // If it is, add this request to the list of listeners.
            request.addContainer(imageContainer);
            return imageContainer;
        }

        // The request is not already in flight. Send the new request to the
        // network and
        // track it.
        Request<Bitmap> newRequest = makeImageRequest(requestUrl, maxWidth, maxHeight, scaleType, cacheKey);

        mRequestQueue.add(newRequest);
        mInFlightRequests.put(cacheKey, new BatchedImageRequest(newRequest, imageContainer));
        return imageContainer;
    }

    @NonNull
    protected Request<Bitmap> makeImageRequest(@NonNull String requestUrl, int maxWidth, int maxHeight, @NonNull ScaleType scaleType,
                                               @NonNull final String cacheKey)
    {
        return new ImageRequest(requestUrl, new Listener<Bitmap>() {

            @Override
            public void onResponse(Bitmap response)
            {
                onGetImageSuccess(cacheKey, response);
            }
        }, maxWidth, maxHeight, scaleType, Config.RGB_565, new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error)
            {
                onGetImageError(cacheKey, error);
            }
        });
    }

    /**
     * Sets the amount of time to wait after the first response arrives before
     * delivering all
     * responses. Batching can be disabled entirely by passing in 0.
     * 
     * @param newBatchedResponseDelayMs
     *            The time in milliseconds to wait.
     */
    public void setBatchedResponseDelay(int newBatchedResponseDelayMs)
    {
        mBatchResponseDelayMs = newBatchedResponseDelayMs;
    }

    /**
     * Handler for when an image was successfully loaded.
     * 
     * @param cacheKey
     *            The cache key that is associated with the image request.
     * @param response
     *            The bitmap that was returned from the network.
     */
    protected void onGetImageSuccess(@NonNull String cacheKey, @NonNull Bitmap response)
    {
        // cache the image that was fetched.
        mCache.putBitmap(cacheKey, response);

        // remove the request from the list of in-flight requests.
        BatchedImageRequest request = mInFlightRequests.remove(cacheKey);

        if (request != null) {
            // Update the response bitmap.
            request.setResponseBitmap(response);

            // Send the batched response
            batchResponse(cacheKey, request);
        }
    }

    /**
     * Handler for when an image failed to load.
     * 
     * @param cacheKey
     *            The cache key that is associated with the image request.
     */
    protected void onGetImageError(@NonNull String cacheKey, @NonNull VolleyError error)
    {
        // Notify the requesters that something failed via a null result.
        // Remove this request from the list of in-flight requests.
        BatchedImageRequest request = mInFlightRequests.remove(cacheKey);

        if (request != null) {
            // Set the error for this request
            request.setError(error);

            // Send the batched response
            batchResponse(cacheKey, request);
        }
    }

    /**
     * Container object for all of the data surrounding an image request.
     */
    public class ImageContainer {

        /**
         * The most relevant bitmap for the container. If the image was in
         * cache, the
         * Holder to use for the final bitmap (the one that pairs to the
         * requested URL).
         */
        private Bitmap mBitmap;

        private final ImageListener mListener;

        /** The cache key that was associated with the request */
        private final String mCacheKey;

        /** The request URL that was specified */
        private final String mRequestUrl;

        /**
         * Constructs a BitmapContainer object.
         * 
         * @param bitmap
         *            The final bitmap (if it exists).
         * @param requestUrl
         *            The requested URL for this container.
         * @param cacheKey
         *            The cache key that identifies the requested URL for this
         *            container.
         */
        public ImageContainer(@Nullable Bitmap bitmap, @NonNull String requestUrl, @Nullable String cacheKey, @Nullable ImageListener listener)
        {
            mBitmap = bitmap;
            mRequestUrl = requestUrl;
            mCacheKey = cacheKey;
            mListener = listener;
        }

        /**
         * Releases interest in the in-flight request (and cancels it if no one
         * else is listening).
         */
        public void cancelRequest()
        {
            if (mListener == null) {
                return;
            }

            BatchedImageRequest request = getInFlightRequests().get(mCacheKey);
            if (request != null) {
                boolean canceled = request.removeContainerAndCancelIfNecessary(this);
                if (canceled) {
                    getInFlightRequests().remove(mCacheKey);
                }
            } else {
                // check to see if it is already batched for delivery.
                request = getBatchedResponses().get(mCacheKey);
                if (request != null) {
                    request.removeContainerAndCancelIfNecessary(this);
                    if (request.getContainers().isEmpty()) {
                        getBatchedResponses().remove(mCacheKey);
                    }
                }
            }
        }

        /**
         * Returns the bitmap associated with the request URL if it has been
         * loaded, null otherwise.
         */
        @Nullable
        public Bitmap getBitmap()
        {
            return mBitmap;
        }

        void setBitmap(Bitmap mBitmap) {
            this.mBitmap = mBitmap;
        }

        /**
         * Returns the requested URL for this container.
         */
        @Nullable
        public String getRequestUrl()
        {
            return mRequestUrl;
        }

        ImageListener getListener() {
            return mListener;
        }
    }

    /**
     * Wrapper class used to map a Request to the set of active ImageContainer
     * objects that are
     * interested in its results.
     */
    private class BatchedImageRequest {

        /** The request being tracked */
        private final Request<?> mRequest;

        /** The result of the request being tracked by this item */
        private Bitmap mResponseBitmap;

        /** Error if one occurred for this response */
        private VolleyError mError;

        /**
         * List of all of the active ImageContainers that are interested in the
         * request
         */
        private final LinkedList<ImageContainer> mContainers = new LinkedList<>();

        /**
         * Constructs a new BatchedImageRequest object
         * 
         * @param request
         *            The request being tracked
         * @param container
         *            The ImageContainer of the person who initiated the
         *            request.
         */
        public BatchedImageRequest(Request<?> request, ImageContainer container)
        {
            mRequest = request;
            mContainers.add(container);
        }

        /**
         * Set the error for this response
         */
        public void setError(VolleyError error)
        {
            mError = error;
        }

        /**
         * Get the error for this response
         */
        public VolleyError getError()
        {
            return mError;
        }

        /**
         * Adds another ImageContainer to the list of those interested in the
         * results of
         * the request.
         */
        public void addContainer(ImageContainer container)
        {
            mContainers.add(container);
        }

        /**
         * Detatches the bitmap container from the request and cancels the
         * request if no one is
         * left listening.
         * 
         * @param container
         *            The container to remove from the list
         * @return True if the request was canceled, false otherwise.
         */
        public boolean removeContainerAndCancelIfNecessary(ImageContainer container)
        {
            mContainers.remove(container);
            if (mContainers.isEmpty()) {
                mRequest.cancel();
                return true;
            }
            return false;
        }

        Bitmap getResponseBitmap() {
            return mResponseBitmap;
        }

        void setResponseBitmap(Bitmap mResponseBitmap) {
            this.mResponseBitmap = mResponseBitmap;
        }

        LinkedList<ImageContainer> getContainers() {
            return CollectionUtil.copyLinkedList(mContainers);
        }
    }

    /**
     * Starts the runnable for batched delivery of responses if it is not
     * already started.
     * 
     * @param cacheKey
     *            The cacheKey of the response being delivered.
     * @param request
     *            The BatchedImageRequest to be delivered.
     */
    private void batchResponse(String cacheKey, BatchedImageRequest request)
    {
        mBatchedResponses.put(cacheKey, request);
        // If we don't already have a batch delivery runnable in flight, make a
        // new one.
        // Note that this will be used to deliver responses to all callers in
        // mBatchedResponses.
        if (mRunnable == null) {
            mRunnable = new Runnable() {

                @Override
                public void run()
                {
                    for (BatchedImageRequest bir : getBatchedResponses().values()) {
                        for (ImageContainer container : bir.getContainers()) {
                            // If one of the callers in the batched request
                            // canceled the request
                            // after the response was received but before it was
                            // delivered,
                            // skip them.
                            if (container.getListener() == null) {
                                continue;
                            }
                            if (bir.getError() == null) {
                                container.setBitmap(bir.getResponseBitmap());
                                container.getListener().onResponse(container, false);
                            } else {
                                container.getListener().onErrorResponse(bir.getError());
                            }
                        }
                    }
                    getBatchedResponses().clear();
                    setRunnable(null);
                }

            };
            // Post the runnable.
            mHandler.postDelayed(mRunnable, mBatchResponseDelayMs);
        }
    }

    private void throwIfNotOnMainThread()
    {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("ImageLoader must be invoked from the main thread.");
        }
    }

    /**
     * Creates a cache key for use with the L1 cache.
     * 
     * @param url
     *            The URL of the request.
     * @param maxWidth
     *            The max-width of the output.
     * @param maxHeight
     *            The max-height of the output.
     * @param scaleType
     *            The scaleType of the imageView.
     */
    private static String getCacheKey(String url, int maxWidth, int maxHeight, ScaleType scaleType)
    {
        return new StringBuilder(url.length() + 12).append("#W").append(maxWidth).append("#H").append(maxHeight)
                        .append("#S").append(scaleType.ordinal()).append(url).toString();
    }

    HashMap<String, BatchedImageRequest> getInFlightRequests() {
        return mInFlightRequests;
    }

    HashMap<String, BatchedImageRequest> getBatchedResponses() {
        return mBatchedResponses;
    }

    void setRunnable(Runnable runnable) {
        this.mRunnable = runnable;
    }
}
