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

package org.metaagent.framework.core.agent.chat.message.part;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.metaagent.framework.common.metadata.MapMetadataProvider;
import org.metaagent.framework.common.metadata.MetadataProvider;

import java.time.Instant;
import java.util.Objects;

/**
 * Text message part
 *
 * @author vyckey
 * @see MessagePart
 */
public record TextMessagePart(
        @JsonDeserialize(as = MessagePartIdValue.class)
        MessagePartId id,

        String subType,

        String text,

        Instant createdAt,

        Instant updatedAt,

        @JsonDeserialize(as = MapMetadataProvider.class)
        MetadataProvider metadata
) implements MessagePart {
    public static final String TYPE = "text";
    public static final String SUB_TYPE_REASONING = "reasoning";

    public TextMessagePart {
        id = id != null ? id : MessagePartId.next();
        Objects.requireNonNull(text, "text is required");
        createdAt = createdAt != null ? createdAt : Instant.now();
        updatedAt = updatedAt != null ? updatedAt : Instant.now();
        metadata = metadata != null ? metadata : MetadataProvider.empty();
    }

    public TextMessagePart(String text, MetadataProvider metadata) {
        this(MessagePartId.next(), "", text, Instant.now(), Instant.now(), metadata);
    }

    public TextMessagePart(String text) {
        this(text, MetadataProvider.empty());
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


    public static class Builder extends AbstractMessagePartBuilder<Builder> {
        private String subType;
        private String text;

        @Override
        protected Builder self() {
            return this;
        }

        public Builder subType(String subType) {
            this.subType = subType;
            return self();
        }

        public Builder text(String text) {
            this.text = text;
            return self();
        }

        public TextMessagePart build() {
            return new TextMessagePart(id, subType, text, createdAt, updatedAt, metadata);
        }
    }
}
