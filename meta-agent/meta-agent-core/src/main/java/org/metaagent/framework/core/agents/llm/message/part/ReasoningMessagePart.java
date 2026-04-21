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

package org.metaagent.framework.core.agents.llm.message.part;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.metaagent.framework.common.metadata.MapMetadataProvider;
import org.metaagent.framework.common.metadata.MetadataProvider;
import org.metaagent.framework.core.agent.chat.message.part.AbstractMessagePartBuilder;
import org.metaagent.framework.core.agent.chat.message.part.MessagePart;
import org.metaagent.framework.core.agent.chat.message.part.MessagePartId;
import org.metaagent.framework.core.agent.chat.message.part.MessagePartIdValue;

import java.time.Instant;
import java.util.Objects;

/**
 * ReasoningMessagePart represents a message part that contains a reasoning.
 *
 * @author vyckey
 * @see MessagePart
 */
public record ReasoningMessagePart(
        @JsonDeserialize(as = MessagePartIdValue.class)
        MessagePartId id,

        ReasoningStatus status,

        String text,

        Instant createdAt,

        Instant updatedAt,

        @JsonDeserialize(as = MapMetadataProvider.class)
        MetadataProvider metadata
) implements MessagePart {
    public static final String TYPE = "reasoning";

    public ReasoningMessagePart {
        id = id != null ? id : MessagePartId.next();
        Objects.requireNonNull(status, "status is required");
        if (status == ReasoningStatus.PROCESSING) {
            Objects.requireNonNull(text, "text is required");
        } else {
            text = "";
        }
        createdAt = createdAt != null ? createdAt : Instant.now();
        updatedAt = updatedAt != null ? updatedAt : Instant.now();
        metadata = metadata != null ? metadata : MetadataProvider.empty();
    }

    public ReasoningMessagePart(ReasoningStatus status, String text, MetadataProvider metadata) {
        this(MessagePartId.next(), status, text, Instant.now(), Instant.now(), metadata);
    }

    public static ReasoningMessagePart started() {
        return new ReasoningMessagePart(ReasoningStatus.STARTED, "", MetadataProvider.empty());
    }

    public static ReasoningMessagePart completed() {
        return new ReasoningMessagePart(ReasoningStatus.COMPLETED, "", MetadataProvider.empty());
    }

    public static ReasoningMessagePart text(String text, MetadataProvider metadata) {
        return new ReasoningMessagePart(ReasoningStatus.PROCESSING, text, metadata);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public String content() {
        return text;
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public enum ReasoningStatus {
        STARTED,
        PROCESSING,
        COMPLETED,
    }


    public static class Builder extends AbstractMessagePartBuilder<Builder> {
        private ReasoningStatus status;
        private String text;

        private Builder() {
        }

        public Builder(ReasoningMessagePart messagePart) {
            super(messagePart);
            this.status = messagePart.status;
            this.text = messagePart.text;
        }

        @Override
        protected Builder self() {
            return this;
        }

        public Builder status(ReasoningStatus status) {
            this.status = status;
            return self();
        }

        public Builder started() {
            this.status = ReasoningStatus.STARTED;
            return self();
        }

        public Builder completed() {
            this.status = ReasoningStatus.COMPLETED;
            return self();
        }

        public Builder text(String text) {
            this.status = ReasoningStatus.PROCESSING;
            this.text = text;
            return self();
        }

        public ReasoningMessagePart build() {
            return new ReasoningMessagePart(id, status, text, createdAt, updatedAt, metadata);
        }
    }
}
