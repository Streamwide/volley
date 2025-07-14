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


import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * DateUtil date class.
 *
 * TODO translate days strings, including today and yesterday using i18n and
 * string.xml file
 *
 * @author MGMClient team <MGMClient@streamwide.com>
 */
public class STWDateUtil {



    private static final Locale LOCALE = Locale.ENGLISH;

    /**
     * private constructor to hide the implicit public one.
     */
    private STWDateUtil()
    {
        // do nothing...
    }

    @Nullable
    public static Date parse(@NonNull String strDate, @NonNull String pattern) throws ParseException
    {
        SimpleDateFormat df = new SimpleDateFormat(pattern, LOCALE);
        return df.parse(strDate);
    }
}
