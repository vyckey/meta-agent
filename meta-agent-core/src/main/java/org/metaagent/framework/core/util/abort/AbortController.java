/*
 * MIT License
 *
 * Copyright (c) 2025 MetaAgent
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

package org.metaagent.framework.core.util.abort;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AbortController represents an object that allows you to abort agent or tool executions as and when desired.
 *
 * @author vyckey
 * @see AbortSignal
 */
public class AbortController {
    private static final AbortController GLOBAL = new AbortController();
    private final AbortSignalImpl abortSignal = new AbortSignalImpl();

    public static AbortController global() {
        return GLOBAL;
    }

    /**
     * Returns an AbortSignal object.
     *
     * @return an AbortSignal object
     */
    public AbortSignal signal() {
        return abortSignal;
    }

    /**
     * Aborts the agent or tool execution.
     */
    public void abort() {
        abort("Aborted");
    }

    /**
     * Aborts the agent or tool execution.
     */
    public void abort(String reason) {
        abortSignal.abort(new AbortException(reason));
    }

    /**
     * Aborts the agent or tool execution with a specific reason.
     *
     * @param reason the reason for aborting
     */
    public void abort(Throwable reason) {
        abortSignal.abort(new AbortException(reason));
    }

    static class AbortSignalImpl implements AbortSignal {
        final AtomicBoolean aborted = new AtomicBoolean(false);
        final List<AbortListener> listeners = new CopyOnWriteArrayList<>();
        volatile Throwable abortReason;

        @Override
        public boolean isAborted() {
            return aborted.get();
        }

        @Override
        public Throwable abortReason() {
            return abortReason;
        }

        @Override
        public void addAbortListener(AbortListener listener) {
            listeners.add(listener);
        }

        void abort(Throwable cause) {
            if (aborted.compareAndSet(false, true)) {
                abortReason = cause;
                for (AbortListener listener : listeners) {
                    listener.onAbort(this);
                }
                listeners.clear();
            }
        }
    }
}
