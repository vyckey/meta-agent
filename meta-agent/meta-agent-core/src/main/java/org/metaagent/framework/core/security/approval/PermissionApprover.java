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

/**
 * Permission Approver.
 *
 * @param <T> the type of permission request
 * @author vyckey
 */
@FunctionalInterface
public interface PermissionApprover<T extends PermissionRequest> {
    /**
     * Create a permission approver that always approves the given permission request.
     *
     * @param <T> the type of permission request
     * @return the permission approver
     */
    static <T extends PermissionRequest> PermissionApprover<T> alwaysApproved() {
        return request -> PermissionApprovalResult.builder()
                .approvalStatus(ApprovalStatus.APPROVED).content("No need approver")
                .build();
    }

    /**
     * Approve the given permission request.
     *
     * @param request the permission request
     * @return the permission approval
     */
    PermissionApproval approve(T request);
}
