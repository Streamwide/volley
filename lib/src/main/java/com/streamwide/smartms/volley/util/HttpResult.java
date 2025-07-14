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
 * @lastModifiedOn Tue, 4 Mar 2025 09:34:52 +0100
 */
package com.streamwide.smartms.volley.util;

import androidx.annotation.Nullable;

/** HTTP transaction result class */
public class HttpResult {

    /** HTTP transaction succeeds */
    public static final int TYPE_SUCCESS = 0x11;
    /** HTTP transaction fails with server response */
    public static final int TYPE_FAIL = 0x12;
    /**
     * HTTP transaction error (needs retry, still TYPE_ERROR after retrying
     * reaches limit.
     */
    public static final int TYPE_ERROR = 0x13;
    public static final int TYPE_CANCEL = 0x14;

    /**
     * HTTP response code
     */
    public static final int RESPONSE_TYPE_TRANSFER = 0x15;
    public static final int RESPONSE_TYPE_ACCEPT = 0x16;
    public static final int RESPONSE_TYPE_REJECT = 0x17;
    public static final int RESPONSE_TYPE_ERROR = 0x18;

    /** HTTP transaction fails with server because file is infected */
    public static final int TYPE_INFECTED = 0x19;
    public static final int RESPONSE_TYPE_SERVICE_ORGANISATION_ID = 0x1A;
    public static final int RESPONSE_TYPE_OPENID_CONNECT = 0x1B;

    private int mResponseType;

    /**
     * Server code response used to handle error case
     */
    private int mResponseCode;

    /** Result type */
    private int mType;
    /** Error data */
    private Object mData;

    public void setType(int type)
    {
        mType = type;
    }

    public int getType()
    {
        return mType;
    }

    public void setData(@Nullable Object data)
    {
        mData = data;
    }

    public @Nullable Object getData()
    {
        return mData;
    }

    public int getResponseCode()
    {
        return mResponseCode;
    }

    public void setResponseCode(int responseCode)
    {
        mResponseCode = responseCode;
    }

    public int getResponseType()
    {
        return mResponseType;
    }

    public void setResponseType(int requestResponseType)
    {
        mResponseType = requestResponseType;
    }

    @Override
    public String toString()
    {
        return "HttpResult{" + "mResponseType=" + mResponseType + ", mResponseCode=" + mResponseCode + ", mType="
            + mType + ", mData=" + mData + '}';
    }
}