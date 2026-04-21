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

/**
 * LlmStartMessagePart represents a message part that indicates the start of an LLM process.
 *
 * @author vyckey
 * @see MessagePart
 */
public record LlmStartMessagePart(
        @JsonDeserialize(as = MessagePartIdValue.class)
        MessagePartId id,

        Instant createdAt,

        @JsonDeserialize(as = MapMetadataProvider.class)
        MetadataProvider metadata
) implements MessagePart {
    public static final String TYPE = "llm_start";

    public LlmStartMessagePart {
        id = id != null ? id : MessagePartId.next();
        createdAt = createdAt != null ? createdAt : Instant.now();
        metadata = metadata != null ? metadata : MetadataProvider.empty();
    }

    public LlmStartMessagePart(MetadataProvider metadata) {
        this(MessagePartId.next(), Instant.now(), metadata);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public Instant updatedAt() {
        return createdAt;
    }

    @Override
    public String content() {
        return "";
    }


    public static class Builder extends AbstractMessagePartBuilder<Builder> {
        @Override
        protected Builder self() {
            return this;
        }

        public LlmStartMessagePart build() {
            return new LlmStartMessagePart(id, createdAt, metadata);
        }
    }
}
