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

package com.streamwide.smartms.volley;

import android.net.TrafficStats;
import android.os.Process;
import android.os.SystemClock;

import androidx.annotation.NonNull;

import com.streamwide.smartms.volley.api.Cache;
import com.streamwide.smartms.volley.api.NetworkResponse;
import com.streamwide.smartms.volley.api.Request;
import com.streamwide.smartms.volley.api.Response;
import com.streamwide.smartms.volley.api.VolleyError;
import com.streamwide.smartms.volley.util.CollectionUtil;

import java.util.concurrent.BlockingQueue;

/**
 * Provides a thread for performing network dispatch from a queue of requests.
 * 
 * Requests added to the specified queue are processed from the network via a
 * specified {@link Network} interface. Responses are committed to cache, if
 * eligible, using a specified {@link Cache} interface. Valid responses and
 * errors are posted back to the caller via a {@link ResponseDelivery}.
 */
public class NetworkDispatcher extends Thread {

    private static final String CLASS_NAME = "NetworkDispatcher";
    /** The queue of requests to service. */
    private final BlockingQueue<Request<?>> mQueue;
    /** The network interface for processing requests. */
    private final Network mNetwork;
    /** For posting responses and errors. */
    private final ResponseDelivery mDelivery;
    /** Used for telling us to die. */
    private volatile boolean mQuit = false;

    /**
     * Creates a new network dispatcher thread. You must call {@link #start()}
     * in order to begin processing.
     *  @param queue
     *            Queue of incoming requests for triage
     * @param delivery
     * @param network
*            Network interface to use for performing requests
     */
    public NetworkDispatcher(@NonNull BlockingQueue<Request<?>> queue, @NonNull ResponseDelivery delivery, @NonNull Network network)
    {
        mQueue = CollectionUtil.copyBlockingQueue(queue);
        mNetwork = network;
        mDelivery = delivery;
    }

    /**
     * Forces this dispatcher to quit immediately. If any requests are still in
     * the queue, they are not guaranteed to be processed.
     */
    public void quit()
    {
        mQuit = true;
        interrupt();
    }

    private void addTrafficStatsTag(Request<?> request)
    {
        // Tag the request (if API >= 14)
        TrafficStats.setThreadStatsTag(request.getTrafficStatsTag());
    }

    @Override
    public void run()
    {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        while (true) {
            long startTimeMs = SystemClock.elapsedRealtime();
            Request<?> request;
            try {
                // Take a request from the queue.
                request = mQueue.take();
            } catch (InterruptedException e) {
                // We may have been interrupted because it was time to quit.
                if (mQuit) {
                    return;
                }
                Thread.currentThread().interrupt();
                continue;
            }

            try {
                request.addMarker("network-queue-take");

                // If the request was cancelled already, do not perform the
                // network request.
                if (request.isCanceled()) {
                    request.finish("network-discard-cancelled");
                    continue;
                }

                addTrafficStatsTag(request);

                // Perform the network request.
                NetworkResponse networkResponse = mNetwork.performRequest(request);
                request.addMarker("network-http-complete");

                // If the server returned 304 AND we delivered a response
                // already,
                // we're done -- don't deliver a second identical response.
                if (networkResponse.notModified && request.hasHadResponseDelivered()) {
                    request.finish("not-modified");
                    continue;
                }

                // Parse the response here on the worker thread.
                Response<?> response = request.parseNetworkResponse(networkResponse);
                request.addMarker("network-parse-complete");

                // Post the response back.
                request.markDelivered();
                mDelivery.postResponse(request, response);
            } catch (VolleyError volleyError) {
                volleyError.setNetworkTimeMs(SystemClock.elapsedRealtime() - startTimeMs);
                parseAndDeliverNetworkError(request, volleyError);
            } catch (Exception e) {
                VolleyLog.e(e, "Unhandled exception %s", e.toString());
                VolleyError volleyError = new VolleyError(e);
                volleyError.setNetworkTimeMs(SystemClock.elapsedRealtime() - startTimeMs);
                mDelivery.postError(request, volleyError);
            }
        }
    }

    private void parseAndDeliverNetworkError(Request<?> request, VolleyError error)
    {
        error = request.parseNetworkError(error);
        mDelivery.postError(request, error);
    }
}
