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

package org.metaagent.framework.core.agents.llm.message;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.common.metadata.MapMetadataProvider;
import org.metaagent.framework.common.metadata.MetadataProvider;
import org.metaagent.framework.core.agent.chat.message.part.AbstractMessagePartBuilder;
import org.metaagent.framework.core.agent.chat.message.part.MessagePart;
import org.metaagent.framework.core.agent.chat.message.part.MessagePartId;
import org.metaagent.framework.core.agent.chat.message.part.MessagePartIdValue;
import org.metaagent.framework.core.model.prompt.PromptValue;
import org.metaagent.framework.core.model.prompt.StringPromptValue;

import java.time.Instant;

/**
 * System message part
 *
 * @author vyckey
 * @see MessagePart
 */
public record SystemMessagePart(
        @JsonDeserialize(as = MessagePartIdValue.class)
        MessagePartId id,

        String text,

        Instant createdAt,

        @JsonDeserialize(as = MapMetadataProvider.class)
        MetadataProvider metadata
) implements MessagePart {
    public static final String TYPE = "system";

    public SystemMessagePart {
        id = id != null ? id : MessagePartId.next();
        Preconditions.checkArgument(StringUtils.isNotBlank(text), "text is required");
        text = text.trim();
        createdAt = createdAt != null ? createdAt : Instant.now();
        metadata = metadata != null ? metadata : MetadataProvider.empty();
    }

    public SystemMessagePart(String text, MetadataProvider metadata) {
        this(MessagePartId.next(), text, Instant.now(), metadata);
    }

    public SystemMessagePart(String text) {
        this(text, MetadataProvider.empty());
    }

    public SystemMessagePart(PromptValue systemPrompt) {
        this(systemPrompt.text(), MetadataProvider.empty());
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
        return text;
    }

    public PromptValue toPromptValue() {
        return StringPromptValue.from(text);
    }

    public Builder toBuilder() {
        return new Builder(this);
    }


    public static class Builder extends AbstractMessagePartBuilder<Builder> {
        private String text;

        private Builder() {
        }

        public Builder(SystemMessagePart messagePart) {
            super(messagePart);
            this.text = messagePart.text;
        }

        @Override
        protected Builder self() {
            return this;
        }

        public Builder text(String text) {
            this.text = text;
            return self();
        }

        public SystemMessagePart build() {
            return new SystemMessagePart(id, text, createdAt, metadata);
        }
    }
}
