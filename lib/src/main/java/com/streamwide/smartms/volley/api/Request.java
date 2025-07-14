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

import android.net.TrafficStats;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streamwide.smartms.volley.AuthFailureError;
import com.streamwide.smartms.volley.BuildConfig;
import com.streamwide.smartms.volley.DefaultRetryPolicy;
import com.streamwide.smartms.volley.RequestQueue;
import com.streamwide.smartms.volley.RetryPolicy;
import com.streamwide.smartms.volley.TimeoutError;
import com.streamwide.smartms.volley.VolleyLog;
import com.streamwide.smartms.volley.toolbox.HurlStack;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;

/**
 * Base class for all network requests.
 * 
 * @param <T>
 *            The type of parsed response this request expects.
 */
public abstract class Request<T> implements Comparable<Request<T>> {

    /**
     * Default encoding for POST or PUT parameters. See
     * {@link #getParamsEncoding()}.
     */
    private static final String DEFAULT_PARAMS_ENCODING = "UTF-8";

    /** An event log tracing the lifetime of this request; for debugging. */
    private final VolleyLog.MarkerLog mEventLog = false ? new VolleyLog.MarkerLog() : null;

    /**
     * Request method of this request. Currently supports GET, POST, PUT,
     * DELETE, HEAD, OPTIONS,
     * TRACE, and PATCH.
     */
    private final String mMethod;

    /** URL of this request. */
    private final String mUrl;

    /** Default tag for {@link TrafficStats}. */
    private final int mDefaultTrafficStatsTag;

    /** Listener interface for errors. */
    private final Response.ErrorListener mErrorListener;

    /** Sequence number of this request, used to enforce FIFO ordering. */
    private Integer mSequence;

    /** The request queue this request is associated with. */
    private RequestQueue mRequestQueue;


    /** Whether or not this request has been canceled. */
    private boolean mCanceled = false;

    /** Whether or not a response has been delivered for this request yet. */
    private boolean mResponseDelivered = false;

    // A cheap variant of request tracing used to dump slow requests.
    private long mRequestBirthTime = 0;

    // if true use SW certif for initializing tls connection, false ignore cert in
    // case of https connection
    private boolean mUseTls = false;

    // if true use this request is part of SmartMs API
    private boolean mInternalApi = false;

    /**
     * Threshold at which we should log the request (even when debug logging is
     * not enabled).
     */
    private static final long SLOW_REQUEST_THRESHOLD_MS = 3000;

    /** The retry policy for this request. */
    private RetryPolicy mRetryPolicy;


    /** An opaque token tagging this request; used for bulk cancellation. */
    private Object mTag;

    /**
     * Creates a new request with the given URL and error listener. Note that
     * the normal response listener is not provided here as delivery of
     * responses
     * is provided by subclasses, who have a better idea of how to deliver an
     * already-parsed response.
     * 
     * @deprecated Use
     *             {@link #Request(String, String, Response.ErrorListener)}
     *             .
     */
    @Deprecated
    public Request(@Nullable String url, @Nullable Response.ErrorListener listener)
    {
        this(HurlStack.HttpMethod.GET, url, listener);
    }

    /**
     * Creates a new request with the given method (one of the values from
     * URL, and error listener. Note that the normal response listener is not
     * provided here as
     * delivery of responses is provided by subclasses, who have a better idea
     * of how to deliver
     * an already-parsed response.
     */
    public Request(@NonNull String method, @Nullable String url, @Nullable Response.ErrorListener listener)
    {
        mMethod = method;
        mUrl = url;
        mErrorListener = listener;
        setRetryPolicy(new DefaultRetryPolicy());

        mDefaultTrafficStatsTag = findDefaultTrafficStatsTag(url);
    }

    /**
     * Return the method for this request. Can be one of the values in
     */
    @NonNull
    public String getMethod()
    {
        return mMethod;
    }

    /**
     * Set a tag on this request. Can be used to cancel all requests with this
     * tag by {@link RequestQueue#cancelAll(Object)}.
     *
     * @return This Request object to allow for chaining.
     */
    @NonNull
    public Request<?> setTag(@NonNull Object tag)
    {
        mTag = tag;
        return this;
    }

    /**
     * Returns this request's tag.
     *
     * @see Request#setTag(Object)
     */
    @Nullable
    public Object getTag()
    {
        return mTag;
    }

    /**
     * @return this request's
     *         {@link Response.ErrorListener}.
     */
    @Nullable
    public Response.ErrorListener getErrorListener()
    {
        return mErrorListener;
    }

    /**
     * @return A tag for use with {@link TrafficStats#setThreadStatsTag(int)}
     */
    public int getTrafficStatsTag()
    {
        return mDefaultTrafficStatsTag;
    }

    VolleyLog.MarkerLog getmEventLog() {
        return mEventLog;
    }

    /**
     * @return The hashcode of the URL's host component, or 0 if there is none.
     */
    private static int findDefaultTrafficStatsTag(String url)
    {
        if (!TextUtils.isEmpty(url)) {
            Uri uri = Uri.parse(url);
            if (uri != null) {
                String host = uri.getHost();
                if (host != null) {
                    return host.hashCode();
                }
            }
        }
        return 0;
    }

    /**
     * Sets the retry policy for this request.
     * 
     * @return This Request object to allow for chaining.
     */
    @NonNull
    public Request<?> setRetryPolicy(@NonNull RetryPolicy retryPolicy)
    {
        mRetryPolicy = retryPolicy;
        return this;
    }

    /**
     * Adds an event to this request's event log; for debugging.
     */
    public void addMarker(@NonNull String tag)
    {
        if (BuildConfig.DEBUG) {
            if (mEventLog == null) return;
            mEventLog.add(tag, Thread.currentThread().getId());
        } else if (mRequestBirthTime == 0) {
            mRequestBirthTime = SystemClock.elapsedRealtime();
        }
    }

    /**
     * Notifies the request queue that this request has finished (successfully
     * or with error).
     * 
     * <p>
     * Also dumps all events from this request's event log; for debugging.
     * </p>
     */
    public void finish(final String tag)
    {
        if (mRequestQueue != null) {
            mRequestQueue.finish(this);
        }
        if (BuildConfig.DEBUG) {
            final long threadId = Thread.currentThread().getId();
            if (Looper.myLooper() != Looper.getMainLooper()) {
                // If we finish marking off of the main thread, we need to
                // actually do it on the main thread to ensure correct ordering.
                Handler mainThread = new Handler(Looper.getMainLooper());
                mainThread.post(new Runnable() {

                    @Override
                    public void run()
                    {
                        if ( getmEventLog() == null) return;
                        getmEventLog().add(tag, threadId);
                        getmEventLog().finish(this.toString());
                    }
                });
                return;
            }

            if (mEventLog != null) {
                mEventLog.add(tag, threadId);
                mEventLog.finish(this.toString());
            }

        } else {
            long requestTime = SystemClock.elapsedRealtime() - mRequestBirthTime;
            if (requestTime >= SLOW_REQUEST_THRESHOLD_MS) {
                VolleyLog.d("%d ms: %s", requestTime, this.toString());
            }
        }
    }

    /**
     * Associates this request with the given queue. The request queue will be
     * notified when this
     * request has finished.
     * 
     * @return This Request object to allow for chaining.
     */
    @NonNull
    public Request<?> setRequestQueue(@NonNull RequestQueue requestQueue)
    {
        mRequestQueue = requestQueue;
        return this;
    }

    /**
     * Sets the sequence number of this request. Used by {@link RequestQueue}.
     * 
     * @return This Request object to allow for chaining.
     */
    @NonNull
    public final Request<?> setSequence(int sequence)
    {
        mSequence = sequence;
        return this;
    }

    /**
     * Returns the sequence number of this request.
     */
    public final int getSequence()
    {
        if (mSequence == null) {
            throw new IllegalStateException("getSequence called before setSequence");
        }
        return mSequence;
    }

    /**
     * Returns the URL of this request.
     */
    @Nullable
    public String getUrl()
    {
        return mUrl;
    }

    /**
     * Mark this request as canceled. No callback will be delivered.
     */
    public void cancel()
    {
        mCanceled = true;
    }

    /**
     * Returns true if this request has been canceled.
     */
    public boolean isCanceled()
    {
        return mCanceled;
    }

    /**
     * Returns a list of extra HTTP headers to go along with this request. Can
     * throw {@link AuthFailureError} as authentication may be required to
     * provide these values.
     * 
     * @throws AuthFailureError
     *             In the event of auth failure
     */
    @NonNull
    public Map<String, String> getHeaders() throws AuthFailureError
    {
        return Collections.emptyMap();
    }

    /**
     * Returns a Map of POST parameters to be used for this request, or null if
     * a simple GET should be used. Can throw {@link AuthFailureError} as
     * authentication may be required to provide these values.
     * 
     * <p>
     * Note that only one of getPostParams() and getPostBody() can return a
     * non-null value.
     * </p>
     * 
     * @throws AuthFailureError
     *             In the event of auth failure
     * 
     * @deprecated Use {@link #getParams()} instead.
     */
    @Deprecated
    @Nullable
    protected Map<String, String> getPostParams() throws AuthFailureError
    {
        return getParams();
    }

    /**
     * Returns which encoding should be used when converting POST parameters
     * returned by {@link #getPostParams()} into a raw POST body.
     * 
     * <p>
     * This controls both encodings:
     * <ol>
     * <li>The string encoding used when converting parameter names and values
     * into bytes prior to URL encoding them.</li>
     * <li>The string encoding used when converting the URL encoded parameters
     * into a raw byte array.</li>
     * </ol>
     * 
     * @deprecated Use {@link #getParamsEncoding()} instead.
     */
    @Deprecated
    @NonNull
    protected String getPostParamsEncoding()
    {
        return getParamsEncoding();
    }

    /**
     * @deprecated Use {@link #getBodyContentType()} instead.
     */
    @Deprecated
    @NonNull
    public String getPostBodyContentType()
    {
        return getBodyContentType();
    }

    /**
     * Returns the raw POST body to be sent.
     * 
     * @throws AuthFailureError
     *             In the event of auth failure
     * 
     * @deprecated Use {@link #getBody()} instead.
     */
    @Deprecated
    @Nullable
    public byte[] getPostBody() throws AuthFailureError
    {
        // Note: For compatibility with legacy clients of volley, this
        // implementation must remain
        // here instead of simply calling the getBody() function because this
        // function must
        // call getPostParams() and getPostParamsEncoding() since legacy clients
        // would have
        // overridden these two member functions for POST requests.
        Map<String, String> postParams = getPostParams();
        if (postParams != null && postParams.size() > 0) {
            return encodeParameters(postParams, getPostParamsEncoding());
        }
        return null;
    }

    /**
     * Returns a Map of parameters to be used for a POST or PUT request. Can
     * throw {@link AuthFailureError} as authentication may be required to
     * provide these values.
     * 
     * <p>
     * Note that you can directly override {@link #getBody()} for custom data.
     * </p>
     * 
     * @throws AuthFailureError
     *             in the event of auth failure
     */
    @Nullable
    protected Map<String, String> getParams() throws AuthFailureError
    {
        return null;
    }

    /**
     * Returns which encoding should be used when converting POST or PUT
     * parameters returned by {@link #getParams()} into a raw POST or PUT body.
     * 
     * <p>
     * This controls both encodings:
     * <ol>
     * <li>The string encoding used when converting parameter names and values
     * into bytes prior to URL encoding them.</li>
     * <li>The string encoding used when converting the URL encoded parameters
     * into a raw byte array.</li>
     * </ol>
     */
    @NonNull
    protected String getParamsEncoding()
    {
        return DEFAULT_PARAMS_ENCODING;
    }

    /**
     * Returns the content type of the POST or PUT body.
     */
    @NonNull
    public String getBodyContentType()
    {
        return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
    }

    /**
     * Returns the raw POST or PUT body to be sent.
     * 
     * <p>
     * By default, the body consists of the request parameters in
     * application/x-www-form-urlencoded format. When overriding this method,
     * consider overriding {@link #getBodyContentType()} as well to match the
     * new body format.
     * 
     * @throws AuthFailureError
     *             in the event of auth failure
     */
    @Nullable
    public byte[] getBody() throws AuthFailureError
    {
        Map<String, String> params = getParams();
        if (params != null && params.size() > 0) {
            return encodeParameters(params, getParamsEncoding());
        }
        return null;
    }

    /**
     * Converts <code>params</code> into an application/x-www-form-urlencoded
     * encoded string.
     */
    private byte[] encodeParameters(Map<String, String> params, String paramsEncoding)
    {
        StringBuilder encodedParams = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
                encodedParams.append('&');
            }
            return encodedParams.toString().getBytes(paramsEncoding);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
        }
    }

    /**
     * Priority values. Requests will be processed from higher priorities to
     * lower priorities, in FIFO order.
     */
    public enum Priority {
                          LOW, NORMAL, HIGH, IMMEDIATE
    }

    /**
     * Returns the {@link Priority} of this request; {@link Priority#NORMAL} by
     * default.
     */
    @NonNull
    public Priority getPriority()
    {
        return Priority.NORMAL;
    }

    /**
     * Returns the socket timeout in milliseconds per retry attempt. (This value
     * can be changed
     * per retry attempt if a backoff is specified via backoffTimeout()). If
     * there are no retry
     * attempts remaining, this will cause delivery of a {@link TimeoutError}
     * error.
     */
    public final int getTimeoutMs()
    {
        return mRetryPolicy.getCurrentTimeout();
    }

    /**
     * Returns the retry policy that should be used for this request.
     */
    @Nullable
    public RetryPolicy getRetryPolicy()
    {
        return mRetryPolicy;
    }

    /**
     * Mark this request as having a response delivered on it. This can be used
     * later in the request's lifetime for suppressing identical responses.
     */
    public void markDelivered()
    {
        mResponseDelivered = true;
    }

    /**
     * Returns true if this request has had a response delivered for it.
     */
    public boolean hasHadResponseDelivered()
    {
        return mResponseDelivered;
    }

    /**
     * Subclasses must implement this to parse the raw network response
     * and return an appropriate response type. This method will be
     * called from a worker thread. The response will not be delivered
     * if you return null.
     * 
     * @param response
     *            Response from the network
     * @return The parsed response, or null in the case of an error
     */
    @Nullable
    public abstract Response<T> parseNetworkResponse(@NonNull NetworkResponse response);

    /**
     * Subclasses can override this method to parse 'networkError' and return a
     * more specific error.
     * 
     * <p>
     * The default implementation just returns the passed 'networkError'.
     * </p>
     * 
     * @param volleyError
     *            the error retrieved from the network
     * @return an NetworkError augmented with additional information
     */
    @NonNull
    public VolleyError parseNetworkError(@NonNull VolleyError volleyError)
    {
        return volleyError;
    }

    /**
     * Subclasses must implement this to perform delivery of the parsed
     * response to their listeners. The given response is guaranteed to
     * be non-null; responses that fail to parse are not delivered.
     * 
     * @param response
     *            The parsed response returned by
     *            {@link #parseNetworkResponse(NetworkResponse)}
     */
    public abstract void deliverResponse(@Nullable T response);

    /**
     * Delivers error message to the ErrorListener that the Request was
     * initialized with.
     * 
     * @param error
     *            Error details
     */
    public void deliverError(@Nullable VolleyError error)
    {
        if (mErrorListener != null) {
            mErrorListener.onErrorResponse(error);
        }
    }

    public boolean getUseTls()
    {
        return mUseTls;
    }

    /**
     * If true use SW certif for initializing tls connection, false ignore cert in
     * case of https connection
     *
     * @param useTls
     */
    public void setUseTls(boolean useTls)
    {
        this.mUseTls = useTls;
    }

    public boolean isInternalApi()
    {
        return mInternalApi;
    }

    /**
     * if true use this request is part of SmartMs API
     *
     * @param internalApi
     */
    public void setInternalApi(boolean internalApi)
    {
        this.mInternalApi = internalApi;
    }

    /**
     * Our comparator sorts from high to low priority, and secondarily by
     * sequence number to provide FIFO ordering.
     */
    @Override
    public int compareTo(@NonNull Request<T> other)
    {
        Priority left = this.getPriority();
        Priority right = other.getPriority();

        // High-priority requests are "lesser" so they are sorted to the front.
        // Equal priorities are sorted by sequence number to provide FIFO
        // ordering.
        return left == right ? this.mSequence - other.mSequence : right.ordinal() - left.ordinal();
    }

    @Override
    public boolean equals(Object obj)
    {
        return super.equals(obj);
    }

    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

    @Override
    public String toString()
    {
        String trafficStatsTag = "0x" + Integer.toHexString(getTrafficStatsTag());
        return (mCanceled ? "[X] " : "[ ] ") + getUrl() + " " + trafficStatsTag + " " + getPriority() + " " + mSequence;
    }
}
