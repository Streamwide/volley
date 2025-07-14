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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streamwide.smartms.volley.api.Request;
import com.streamwide.smartms.volley.api.Response;
import com.streamwide.smartms.volley.api.VolleyError;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A Future that represents a Volley request.
 * 
 * Used by providing as your response and error listeners. For example:
 * 
 * <pre>
 * RequestFuture&lt;JSONObject&gt; future = RequestFuture.newFuture();
 * MyRequest request = new MyRequest(URL, future, future);
 * 
 * // If you want to be able to cancel the request:
 * future.setRequest(requestQueue.add(request));
 * 
 * // Otherwise:
 * requestQueue.add(request);
 * 
 * try {
 *     JSONObject response = future.get();
 *     // do something with response
 * } catch (InterruptedException e) {
 *     // handle the error
 * } catch (ExecutionException e) {
 *     // handle the error
 * }
 * </pre>
 * 
 * @param <T>
 *            The type of parsed response this future expects.
 */
public class RequestFuture<T> implements Future<T>, Response.Listener<T>, Response.ErrorListener {

    private Request<?> mRequest;
    private boolean mResultReceived = false;
    private T mResult;
    private VolleyError mException;

    @NonNull
    public static <E> RequestFuture<E> newFuture()
    {
        return new RequestFuture<>();
    }

    private RequestFuture()
    {
    }

    public void setRequest(@NonNull Request<?> request)
    {
        mRequest = request;
    }

    @Override
    public synchronized boolean cancel(boolean mayInterruptIfRunning)
    {
        if (mRequest == null) {
            return false;
        }

        if (!isDone()) {
            mRequest.cancel();
            return true;
        } else {
            return false;
        }
    }

    @Override
    @Nullable
    public T get() throws InterruptedException, ExecutionException
    {
        try {
            return doGet(null);
        } catch (TimeoutException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    @Nullable
    public T get(long timeout, @NonNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
    {
        return doGet(TimeUnit.MILLISECONDS.convert(timeout, unit));
    }

    private synchronized T doGet(Long timeoutMs) throws InterruptedException, ExecutionException, TimeoutException
    {
        if (mException != null) {
            throw new ExecutionException(mException);
        }

        if (mResultReceived) {
            return mResult;
        }

        if (timeoutMs == null) {
            wait(0);
        } else if (timeoutMs > 0) {
            wait(timeoutMs);
        }

        if (mException != null) {
            throw new ExecutionException(mException);
        }

        if (!mResultReceived) {
            throw new TimeoutException();
        }

        return mResult;
    }

    @Override
    public boolean isCancelled()
    {
        if (mRequest == null) {
            return false;
        }
        return mRequest.isCanceled();
    }

    @Override
    public synchronized boolean isDone()
    {
        return mResultReceived || mException != null || isCancelled();
    }

    @Override
    public synchronized void onResponse(@Nullable T response)
    {
        mResultReceived = true;
        mResult = response;
        notifyAll();
    }

    @Override
    public synchronized void onErrorResponse(@Nullable VolleyError error)
    {
        mException = error;
        notifyAll();
    }
}
