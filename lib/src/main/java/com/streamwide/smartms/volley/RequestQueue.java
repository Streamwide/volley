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

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import com.streamwide.smartms.volley.api.Request;
/**
 * A request dispatch queue with a thread pool of dispatchers.
 * 
 * Calling {@link #add(Request)} will enqueue the given Request for dispatch,
 * resolving from either cache or network on a worker thread, and then
 * delivering
 * a parsed response on the main thread.
 */
public class RequestQueue {

    /** Callback interface for completed requests. */
    public static interface RequestFinishedListener<T> {

        /** Called when a request has finished processing. */
        public void onRequestFinished(@NonNull Request<T> request);
    }

    /**
     * Used for generating monotonically-increasing sequence numbers for
     * requests.
     */
    private AtomicInteger mSequenceGenerator = new AtomicInteger();


    /**
     * The set of all requests currently being processed by this RequestQueue. A
     * Request
     * will be in this set if it is waiting in any queue or currently being
     * processed by
     * any dispatcher.
     */
    private final Set<Request<?>> mCurrentRequests = new HashSet<>();


    /** The queue of requests that are actually going out to the network. */
    private final PriorityBlockingQueue<Request<?>> mNetworkQueue = new PriorityBlockingQueue<>();

    /** Number of network request dispatcher threads to start. */
    private static final int DEFAULT_NETWORK_THREAD_POOL_SIZE = 4;

    /** Network interface for performing requests. */
    private final Network mNetwork;

    /** Response delivery mechanism. */
    private final ResponseDelivery mDelivery;

    /** The network dispatchers. */
    private NetworkDispatcher[] mDispatchers;

    private List<RequestFinishedListener> mFinishedListeners = new ArrayList<>();

    /**
     * Creates the worker pool. Processing will not begin until {@link #start()}
     * is called.
     * @param threadPoolSize
     *            Number of network dispatcher threads to create
     * @param delivery
     * @param network
*            A Network interface for performing HTTP requests
     */
    public RequestQueue(int threadPoolSize, @NonNull ResponseDelivery delivery, @NonNull Network network)
    {
        mNetwork = network;
        mDispatchers = new NetworkDispatcher[threadPoolSize];
        mDelivery = delivery;
    }

    /**
     * Creates the worker pool. Processing will not begin until {@link #start()}
     * is called.

     * @param threadPoolSize
     * @param network
 *            A Network interface for performing HTTP requests
     */
    public RequestQueue(int threadPoolSize, @NonNull Network network)
    {
        this(threadPoolSize, new ExecutorDelivery(new Handler(Looper.getMainLooper())), network);
    }

    /**
     * Creates the worker pool. Processing will not begin until {@link #start()}
     * is called.
     *
     * @param network
     *            A Network interface for performing HTTP requests
     */
    public RequestQueue(@NonNull Network network)
    {
        this(DEFAULT_NETWORK_THREAD_POOL_SIZE, network);
    }

    /**
     * Starts the dispatchers in this queue.
     */
    public void start()
    {
        stop(); // Make sure any currently running dispatchers are stopped.

        // Create network dispatchers (and corresponding threads) up to the pool
        // size.
        for (int i = 0; i < mDispatchers.length; i++) {
            NetworkDispatcher networkDispatcher = new NetworkDispatcher(mNetworkQueue, mDelivery, mNetwork);
            mDispatchers[i] = networkDispatcher;
            networkDispatcher.start();
        }
    }

    /**
     * Stops the cache and network dispatchers.
     */
    public void stop()
    {
        for (int i = 0; i < mDispatchers.length; i++) {
            if (mDispatchers[i] != null) {
                mDispatchers[i].quit();
            }
        }
    }

    /**
     * Gets a sequence number.
     */
    public int getSequenceNumber()
    {
        return mSequenceGenerator.incrementAndGet();
    }


    /**
     * A simple predicate or filter interface for Requests, for use by
     * {@link RequestQueue#cancelAll(RequestFilter)}.
     */
    public interface RequestFilter {

        public boolean apply(@NonNull Request<?> request);
    }

    /**
     * Cancels all requests in this queue for which the given filter applies.
     * 
     * @param filter
     *            The filtering function to use
     */
    public void cancelAll(@NonNull RequestFilter filter)
    {
        synchronized (mCurrentRequests) {
            for (Request<?> request : mCurrentRequests) {
                if (filter.apply(request)) {
                    request.cancel();
                }
            }
        }
    }

    /**
     * Cancels all requests in this queue with the given tag. Tag must be
     * non-null
     * and equality is by identity.
     */
    public void cancelAll(@Nullable final Object tag)
    {
        if (tag == null) {
            throw new IllegalArgumentException("Cannot cancelAll with a null tag");
        }
        cancelAll(new RequestFilter() {

            @Override
            public boolean apply(@NonNull Request<?> request)
            {
                return request.getTag() == tag;
            }
        });
    }

    /**
     * Adds a Request to the dispatch queue.
     * 
     * @param request
     *            The request to service
     * @return The passed-in request
     */
    @NonNull
    public <T> Request<T> add(@NonNull Request<T> request)
    {
        // Tag the request as belonging to this queue and add it to the set of
        // current requests.
        request.setRequestQueue(this);
        synchronized (mCurrentRequests) {
            mCurrentRequests.add(request);
        }

        // Process requests in the order they are added.
        request.setSequence(getSequenceNumber());
        request.addMarker("add-to-queue");

        mNetworkQueue.add(request);
        return request;

    }

    /**
     * Called from {@link Request#finish(String)}, indicating that processing of
     * the given request
     * has finished.
     * 
     * <p>
     * Releases waiting requests for <code>request.getCacheKey()</code> if
     * <code>request.isShouldCache()</code>.
     * </p>
     */
    public <T> void finish(Request<T> request)
    {
        // Remove from the set of requests currently being processed.
        synchronized (mCurrentRequests) {
            mCurrentRequests.remove(request);
        }
        synchronized (mFinishedListeners) {
            for (RequestFinishedListener<T> listener : mFinishedListeners) {
                listener.onRequestFinished(request);
            }
        }
    }

    public <T> void addRequestFinishedListener(@NonNull RequestFinishedListener<T> listener)
    {
        synchronized (mFinishedListeners) {
            mFinishedListeners.add(listener);
        }
    }

    /**
     * Remove a RequestFinishedListener. Has no effect if listener was not
     * previously added.
     */
    public <T> void removeRequestFinishedListener(@NonNull RequestFinishedListener<T> listener)
    {
        synchronized (mFinishedListeners) {
            mFinishedListeners.remove(listener);
        }
    }
}
