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

package com.streamwide.smartms.volley.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streamwide.smartms.volley.util.STWDateUtil;
import com.streamwide.smartms.volley.VolleyLog;

import java.text.ParseException;
import java.util.Map;

/**
 * Utility methods for parsing HTTP headers.
 */
public class HttpHeaderParser {

    private static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";

    /**
     * private constructor to hide the implicit public one.
     */
    private HttpHeaderParser()
    {
        // do nothing...
    }

    /**
     * Extracts a {@link Cache.Entry} from a {@link NetworkResponse}.
     * 
     * @param response
     *            The network response to parse headers from
     * @return a cache entry for the given response, or null if the response is
     *         not cacheable.
     */
    @Nullable
    public static Cache.Entry parseCacheHeaders(@NonNull NetworkResponse response)
    {
        long now = System.currentTimeMillis();

        Map<String, String> headers = response.headers;

        long serverDate = 0;
        long lastModified = 0;
        long serverExpires = 0;
        long softExpire = 0;
        long finalExpire = 0;
        long maxAge = 0;
        long staleWhileRevalidate = 0;
        boolean hasCacheControl = false;
        boolean mustRevalidate = false;

        String serverEtag = null;
        String headerValue;

        headerValue = headers.get("Date");
        if (headerValue != null) {
            serverDate = parseDateAsEpoch(headerValue);
        }

        headerValue = headers.get("Cache-Control");
        if (headerValue != null) {
            hasCacheControl = true;
            String[] tks = headerValue.split(",");
            for (int i = 0; i < tks.length; i++) {
                String tk = tks[i].trim();
                if (tk.equals("no-cache") || tk.equals("no-store")) {
                    return null;
                } else if (tk.startsWith("max-age=")) {
                    try {
                        maxAge = Long.parseLong(tk.substring(8));
                    } catch (Exception e) {
                        // do nothing...
                    }
                } else if (tk.startsWith("stale-while-revalidate=")) {
                    try {
                        staleWhileRevalidate = Long.parseLong(tk.substring(23));
                    } catch (Exception e) {
                        // do nothing...
                    }
                } else if (tk.equals("must-revalidate") || tk.equals("proxy-revalidate")) {
                    mustRevalidate = true;
                }
            }
        }

        headerValue = headers.get("Expires");
        if (headerValue != null) {
            serverExpires = parseDateAsEpoch(headerValue);
        }

        headerValue = headers.get("Last-Modified");
        if (headerValue != null) {
            lastModified = parseDateAsEpoch(headerValue);
        }

        serverEtag = headers.get("ETag");

        // Cache-Control takes precedence over an Expires header, even if both
        // exist and Expires
        // is more restrictive.
        if (hasCacheControl) {
            softExpire = now + maxAge * 1000;
            finalExpire = mustRevalidate ? softExpire : softExpire + staleWhileRevalidate * 1000;
        } else if (serverDate > 0 && serverExpires >= serverDate) {
            // Default semantic for Expire header in HTTP specification is
            // softExpire.
            softExpire = now + (serverExpires - serverDate);
            finalExpire = softExpire;
        }

        Cache.Entry entry = new Cache.Entry();
        entry.setData(response.data);
        entry.setETag(serverEtag);
        entry.setSoftTTL(softExpire);
        entry.setTTL(finalExpire);
        entry.setServerDate(serverDate);
        entry.setLastModified(lastModified);
        entry.setResponseHeaders(headers);

        return entry;
    }

    /**
     * Parse date in RFC1123 format, and return its value as epoch
     */
    private static long parseDateAsEpoch(String dateStr)
    {
        try {
            // Parse date in RFC1123 format if this header contains one
            return STWDateUtil.parse(dateStr, PATTERN_RFC1123).getTime();
        } catch (ParseException e) {
            VolleyLog.e(null, "Error while parsing Date : %s", e.getMessage());
            return 0;
        }

    }

    /**
     * Retrieve a charset from headers
     * 
     * @param headers
     *            An {@link Map} of headers
     * @param defaultCharset
     *            Charset to return if none can be found
     * @return Returns the charset specified in the Content-Type of this header,
     *         or the defaultCharset if none can be found.
     */
    @NonNull
    public static String parseCharset(@NonNull Map<String, String> headers, @NonNull String defaultCharset)
    {
        String contentType = headers.get("Content-Type");
        if (contentType != null) {
            String[] params = contentType.split(";");
            for (int i = 1; i < params.length; i++) {
                String[] pair = params[i].trim().split("=");
                if (pair.length == 2 && pair[0].equals("charset")) {
                    return pair[1];
                }
            }
        }

        return defaultCharset;
    }

    /**
     * Returns the charset specified in the Content-Type of this header,
     * or the HTTP default (ISO-8859-1) if none can be found.
     */
    @NonNull
    public static String parseCharset(@NonNull Map<String, String> headers)
    {
        return parseCharset(headers, "ISO-8859-1");
    }
}
