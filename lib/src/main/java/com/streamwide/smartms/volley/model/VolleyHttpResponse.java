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

import com.streamwide.smartms.volley.util.HttpResult;

import net.gotev.uploadservice.CollectionUtil;
import net.gotev.uploadservice.NameValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by streamwide on 28/08/17.
 */

public class VolleyHttpResponse {

    private VolleyHttpEntity entity;
    private List<NameValue> headers;
    private HttpResult httpResult;

    public VolleyHttpResponse()
    {
        // do nothing...
    }

    @Nullable
    public VolleyHttpEntity getEntity()
    {
        return entity;
    }

    public void setEntity(@Nullable VolleyHttpEntity entity)
    {
        this.entity = entity;
    }

    public void addHeader(@Nullable NameValue header)
    {
        if (headers == null) {
            headers = new ArrayList<>();
        }
        headers.add(header);
    }

    @Nullable
    public List<NameValue> getHeaders()
    {
        return CollectionUtil.copyList(headers);
    }

    public void setHeaders(@Nullable List<NameValue> headers)
    {
        this.headers = CollectionUtil.copyList(headers);
    }

    @Nullable
    public HttpResult getHttpResult()
    {
        return httpResult;
    }

    public void setHttpResult(@Nullable HttpResult httpResult)
    {
        this.httpResult = httpResult;
    }
}
