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

package com.streamwide.smartms.volley.toolbox;

import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streamwide.smartms.volley.AuthFailureError;
import com.streamwide.smartms.volley.Network;
import com.streamwide.smartms.volley.NetworkError;
import com.streamwide.smartms.volley.api.NetworkResponse;
import com.streamwide.smartms.volley.NoConnectionError;
import com.streamwide.smartms.volley.api.Request;
import com.streamwide.smartms.volley.RetryPolicy;
import com.streamwide.smartms.volley.ServerError;
import com.streamwide.smartms.volley.TimeoutError;
import com.streamwide.smartms.volley.api.VolleyError;
import com.streamwide.smartms.volley.VolleyLog;
import com.streamwide.smartms.volley.model.VolleyHttpEntity;
import com.streamwide.smartms.volley.model.VolleyHttpResponse;
import com.streamwide.smartms.volley.util.HttpResult;

import net.gotev.uploadservice.NameValue;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A network performing Volley requests over an {@link HttpStack}.
 */
public class BasicNetwork implements Network {

    private static final int SLOW_REQUEST_THRESHOLD_MS = 3000;

    private static final int DEFAULT_POOL_SIZE = 4096;

    protected final HttpStack mHttpStack;

    protected final ByteArrayPool mPool;

    /**
     * @param httpStack
     *            HTTP stack to be used
     */
    public BasicNetwork(@NonNull HttpStack httpStack)
    {
        // If a pool isn't passed in, then build a small default pool that will
        // give us a lot of
        // benefit and not use too much memory.
        this(new ByteArrayPool(DEFAULT_POOL_SIZE), httpStack);
    }

    /**
     * @param pool
     *            a buffer pool that improves GC performance in copy operations
     * @param httpStack
 *            HTTP stack to be used
     */
    public BasicNetwork(@NonNull ByteArrayPool pool, @NonNull HttpStack httpStack)
    {
        mHttpStack = httpStack;
        mPool = pool;
    }

    @Override
    @NonNull
    public NetworkResponse performRequest(@NonNull Request<?> request) throws VolleyError
    {
        long requestStart = SystemClock.elapsedRealtime();
        while (true) {
            VolleyHttpResponse httpResponse = null;
            byte[] responseContents = null;
            Map<String, String> responseHeaders = Collections.emptyMap();
            try {
                // Gather headers.
                Map<String, String> headers = new HashMap<>();
                httpResponse = mHttpStack.performRequest(request, headers);
                HttpResult httpResult = httpResponse.getHttpResult();
                int statusCode = httpResult.getResponseCode();

                responseHeaders = convertHeaders(httpResponse.getHeaders());
                // Handle cache validation.
                if (statusCode == HttpURLConnection.HTTP_NOT_MODIFIED) {

                    return new NetworkResponse(HttpURLConnection.HTTP_NOT_MODIFIED, null, responseHeaders, true,
                            SystemClock.elapsedRealtime() - requestStart);
                }

                // Some responses such as 204s do not have content. We must
                // check.
                if (httpResponse.getEntity() != null) {
                    responseContents = entityToBytes(httpResponse.getEntity());
                } else {
                    // Add 0 byte response as a way of honestly representing a
                    // no-content request.
                    responseContents = new byte[0];
                }

                // if the request is slow, log it.
                long requestLifetime = SystemClock.elapsedRealtime() - requestStart;
                logSlowRequests(requestLifetime, request, responseContents, statusCode);

                if (statusCode < 200 || statusCode > 299) {
                    throw new IOException();
                }
                return new NetworkResponse(statusCode, responseContents, responseHeaders, false,
                                SystemClock.elapsedRealtime() - requestStart);
            } catch (SocketTimeoutException e) {
                attemptRetryOnException("socket", request, new TimeoutError());
            } catch (MalformedURLException e) {
                throw new RuntimeException("Bad URL " + request.getUrl(), e);
            } catch (IOException e) {
                int statusCode = 0;
                NetworkResponse networkResponse = null;
                if (httpResponse != null) {
                    statusCode = httpResponse.getHttpResult().getResponseCode();
                } else {
                    throw new NoConnectionError(e);
                }

                VolleyLog.e(e, "Unexpected response code %d for %s", statusCode, request.getUrl());
                if (responseContents != null) {
                    networkResponse = new NetworkResponse(statusCode, responseContents, responseHeaders, false,
                                    SystemClock.elapsedRealtime() - requestStart);
                    if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED
                        || statusCode == HttpURLConnection.HTTP_FORBIDDEN) {
                        attemptRetryOnException("auth", request, new AuthFailureError(networkResponse));
                    } else {
                        // Only throw ServerError for 5xx status codes.
                        throw new ServerError(networkResponse);
                    }
                } else {
                    throw new NetworkError(networkResponse);
                }
            }
        }
    }

    /**
     * Logs requests that took over SLOW_REQUEST_THRESHOLD_MS to complete.
     */
    private void logSlowRequests(long requestLifetime, Request<?> request, byte[] responseContents, int statusCode)
    {
        if (requestLifetime > SLOW_REQUEST_THRESHOLD_MS) {
            VolleyLog.d("HTTP response for request=<%s> [lifetime=%d], [size=%s], " + "[rc=%d], [retryCount=%s]",
                            request, requestLifetime, responseContents != null ? responseContents.length : "null",
                            statusCode, request.getRetryPolicy().getCurrentRetryCount());
        }
    }

    /**
     * Attempts to prepare the request for a retry. If there are no more
     * attempts remaining in the
     * request's retry policy, a timeout exception is thrown.
     * 
     * @param request
     *            The request to use.
     */
    private static void attemptRetryOnException(String logPrefix, Request<?> request, VolleyError exception)
        throws VolleyError
    {
        RetryPolicy retryPolicy = request.getRetryPolicy();
        int oldTimeout = request.getTimeoutMs();

        try {
            retryPolicy.retry(exception);
        } catch (VolleyError e) {
            request.addMarker(logPrefix+"-timeout-giveup [timeout="+oldTimeout+"]");
            throw e;
        }
        request.addMarker(logPrefix+"-retry [timeout="+oldTimeout+"]");
    }

    protected void logError(@Nullable String what, @Nullable String url, long start)
    {
        long now = SystemClock.elapsedRealtime();
        VolleyLog.d("HTTP ERROR(%s) %d ms to fetch %s", what, (now - start), url);
    }

    /** Reads the contents of VolleyHttpEntity into a byte[]. */
    private byte[] entityToBytes(VolleyHttpEntity entity) throws IOException, ServerError
    {
        PoolingByteArrayOutputStream bytes = new PoolingByteArrayOutputStream(mPool, (int) entity.getContentLength());
        byte[] buffer = null;
        try {
            InputStream in = entity.getContent();
            if (in == null) {
                throw new ServerError();
            }
            buffer = mPool.getBuf(1024);
            int count;
            while ((count = in.read(buffer)) != -1) {
                bytes.write(buffer, 0, count);
            }
            return bytes.toByteArray();
        } finally {
            mPool.returnBuf(buffer);
            bytes.close();
        }
    }

    /**
     * Converts Headers[] to Map<String, String>.
     */
    @NonNull
    protected static Map<String, String> convertHeaders(@NonNull List<NameValue> headers)
    {
        Map<String, String> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 0; i < headers.size(); i++) {
            result.put(headers.get(i).getName(), headers.get(i).getValue());
        }
        return result;
    }
}
