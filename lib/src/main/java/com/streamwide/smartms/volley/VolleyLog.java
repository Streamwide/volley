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

package com.streamwide.smartms.volley;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Logging helper class.
 * <p/>
 * to see Volley logs call:<br/>
 * {@code <android-sdk>/platform-tools/adb shell setprop log.tag.Volley VERBOSE}
 */
public class VolleyLog {

    private static final String CLASS_NAME = "VolleyLog";

    private static String TAG = VolleyLog.class.getSimpleName();

    /**
     * private constructor to hide the implicit public one.
     */
    private VolleyLog()
    {
        // do nothing...
    }

    /**
     * Customize the log tag for your application, so that other apps
     * using Volley don't mix their logs with yours. <br />
     * Enable the log property for your tag before starting your app: <br />
     * {@code adb shell setprop log.tag.&lt;tag&gt;}
     */
    public static void setTag(@NonNull String tag)
    {
        d("Changing log tag to %s", tag);
        TAG = tag;
    }

    public static void d(@NonNull String format, @Nullable Object... args)
    {
        //Logger.d(Pair.create(CLASS_NAME, "d"), TAG, buildMessage(format, args));
    }

    public static void e(@Nullable Throwable tr, @NonNull String format, @Nullable Object... args)
    {
        //Logger.e(Pair.create(CLASS_NAME, "e"), tr, TAG, buildMessage(format, args));
    }

    public static void wtf(@NonNull String format, @Nullable Object... args)
    {
        //Logger.wtf(Pair.create(CLASS_NAME, "wtf"), TAG, buildMessage(format, args));
    }

    public static void wtf(@Nullable Throwable tr, @NonNull String format, @Nullable Object... args)
    {
        //Logger.wtf(Pair.create(CLASS_NAME, "wtf"), TAG, buildMessage(format, args), tr);
    }

    /**
     * Formats the caller's provided message and prepends useful info like
     * calling thread ID and method name.
     */
    private static String buildMessage(String format, Object... args)
    {
        String msg = (args == null) ? format : String.format(Locale.US, format, args);
        StackTraceElement[] trace = new Throwable().fillInStackTrace().getStackTrace();

        String caller = "<unknown>";
        // Walk up the stack looking for the first caller outside of VolleyLog.
        // It will be at least two frames up, so start there.
        for (int i = 2; i < trace.length; i++) {
            Class<?> clazz = trace[i].getClass();
            if (!clazz.equals(VolleyLog.class)) {
                String callingClass = trace[i].getClassName();
                callingClass = callingClass.substring(callingClass.lastIndexOf('.') + 1);
                callingClass = callingClass.substring(callingClass.lastIndexOf('$') + 1);

                caller = callingClass + "." + trace[i].getMethodName();
                break;
            }
        }
        return String.format(Locale.US, "[%d] %s: %s", Thread.currentThread().getId(), caller, msg);
    }

    /**
     * A simple event log with records containing a name, thread ID, and
     * timestamp.
     */
    public static class MarkerLog {

        /**
         * Minimum duration from first marker to last in an marker log to
         * warrant logging.
         */
        private static final long MIN_DURATION_FOR_LOGGING_MS = 0;

        private static class Marker {

            public final String name;
            public final long thread;
            public final long time;

            public Marker(String name, long thread, long time)
            {
                this.name = name;
                this.thread = thread;
                this.time = time;
            }
        }

        private final List<Marker> mMarkers = new ArrayList<>();
        private boolean mFinished = false;

        /** Adds a marker to this log with the specified name. */
        public synchronized void add(String name, long threadId)
        {
            if (mFinished) {
                throw new IllegalStateException("Marker added to finished log");
            }

            mMarkers.add(new Marker(name, threadId, SystemClock.elapsedRealtime()));
        }

        /**
         * Closes the log, dumping it to logcat if the time difference between
         * the first and last markers is greater than
         * {@link #MIN_DURATION_FOR_LOGGING_MS}.
         * 
         * @param header
         *            Header string to print above the marker log.
         */
        public synchronized void finish(String header)
        {
            mFinished = true;

            long duration = getTotalDuration();
            if (duration <= MIN_DURATION_FOR_LOGGING_MS) {
                return;
            }

            long prevTime = mMarkers.get(0).time;
            d("(%-4d ms) %s", duration, header);
            for (Marker marker : mMarkers) {
                long thisTime = marker.time;
                d("(+%-4d) [%2d] %s", (thisTime - prevTime), marker.thread, marker.name);
                prevTime = thisTime;
            }
        }

        @Override
        protected void finalize() throws Throwable
        {
            // Catch requests that have been collected (and hence end-of-lifed)
            // but had no debugging output printed for them.
            if (!mFinished) {
                finish("Request on the loose");
                e(null, "Marker log finalized without finish() - uncaught exit point for request");
            }
        }

        /**
         * Returns the time difference between the first and last events in this
         * log.
         */
        private long getTotalDuration()
        {
            if (mMarkers.isEmpty()) {
                return 0;
            }

            long first = mMarkers.get(0).time;
            long last = mMarkers.get(mMarkers.size() - 1).time;
            return last - first;
        }
    }
}
