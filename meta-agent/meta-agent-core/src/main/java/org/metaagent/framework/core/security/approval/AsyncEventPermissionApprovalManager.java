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

package org.metaagent.framework.core.security.approval;

import org.metaagent.framework.core.security.approval.event.ApprovalEventPublisher;
import org.metaagent.framework.core.security.approval.event.DefaultApprovalEventPublisher;
import org.metaagent.framework.core.security.approval.event.PermissionApprovalEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * AsyncEventPermissionApprovalManager is an implementation of PermissionApprovalManager that uses an
 * ApprovalEventPublisher to publish events and a CompletableFuture to track the approval process.
 *
 * @param <T> the type of permission request
 * @author vyckey
 */
public class AsyncEventPermissionApprovalManager<T extends PermissionRequest>
        implements PermissionApprovalManager<T>, AutoCloseable {
    private final Lock lock = new ReentrantLock();
    private final Map<String, CompletableFuture<PermissionApproval>> pending = new HashMap<>();
    private final ApprovalEventPublisher<T> eventPublisher;

    public AsyncEventPermissionApprovalManager(ApprovalEventPublisher<T> eventPublisher) {
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher is required");
    }

    public static <T extends PermissionRequest> AsyncEventPermissionApprovalManager<T> assign(
            PermissionApprover<T> approver, Executor threadPoll) {
        AsyncEventPermissionApprovalManager<T>[] managerHolder = new AsyncEventPermissionApprovalManager[1];
        ApprovalEventPublisher<T> eventPublisher = new DefaultApprovalEventPublisher<>(
                event -> {
                    T request = event.request();
                    PermissionApproval approval = approver.approve(request);
                    managerHolder[0].completeApproval(request.getId(), approval);
                }, threadPoll
        );
        AsyncEventPermissionApprovalManager<T> approvalManager = new AsyncEventPermissionApprovalManager<>(eventPublisher);
        managerHolder[0] = approvalManager;
        return approvalManager;
    }

    @Override
    public CompletableFuture<PermissionApproval> initiateApproval(T request) {
        lock.lock();
        try {
            eventPublisher.publish(new PermissionApprovalEvent<>(request));

            CompletableFuture<PermissionApproval> future = new CompletableFuture<>();
            pending.put(request.getId(), future);
            return future;
        } finally {
            lock.unlock();
        }
    }

    public void completeApproval(String requestId, PermissionApproval result) {
        lock.lock();
        try {
            CompletableFuture<PermissionApproval> f = pending.remove(requestId);
            if (f == null) {
                // Timeout or duplicate callback, ignore
                return;
            }
            // Fill the future with the result
            f.complete(result);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean cancelApproval(String requestId) {
        lock.lock();
        try {
            CompletableFuture<PermissionApproval> f = pending.remove(requestId);
            if (f != null) {
                f.cancel(false);
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() {
        lock.lock();
        try {
            pending.values().forEach(f -> f.cancel(false));
            pending.clear();
        } finally {
            lock.unlock();
        }
    }
}