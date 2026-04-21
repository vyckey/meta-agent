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
 * CompactMessagePart represents a message part that performs compaction.
 *
 * @author vyckey
 * @see MessagePart
 */
public record CompactionMessagePart(
        @JsonDeserialize(as = MessagePartIdValue.class)
        MessagePartId id,

        String prompt,

        boolean auto,

        Boolean overflow,

        Instant createdAt,

        @JsonDeserialize(as = MapMetadataProvider.class)
        MetadataProvider metadata
) implements MessagePart {
    public static final String TYPE = "compaction";

    public CompactionMessagePart {
        id = id != null ? id : MessagePartId.next();
        prompt = prompt != null ? prompt : "";
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
        return prompt;
    }

    public Builder toBuilder() {
        return new Builder(this);
    }


    public static class Builder extends AbstractMessagePartBuilder<Builder> {
        private String prompt;
        private boolean auto;
        private Boolean overflow;

        private Builder() {
        }

        public Builder(CompactionMessagePart messagePart) {
            super(messagePart);
        }

        @Override
        protected Builder self() {
            return this;
        }

        public Builder prompt(String prompt) {
            this.prompt = prompt;
            return self();
        }

        public Builder auto(boolean auto) {
            this.auto = auto;
            return self();
        }

        public Builder overflow(Boolean overflow) {
            this.overflow = overflow;
            return self();
        }

        public CompactionMessagePart build() {
            return new CompactionMessagePart(id, prompt, auto, overflow, createdAt, metadata);
        }
    }
}
