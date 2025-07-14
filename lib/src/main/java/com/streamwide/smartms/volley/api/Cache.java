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

import java.util.Collections;
import java.util.Map;

/**
 * An interface for a cache keyed by a String with a byte array as data.
 */
public interface Cache {

    /**
     * Retrieves an entry from the cache.
     * 
     * @param key
     *            Cache key
     * @return An {@link Entry} or null in the event of a cache miss
     */
    @Nullable
    public Entry get(@NonNull String key);

    /**
     * Adds or replaces an entry to the cache.
     * 
     * @param key
     *            Cache key
     * @param entry
     *            Data to store and metadata for cache coherency, TTL, etc.
     */
    public void put(@NonNull String key, @NonNull Entry entry);

    /**
     * Performs any potentially long-running actions needed to initialize the
     * cache;
     * will be called from a worker thread.
     */
    public void initialize();

    /**
     * Invalidates an entry in the cache.
     * 
     * @param key
     *            Cache key
     * @param fullExpire
     *            True to fully expire the entry, false to soft expire
     */
    public void invalidate(@NonNull String key, boolean fullExpire);

    /**
     * Removes an entry from the cache.
     * 
     * @param key
     *            Cache key
     */
    public void remove(@NonNull String key);

    /**
     * Empties the cache.
     */
    public void clear();

    /**
     * Data and metadata for an entry returned by the cache.
     */
    public static class Entry {

        /** The data returned from cache. */
        private byte[] data;

        /** ETag for cache coherency. */
        private String etag;

        /** Date of this response as reported by the server. */
        private long serverDate;

        /** The last modified date for the requested object. */
        private long lastModified;

        /** TTL for this record. */
        private long ttl;

        /** Soft TTL for this record. */
        private long softTtl;

        /**
         * Immutable response headers as received from server; must be non-null.
         */
        private Map<String, String> responseHeaders = Collections.emptyMap();

        /** True if the entry is expired. */
        public boolean isExpired()
        {
            return this.ttl < System.currentTimeMillis();
        }

        /** True if a refresh is needed from the original data source. */
        public boolean refreshNeeded()
        {
            return this.softTtl < System.currentTimeMillis();
        }

        @Nullable
        public byte[] getData()
        {
            return data != null ? data.clone() : null;
        }

        public void setData(@Nullable byte[] data)
        {
            this.data = data != null ? data.clone() : null;
        }

        @Nullable
        public String getETag()
        {
            return etag;
        }

        public void setETag(@Nullable String etag)
        {
            this.etag = etag;
        }

        public long getServerDate()
        {
            return serverDate;
        }

        public void setServerDate(long serverDate)
        {
            this.serverDate = serverDate;
        }

        public long getLastModified()
        {
            return lastModified;
        }

        public void setLastModified(long lastModified)
        {
            this.lastModified = lastModified;
        }

        public long getTTL()
        {
            return ttl;
        }

        public void setTTL(long ttl)
        {
            this.ttl = ttl;
        }

        public long getSoftTTL()
        {
            return softTtl;
        }

        public void setSoftTTL(long softTtl)
        {
            this.softTtl = softTtl;
        }

        @Nullable
        public Map<String, String> getResponseHeaders()
        {
            return responseHeaders;
        }

        public void setResponseHeaders(@Nullable Map<String, String> responseHeaders)
        {
            this.responseHeaders = responseHeaders;
        }
    }

}
