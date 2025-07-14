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

import static com.streamwide.smartms.volley.toolbox.HurlStack.HttpMethod.POST;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streamwide.smartms.volley.AuthFailureError;
import com.streamwide.smartms.volley.VolleyLog;
import com.streamwide.smartms.volley.api.Request;
import com.streamwide.smartms.volley.api.SmartMsVolleySingleton;
import com.streamwide.smartms.volley.model.VolleyHttpEntity;
import com.streamwide.smartms.volley.model.VolleyHttpResponse;
import com.streamwide.smartms.volley.util.HttpResult;

import net.gotev.uploadservice.NameValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;

/**
 * An {@link HttpStack} based on {@link HttpURLConnection}.
 */
public class HurlStack implements HttpStack {

    /**
     * An interface for transforming URLs before use.
     */
    public interface UrlRewriter {

        /**
         * Returns a URL to use instead of the provided one, or null to indicate
         * this URL should not be used at all.
         */
        @Nullable
        public String rewriteUrl(@Nullable String originalUrl);
    }

    private final UrlRewriter mUrlRewriter;

    public HurlStack()
    {
        this(null);
    }

    /**
     * @param urlRewriter
     *            Rewriter to use for request URLs
     */
    public HurlStack(@Nullable UrlRewriter urlRewriter)
    {
        mUrlRewriter = urlRewriter;
    }

    @Override
    @NonNull
    public VolleyHttpResponse performRequest(@NonNull Request<?> request, @NonNull Map<String, String> additionalHeaders)
        throws IOException, AuthFailureError
    {
        String url = request.getUrl();
        HashMap<String, String> map = new HashMap<>();
        map.putAll(request.getHeaders());
        map.putAll(additionalHeaders);
        if (mUrlRewriter != null) {
            String rewritten = mUrlRewriter.rewriteUrl(url);
            if (rewritten == null) {
                throw new IOException("URL blocked by rewriter: " + url);
            }
            url = rewritten;
        }
        URL parsedUrl = new URL(url);
        HttpURLConnection connection = openConnection(parsedUrl, request);

        for (Entry<String, String> entry : map.entrySet()) {
            String headerName = entry.getKey();
            connection.addRequestProperty(headerName, map.get(headerName));
        }

        try {
            if (requiresRequestBody(request.getMethod())) {
                connection.setDoOutput(true);

                byte[] body = request.getBody();
                if (body != null) {
                    try (OutputStream os = connection.getOutputStream()) {
                        os.write(body);
                        os.flush();
                    }
                }
            }
        }catch (AuthFailureError error){
            VolleyLog.e(error, "Unexpected body "+error.getMessage());
        }

        // Initialize VolleyHttpResponse with data from the HttpURLConnection.
        int responseCode = connection.getResponseCode();
        if (responseCode == -1) {
            // -1 is returned by getResponseCode() if the response code could
            // not be retrieved.
            // Signal to the caller that something was wrong with the
            // connection.
            throw new IOException("Could not retrieve response code from HttpUrlConnection.");
        }
        HttpResult httpResult = new HttpResult();
        httpResult.setResponseCode(connection.getResponseCode());
        httpResult.setData(connection.getResponseMessage());
        VolleyHttpResponse response = new VolleyHttpResponse();
        response.setHttpResult(httpResult);
        response.setEntity(entityFromConnection(connection));
        for (Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
            if (header.getKey() != null) {
                NameValue h = new NameValue(header.getKey(), header.getValue().get(0));
                response.addHeader(h);
            }
        }
        return response;
    }

    /**
     * Initializes an {@link VolleyHttpEntity} from the given
     * {@link HttpURLConnection}.
     * 
     * @param connection
     * @return an VolleyHttpEntity populated with data from <code>connection</code>.
     */
    private static VolleyHttpEntity entityFromConnection(HttpURLConnection connection)
    {
        VolleyHttpEntity entity = new VolleyHttpEntity();
        InputStream inputStream;
        try {
            inputStream = connection.getInputStream();
        } catch (IOException ioe) {
            inputStream = connection.getErrorStream();
        }
        entity.setContent(inputStream);
        entity.setContentLength(connection.getContentLength());
        entity.setContentEncoding(connection.getContentEncoding());
        entity.setContentType(connection.getContentType());
        return entity;
    }

    /**
     * Create an {@link HttpURLConnection} for the specified {@code url}.
     */
    private HttpURLConnection createConnection(URL url, boolean useTLS) throws IOException
    {
        HttpURLConnection connection = null;

        if (url.getProtocol().equals("https")) {
            connection = (HttpsURLConnection) url.openConnection(Proxy.NO_PROXY);
            if (useTLS) {
                SmartMsVolleySingleton.getInstance()
                        .initTls(connection);
            }
        } else {
            connection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
        }

        return connection;
    }

    /**
     * Opens an {@link HttpURLConnection} with parameters.
     * 
     * @param url
     * @return an open connection
     * @throws IOException
     */
    private HttpURLConnection openConnection(URL url, Request<?> request) throws IOException
    {
        HttpURLConnection connection = createConnection(url, request.getUseTls());

        int timeoutMs = request.getTimeoutMs();
        connection.setRequestMethod(request.getMethod());
        connection.setConnectTimeout(timeoutMs);
        connection.setReadTimeout(timeoutMs);
        connection.setUseCaches(false);
        connection.setDoInput(true);

        return connection;
    }

    private boolean requiresRequestBody(String method) {
        return  method.equalsIgnoreCase(HttpMethod.PUT)
                || method.equalsIgnoreCase(POST);
    }


    public interface HttpMethod {

        String GET = "GET";
        String POST = "POST";
        String PUT = "PUT";
        String DELETE = "DELETE";
        String HEAD = "HEAD";
        String OPTIONS = "OPTIONS";
    }

}
