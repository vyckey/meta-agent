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
import java.util.UUID;

/**
 * PermissionApprovalEvent represents an event of a permission approval request.
 *
 * @author vyckey
 */
public record PermissionApprovalEvent<T extends PermissionRequest>(
        String eventId,
        T request,
        long timestamp) {
    public PermissionApprovalEvent(String eventId, T request, long timestamp) {
        this.eventId = Objects.requireNonNull(eventId, "eventId is required");
        this.request = Objects.requireNonNull(request, "approval request is required");
        this.timestamp = timestamp;
    }

    public PermissionApprovalEvent(T request) {
        this(UUID.randomUUID().toString(), request, System.currentTimeMillis());
    }
}
