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

package org.metaagent.framework.core.agent.chat.message;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.common.metadata.MapMetadataProvider;
import org.metaagent.framework.common.metadata.MetadataProvider;

import java.time.Instant;

/**
 * RoleMessage is a message that contains role, content and media resources.
 *
 * @author vyckey
 */
public record RoleMessageInfo(
        @JsonDeserialize(as = MessageIdValue.class)
        MessageId id,

        String role,

        Instant createdAt,

        Instant updatedAt,

        @JsonDeserialize(as = MapMetadataProvider.class)
        MetadataProvider metadata
) implements MessageInfo {

    public RoleMessageInfo {
        if (StringUtils.isEmpty(role)) {
            throw new IllegalArgumentException("role is required");
        }
        id = id != null ? id : MessageId.next();
        createdAt = createdAt != null ? createdAt : Instant.now();
        updatedAt = updatedAt != null ? updatedAt : Instant.now();
        metadata = metadata != null ? metadata : MetadataProvider.empty();
    }

    public RoleMessageInfo(MessageId id, String role, MetadataProvider metadata) {
        this(id, role, Instant.now(), Instant.now(), metadata);
    }

    public RoleMessageInfo(MessageId id, String role) {
        this(id, role, MetadataProvider.empty());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder user() {
        return builder().role(MessageInfo.ROLE_USER);
    }

    public static Builder assistant() {
        return builder().role(MessageInfo.ROLE_ASSISTANT);
    }

    public static Builder builder(MessageInfo message) {
        return new Builder(message);
    }

    @Override
    public Builder toBuilder() {
        return new Builder(this);
    }


    public static class Builder implements MessageInfo.Builder {
        private MessageId id;
        private String role;
        private Instant createdAt;
        private Instant updatedAt;
        private MetadataProvider metadata;

        public Builder() {
        }

        public Builder(MessageInfo messageInfo) {
            this.id = messageInfo.id();
            this.role = messageInfo.role();
            this.createdAt = messageInfo.createdAt();
            this.updatedAt = messageInfo.updatedAt();
            this.metadata = messageInfo.metadata();
        }

        @Override
        public Builder id(MessageId id) {
            this.id = id;
            return this;
        }

        @Override
        public Builder role(String role) {
            this.role = role;
            return this;
        }

        @Override
        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        @Override
        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        @Override
        public Builder metadata(MetadataProvider metadata) {
            this.metadata = metadata;
            return this;
        }

        public RoleMessageInfo build() {
            return new RoleMessageInfo(id, role, createdAt, updatedAt, metadata);
        }
    }
}
