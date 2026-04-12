/*
 * MIT License
 *
 * Copyright (c) 2026 MetaAgent
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.metaagent.framework.common.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An iterator for blocking queue. This iterator will block when there is no element in the queue,
 * and will return the next element when it is available. The iterator will end when the markEnd()
 * method is called and there are no more elements in the queue.
 *
 * @param <E> the type of elements
 * @author vyckey
 */
public class BlockingQueueIterator<E> implements Iterator<E>, AutoCloseable {
    private final BlockingQueue<E> blockingQueue;
    private final AtomicBoolean hasMoreElements = new AtomicBoolean(true);
    private E nextElement;
    private final long pollTimeoutMillis;

    public BlockingQueueIterator(BlockingQueue<E> blockingQueue, long pollTimeoutMillis) {
        if (pollTimeoutMillis <= 0) {
            throw new IllegalArgumentException("pollTimeoutMillis must be positive");
        }
        this.blockingQueue = Objects.requireNonNull(blockingQueue, "blockingQueue cannot be null");
        this.pollTimeoutMillis = pollTimeoutMillis;
    }

    public BlockingQueueIterator(long pollTimeoutMillis) {
        this(new LinkedBlockingQueue<>(), pollTimeoutMillis);
    }

    public boolean offer(E element) {
        if (element == null) {
            throw new IllegalArgumentException("element cannot be null");
        }
        if (!hasMoreElements.get()) {
            throw new IllegalStateException("No more elements can be added");
        }
        return blockingQueue.offer(element);
    }

    public void markEnd() {
        hasMoreElements.set(false);
    }

    @Override
    public boolean hasNext() {
        while (nextElement == null && hasMoreElements.get()) {
            try {
                E polled = blockingQueue.poll(pollTimeoutMillis, TimeUnit.MILLISECONDS);
                if (polled != null) {
                    nextElement = polled;
                    return true;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                hasMoreElements.set(false);
                return false;
            }
        }

        if (nextElement == null && !blockingQueue.isEmpty()) {
            nextElement = blockingQueue.poll();
        }
        return nextElement != null;
    }

    @Override
    public E next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        E element = nextElement;
        nextElement = null;
        return element;
    }

    @Override
    public void close() {
        hasMoreElements.set(false);
        blockingQueue.clear();
    }
}
