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


import android.os.Parcel;
import android.os.Parcelable;
import android.text.SpannableString;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Represents a request parameter.
 *
 * @author gotev (Aleksandar Gotev)
 *
 */
public final class NameValue implements Parcelable {

    // This is used to regenerate the object.
    // All Parcelables must have a CREATOR that implements these two methods
    public static final Creator<NameValue> CREATOR = new Creator<NameValue>() {

        @Override
        public NameValue createFromParcel(final Parcel in)
        {
            return new NameValue(in);
        }

        @Override
        public NameValue[] newArray(final int size)
        {
            return new NameValue[size];
        }
    };
    private static final String NEW_LINE = "\r\n";
    private final String name;
    private final String value;
    private final Charset US_ASCII = StandardCharsets.US_ASCII;
    private final Charset UTF8 = StandardCharsets.UTF_8;

    public NameValue(@NonNull final String name, @Nullable final String value) {
        this.name = name;
        this.value = value;
    }

    /*pachage*/NameValue(Parcel in)
    {
        name = in.readString();
        value = in.readString();
    }

    @Nullable
    public final String getName() {
        return name;
    }

    @Nullable
    public final String getValue() {
        if (value == null) {
            return null;
        }

        SpannableString spannableString = new SpannableString(value);
        return spannableString.toString();
    }

    @NonNull
    public byte[] getMultipartBytes(boolean isUtf8) throws UnsupportedEncodingException {
        return ("Content-Disposition: form-data; name=\"" + name + "\""
                + NEW_LINE + NEW_LINE + value).getBytes(isUtf8 ? UTF8 : US_ASCII);
    }

    @Override
    public boolean equals(Object object) {

        if (object == null) {
            return false;
        }

        if (this.getClass() != object.getClass()) {
            return false;
        }

        boolean areEqual;

        try {
            final NameValue other = (NameValue) object;
            areEqual = this.name.equals(other.name) && this.value.equals(other.value);
        } catch (ClassCastException ex) {
            areEqual = false;
        }

        return areEqual;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int arg1) {
        parcel.writeString(name);
        parcel.writeString(value);
    }
}
