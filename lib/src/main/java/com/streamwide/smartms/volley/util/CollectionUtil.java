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

import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class CollectionUtil {

    /**
     * private constructor to hide the implicit public one.
     */
    private CollectionUtil()
    {
        // do nothing...
    }


    @Nullable
    public static <E> LinkedList<E> copyLinkedList(@Nullable LinkedList<E> sourceList)
    {
        return sourceList != null ? new LinkedList<>(sourceList) : null;
    }



    @NonNull
    public static <E> BlockingQueue<E> copyBlockingQueue(@Nullable BlockingQueue<E> original)
    {
        if(original == null) {

            return null;
        }

     return new BlockingQueue<E>() {
            @Override
            public boolean add(E e) {
                return original.add(e);
            }

            @Override
            public boolean offer(E e) {
                return original.offer(e);
            }

            @Override
            public E remove() {
                return original.remove();
            }

            @Nullable
            @Override
            public E poll() {
                return original.poll();
            }

            @Override
            public E element() {
                return original.element();
            }

            @Nullable
            @Override
            public E peek() {
                return original.peek();
            }

            @Override
            public void put(@NonNull E e) throws InterruptedException {
                    original.put(e);
            }

            @Override
            public boolean offer(E e, long timeout,@NonNull TimeUnit unit) throws InterruptedException {
                return original.offer(e,timeout,unit);
            }

            @NonNull
            @Override
            public E take() throws InterruptedException {
                return original.take();
            }

            @Override
            public E poll(long timeout,@NonNull TimeUnit unit) throws InterruptedException {
                return original.poll(timeout, unit);
            }

            @Override
            public int remainingCapacity() {
                return original.remainingCapacity();
            }

            @Override
            public boolean remove(Object o) {
                return original.remove(o);
            }

            @Override
            public boolean containsAll(@NonNull Collection<?> c) {
                return original.containsAll(c);
            }

            @Override
            public boolean addAll(@NonNull Collection<? extends E> c) {
                return original.addAll(c);
            }

            @Override
            public boolean removeAll(@NonNull Collection<?> c) {
                return original.removeAll(c);
            }

            @Override
            public boolean retainAll(@NonNull Collection<?> c) {
                return original.retainAll(c);
            }

            @Override
            public void clear() {
                original.clear();
            }

            @Override
            public boolean equals(@Nullable Object o) {
                return original.equals(o);
            }

            @Override
            public int hashCode() {
                return original.hashCode();
            }

            @Override
            public int size() {
                return original.size();
            }

            @Override
            public boolean isEmpty() {
                return original.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                return original.contains(o);
            }

            @NonNull
            @Override
            public Iterator<E> iterator() {
                return original.iterator();
            }

            @NonNull
            @Override
            public Object[] toArray() {
                return original.toArray();
            }

            @NonNull
            @Override
            public <T> T[] toArray(@NonNull T[] a) {
                return original.toArray(a);
            }

            @Override
            public int drainTo(@NonNull Collection<? super E> c) {
                return original.drainTo(c);
            }

            @Override
            public int drainTo(@NonNull Collection<? super E> c, int maxElements) {
                return original.drainTo(c,maxElements);
            }
        };

    }

}
