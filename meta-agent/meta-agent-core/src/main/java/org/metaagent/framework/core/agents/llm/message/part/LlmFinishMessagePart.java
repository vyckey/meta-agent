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
import org.metaagent.framework.core.model.chat.metadata.TokenUsage;

import java.time.Instant;
import java.util.Objects;

/**
 * LlmFinishMessagePart represents a message part that indicates the end of a LLM model's response.
 *
 * @author vyckey
 * @see MessagePart
 */
public record LlmFinishMessagePart(
        @JsonDeserialize(as = MessagePartIdValue.class)
        MessagePartId id,

        String finishReason,

        TokenUsage tokenUsage,

        Instant createdAt,

        @JsonDeserialize(as = MapMetadataProvider.class)
        MetadataProvider metadata
) implements MessagePart {
    public static final String TYPE = "llm_finish";

    public LlmFinishMessagePart {
        id = id != null ? id : MessagePartId.next();
        Objects.requireNonNull(finishReason, "finishReason cannot be null");
        Objects.requireNonNull(tokenUsage, "tokenUsage cannot be null");
        createdAt = createdAt != null ? createdAt : Instant.now();
        metadata = metadata != null ? metadata : MetadataProvider.empty();
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
        private String finishReason;
        private TokenUsage tokenUsage;

        @Override
        protected Builder self() {
            return this;
        }

        public Builder finishReason(String finishReason) {
            this.finishReason = finishReason;
            return this;
        }

        public Builder tokenUsage(TokenUsage tokenUsage) {
            this.tokenUsage = tokenUsage;
            return this;
        }

        public LlmFinishMessagePart build() {
            return new LlmFinishMessagePart(id, finishReason, tokenUsage, createdAt, metadata);
        }
    }
}
