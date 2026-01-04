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

package org.metaagent.framework.core.security.approval.event;

import org.metaagent.framework.core.security.approval.PermissionRequest;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Default implementation of {@link ApprovalEventPublisher}.
 *
 * @author vyckey
 */
public class DefaultApprovalEventPublisher<T extends PermissionRequest> implements ApprovalEventPublisher<T> {
    private final Consumer<PermissionApprovalEvent<T>> listener;
    private final Executor threadPool;

    public DefaultApprovalEventPublisher(Consumer<PermissionApprovalEvent<T>> listener, Executor threadPool) {
        this.listener = Objects.requireNonNull(listener, "listener is required");
        this.threadPool = threadPool;
    }

    @Override
    public boolean publish(PermissionApprovalEvent<T> approval) {
        if (threadPool != null) {
            threadPool.execute(() -> listener.accept(approval));
        } else {
            listener.accept(approval);
        }
        return true;
    }
}
