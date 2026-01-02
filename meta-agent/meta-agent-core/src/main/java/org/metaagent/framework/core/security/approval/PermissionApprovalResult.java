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

import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.common.metadata.MetadataProvider;

import java.util.Objects;

public record PermissionApprovalResult(
        ApprovalStatus approvalStatus,
        String content,
        MetadataProvider metadata) implements PermissionApproval {
    public PermissionApprovalResult(ApprovalStatus approvalStatus, String content, MetadataProvider metadata) {
        this.approvalStatus = Objects.requireNonNull(approvalStatus, "approvalStatus is required");
        this.content = Objects.requireNonNull(content, "content is required");
        this.metadata = Objects.requireNonNull(metadata, "metadata is required");
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public ApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public MetadataProvider getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        if (StringUtils.isNotEmpty(content)) {
            return approvalStatus.name() + ": " + content;
        }
        return approvalStatus.name();
    }

    public static class Builder {
        private ApprovalStatus approvalStatus;
        private String content;
        private MetadataProvider metadata;

        public Builder approvalStatus(ApprovalStatus approvalStatus) {
            this.approvalStatus = approvalStatus;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder metadata(MetadataProvider metadata) {
            this.metadata = metadata;
            return this;
        }

        public PermissionApprovalResult build() {
            if (content == null) {
                content = "";
            }
            if (metadata == null) {
                metadata = MetadataProvider.empty();
            }
            return new PermissionApprovalResult(approvalStatus, content, metadata);
        }
    }
}
