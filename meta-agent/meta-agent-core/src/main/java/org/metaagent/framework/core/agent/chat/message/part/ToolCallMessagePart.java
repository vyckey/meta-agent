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
 * ToolCallMessagePart represents a tool call message part in a chat message.
 *
 * @author vyckey
 * @see MessagePart
 */
public record ToolCallMessagePart(
        @JsonDeserialize(as = MessagePartIdValue.class)
        MessagePartId id,

        ToolCall toolCall,

        Instant createdAt,

        Instant updatedAt,

        @JsonDeserialize(as = MapMetadataProvider.class)
        MetadataProvider metadata
) implements MessagePart {
    public static final String TYPE = "tool_call";

    public ToolCallMessagePart {
        id = id != null ? id : MessagePartId.next();
        Objects.requireNonNull(toolCall, "Tool call cannot be null");
        createdAt = createdAt != null ? createdAt : Instant.now();
        updatedAt = updatedAt != null ? updatedAt : Instant.now();
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
    public String content() {
        return "ToolCall[" + toolCall.id() + "] " + toolCall.name() + ": " + toolCall.arguments();
    }

    public record ToolCall(String id, String type, String name, String arguments) {
    }


    public static class Builder extends AbstractMessagePartBuilder<Builder> {
        private ToolCall toolCall;

        @Override
        protected Builder self() {
            return this;
        }

        public Builder toolCall(ToolCall toolCall) {
            this.toolCall = toolCall;
            return self();
        }

        public ToolCallMessagePart build() {
            return new ToolCallMessagePart(id, toolCall, createdAt, updatedAt, metadata);
        }
    }
}
