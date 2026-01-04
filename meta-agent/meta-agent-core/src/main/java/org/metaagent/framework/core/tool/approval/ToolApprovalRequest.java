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

package org.metaagent.framework.core.tool.approval;

import org.metaagent.framework.common.metadata.MetadataProvider;
import org.metaagent.framework.core.security.approval.PermissionRequest;

import java.util.Objects;
import java.util.UUID;

/**
 * {@link ToolApprovalRequest} represents a request for tool approval.
 *
 * @author vyckey
 */
public record ToolApprovalRequest(
        String id,
        String toolName,
        String approvalContent,
        String arguments,
        MetadataProvider metadata
) implements PermissionRequest {

    public ToolApprovalRequest(String id, String toolName, String approvalContent, String arguments, MetadataProvider metadata) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.toolName = Objects.requireNonNull(toolName, "toolName is required");
        this.approvalContent = Objects.requireNonNull(approvalContent, "approvalContent is required");
        this.arguments = arguments;
        this.metadata = Objects.requireNonNull(metadata, "metadata is required");
    }

    public static ToolApprovalRequest requestCall(String toolName, String arguments, MetadataProvider metadata) {
        return ToolApprovalRequest.builder()
                .toolName(toolName)
                .approvalContent("Request to call tool " + toolName)
                .arguments(arguments)
                .metadata(metadata)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getContent() {
        return approvalContent;
    }

    @Override
    public MetadataProvider getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return "ToolApprovalRequest{tool=\"" + toolName + "\", content=\"" + approvalContent + "\"}";
    }

    public static class Builder {
        private String id;
        private String toolName;
        private String approvalContent;
        private String arguments;
        private MetadataProvider metadata;

        private Builder() {
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder toolName(String toolName) {
            this.toolName = toolName;
            return this;
        }

        public Builder approvalContent(String approvalContent) {
            this.approvalContent = approvalContent;
            return this;
        }

        public Builder arguments(String arguments) {
            this.arguments = arguments;
            return this;
        }

        public Builder metadata(MetadataProvider metadata) {
            this.metadata = metadata;
            return this;
        }

        public ToolApprovalRequest build() {
            if (id == null) {
                id = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16);
            }
            if (metadata == null) {
                metadata = MetadataProvider.empty();
            }
            return new ToolApprovalRequest(id, toolName, approvalContent, arguments, metadata);
        }
    }
}
