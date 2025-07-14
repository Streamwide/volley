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

package com.streamwide.smartms.volley.model;

import androidx.annotation.Nullable;

import java.io.InputStream;

/**
 * Created by streamwide on 28/08/17.
 */

public class VolleyHttpEntity {

    private InputStream content;
    private int contentLength;
    private String contentEncoding;
    private String contentType;

    public VolleyHttpEntity()
    {
        // do nothing...
    }

    @Nullable
    public InputStream getContent()
    {
        return content;
    }

    public void setContent(@Nullable InputStream content)
    {
        this.content = content;
    }

    public int getContentLength()
    {
        return contentLength;
    }

    public void setContentLength(int contentLength)
    {
        this.contentLength = contentLength;
    }

    @Nullable
    public String getContentEncoding()
    {
        return contentEncoding;
    }

    public void setContentEncoding(@Nullable String contentEncoding)
    {
        this.contentEncoding = contentEncoding;
    }

    @Nullable
    public String getContentType()
    {
        return contentType;
    }

    public void setContentType(@Nullable String contentType)
    {
        this.contentType = contentType;
    }
}
