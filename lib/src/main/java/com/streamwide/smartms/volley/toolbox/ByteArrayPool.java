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

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * ByteArrayPool is a source and repository of <code>byte[]</code> objects. Its
 * purpose is to
 * supply those buffers to consumers who need to use them for a short period of
 * time and then
 * dispose of them. Simply creating and disposing such buffers in the
 * conventional manner can
 * considerable heap churn and garbage collection delays on Android, which lacks
 * good management of
 * short-lived heap objects. It may be advantageous to trade off some memory in
 * the form of a
 * permanently allocated pool of buffers in order to gain heap performance
 * improvements; that is
 * what this class does.
 * <p>
 * A good candidate user for this class is something like an I/O system that
 * uses large temporary <code>byte[]</code> buffers to copy data around. In
 * these use cases, often the consumer wants the buffer to be a certain minimum
 * size to ensure good performance (e.g. when copying data chunks off of a
 * stream), but doesn't mind if the buffer is larger than the minimum. Taking
 * this into account and also to maximize the odds of being able to reuse a
 * recycled buffer, this class is free to return buffers larger than the
 * requested size. The caller needs to be able to gracefully deal with getting
 * buffers any size over the minimum.
 * <p>
 * If there is not a suitably-sized buffer in its recycling pool when a buffer
 * is requested, this class will allocate a new buffer and return it.
 * <p>
 * This class has no special ownership of buffers it creates; the caller is free
 * to take a buffer it receives from this pool, use it permanently, and never
 * return it to the pool; additionally, it is not harmful to return to this pool
 * a buffer that was allocated elsewhere, provided there are no other lingering
 * references to it.
 * <p>
 * This class ensures that the total size of the buffers in its recycling pool
 * never exceeds a certain byte limit. When a buffer is returned that would
 * cause the pool to exceed the limit, least-recently-used buffers are disposed.
 */
public class ByteArrayPool {

    /** The buffer pool, arranged both by last use and by buffer size */
    private List<byte[]> mBuffersByLastUse = new LinkedList<>();
    private List<byte[]> mBuffersBySize = new ArrayList<>(64);

    /** The total size of the buffers in the pool */
    private int mCurrentSize = 0;

    /**
     * The maximum aggregate size of the buffers in the pool. Old buffers are
     * discarded to stay
     * under this limit.
     */
    private final int mSizeLimit;

    /** Compares buffers by size */
    protected static final Comparator<byte[]> BUF_COMPARATOR = new Comparator<byte[]>() {

        @Override
        public int compare(byte[] lhs, byte[] rhs)
        {
            return lhs.length - rhs.length;
        }
    };

    /**
     * @param sizeLimit
     *            the maximum size of the pool, in bytes
     */
    ByteArrayPool(int sizeLimit)
    {
        mSizeLimit = sizeLimit;
    }

    /**
     * Returns a buffer from the pool if one is available in the requested size,
     * or allocates a new
     * one if a pooled one is not available.
     * 
     * @param len
     *            the minimum size, in bytes, of the requested buffer. The
     *            returned buffer may be
     *            larger.
     * @return a byte[] buffer is always returned.
     */
    synchronized byte[] getBuf(int len)
    {
        for (int i = 0; i < mBuffersBySize.size(); i++) {
            byte[] buf = mBuffersBySize.get(i);
            if (buf.length >= len) {
                mCurrentSize -= buf.length;
                mBuffersBySize.remove(i);
                mBuffersByLastUse.remove(buf);
                return buf;
            }
        }
        return new byte[len];
    }

    /**
     * Returns a buffer to the pool, throwing away old buffers if the pool would
     * exceed its allotted
     * size.
     * 
     * @param buf
     *            the buffer to return to the pool.
     */
    public synchronized void returnBuf(@Nullable byte[] buf)
    {
        if (buf == null || buf.length > mSizeLimit) {
            return;
        }
        mBuffersByLastUse.add(buf);
        int pos = Collections.binarySearch(mBuffersBySize, buf, BUF_COMPARATOR);
        if (pos < 0) {
            pos = -pos - 1;
        }
        mBuffersBySize.add(pos, buf);
        mCurrentSize += buf.length;
        trim();
    }

    /**
     * Removes buffers from the pool until it is under its size limit.
     */
    private synchronized void trim()
    {
        while (mCurrentSize > mSizeLimit) {
            byte[] buf = mBuffersByLastUse.remove(0);
            mBuffersBySize.remove(buf);
            mCurrentSize -= buf.length;
        }
    }

}
