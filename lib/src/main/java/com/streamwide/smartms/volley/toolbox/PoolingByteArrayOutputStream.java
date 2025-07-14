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

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * A variation of {@link ByteArrayOutputStream} that uses a pool of
 * byte[] buffers instead
 * of always allocating them fresh, saving on heap churn.
 */
public class PoolingByteArrayOutputStream extends ByteArrayOutputStream {

    /**
     * If the {@link #PoolingByteArrayOutputStream(ByteArrayPool)} constructor
     * is called, this is
     * the default size to which the underlying byte array is initialized.
     */
    private static final int DEFAULT_SIZE = 256;

    private final ByteArrayPool mPool;

    /**
     * Constructs a new PoolingByteArrayOutputStream with a default size. If
     * more bytes are written
     * to this instance, the underlying byte array will expand.
     */
    public PoolingByteArrayOutputStream(@NonNull ByteArrayPool pool)
    {
        this(pool, DEFAULT_SIZE);
    }

    /**
     * Constructs a new {@code ByteArrayOutputStream} with a default size of
     * {@code size} bytes. If
     * more than {@code size} bytes are written to this instance, the underlying
     * byte array will
     * expand.
     * 
     * @param size
     *            initial size for the underlying byte array. The value will be
     *            pinned to a default
     *            minimum size.
     */
    public PoolingByteArrayOutputStream(@NonNull ByteArrayPool pool, int size)
    {
        mPool = pool;
        buf = mPool.getBuf(Math.max(size, DEFAULT_SIZE));
    }

    @Override
    public void close() throws IOException
    {
        mPool.returnBuf(buf);
        buf = null;
        super.close();
    }

    @Override
    public void finalize()
    {
        mPool.returnBuf(buf);
    }

    /**
     * Ensures there is enough space in the buffer for the given number of
     * additional bytes.
     */
    private void expand(int i)
    {
        /* Can the buffer handle @i more bytes, if not expand it */
        if (count + i <= buf.length) {
            return;
        }
        byte[] newbuf = mPool.getBuf((count + i) * 2);
        System.arraycopy(buf, 0, newbuf, 0, count);
        mPool.returnBuf(buf);
        buf = newbuf;
    }

    @Override
    public synchronized void write(@NonNull byte[] buffer, int offset, int len)
    {
        expand(len);
        super.write(buffer, offset, len);
    }

    @Override
    public synchronized void write(int oneByte)
    {
        expand(1);
        super.write(oneByte);
    }
}
