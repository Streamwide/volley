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

import com.streamwide.smartms.volley.AuthFailureError;
import com.streamwide.smartms.volley.api.Request;
import com.streamwide.smartms.volley.model.VolleyHttpResponse;

import java.io.IOException;
import java.util.Map;

/**
 * An HTTP stack abstraction.
 */
public interface HttpStack {

    /**
     * Performs an HTTP request with the given parameters.
     * 
     * <p>
     * A GET request is sent if request.getPostBody() == null. A POST request is
     * sent otherwise, and the Content-Type header is set to
     * request.getPostBodyContentType().
     * </p>
     * 
     * @param request
     *            the request to perform
     * @param additionalHeaders
     *            additional headers to be sent together with
     *            {@link Request#getHeaders()}
     * @return the HTTP response
     */
    @NonNull
    public VolleyHttpResponse performRequest(@NonNull Request<?> request, @NonNull Map<String, String> additionalHeaders)
        throws IOException, AuthFailureError;

}
