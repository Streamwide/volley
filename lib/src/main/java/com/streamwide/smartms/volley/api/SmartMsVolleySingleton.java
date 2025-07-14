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

package com.streamwide.smartms.volley.api;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.streamwide.smartms.volley.ExecutorDelivery;
import com.streamwide.smartms.volley.Network;
import com.streamwide.smartms.volley.RequestQueue;
import com.streamwide.smartms.volley.ResponseDelivery;
import com.streamwide.smartms.volley.toolbox.BasicNetwork;
import com.streamwide.smartms.volley.toolbox.HttpStack;
import com.streamwide.smartms.volley.toolbox.HurlStack;
import com.streamwide.smartms.volley.toolbox.ImageLoader;
import com.streamwide.smartms.volley.util.LruBitmapCache;

import java.net.HttpURLConnection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
public class SmartMsVolleySingleton {

    private static final String TAG = SmartMsVolleySingleton.class.getSimpleName();
    private SSLSocketFactory sslSocketFactory;
    private HostnameVerifier hostnameVerifier;

    private static SmartMsVolleySingleton instance;
    private RequestQueue requestQueue;
    private ImageLoader imageLoader;
    private HttpStack mStack;

    private SmartMsVolleySingleton()
    {
        this(new ExecutorDelivery(new Handler(Looper.getMainLooper())));
    }

    @VisibleForTesting
    SmartMsVolleySingleton(ResponseDelivery responseDelivery)
    {
        if (mStack == null) {
            mStack = new HurlStack();
        }

        Network network = new BasicNetwork(mStack);

        RequestQueue queue = new RequestQueue(4, responseDelivery, network);
        queue.start();

        requestQueue = queue;

        imageLoader = new ImageLoader(requestQueue, new LruBitmapCache());
    }

    @NonNull
    public static SmartMsVolleySingleton getInstance()
    {
        if (instance == null) {
            instance = new SmartMsVolleySingleton();
        }
        return instance;
    }

    @VisibleForTesting
    static void setInstance(SmartMsVolleySingleton i)
    {
        instance = i;
    }

    private RequestQueue getRequestQueue()
    {
        return requestQueue;
    }

    @NonNull
    public ImageLoader getImageLoader()
    {
        return imageLoader;
    }

    /**
     * Cancels all requests in this queue with the given tag. Tag must be
     * non-null
     * and equality is by identity.
     */
    public void cancelAll(@Nullable String tag)
    {

        if (TextUtils.isEmpty(tag)) {
            return;
        }
        getRequestQueue().cancelAll(tag);
    }

    public <T> void addToRequestQueue(@NonNull Request<T> req, @Nullable String tag)
    {
        if (!TextUtils.isEmpty(tag)) {
            req.setTag(tag);
        } else {
            req.setTag(TAG);
        }

        getRequestQueue().add(req);
    }

    @VisibleForTesting
    public <T> void addRequestFinishedListener(@NonNull RequestQueue.RequestFinishedListener<T> listener){
        getRequestQueue().addRequestFinishedListener(listener);
    }

    @VisibleForTesting
    public <T> void removeRequestFinishedListener(@NonNull RequestQueue.RequestFinishedListener<T> listener){
        getRequestQueue().removeRequestFinishedListener(listener);
    }

    // Builder Methods
    public SmartMsVolleySingleton setSSLSocketFactory(SSLSocketFactory factory) {
        this.sslSocketFactory = factory;
        return this; // Return instance for chaining
    }

    public SmartMsVolleySingleton setHostnameVerifier(HostnameVerifier verifier) {
        this.hostnameVerifier = verifier;
        return this; // Return instance for chaining
    }

    public SmartMsVolleySingleton build() {

        return this;
    }

    public void initTls(HttpURLConnection connection){
        if(sslSocketFactory != null){
            ((HttpsURLConnection) connection).setSSLSocketFactory(sslSocketFactory);
            ((HttpsURLConnection) connection).setHostnameVerifier(hostnameVerifier);
        }
    }




}