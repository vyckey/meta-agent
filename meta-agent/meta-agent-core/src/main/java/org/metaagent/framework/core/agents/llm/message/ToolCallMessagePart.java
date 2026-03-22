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
import org.metaagent.framework.common.metadata.MapMetadataProvider;
import org.metaagent.framework.common.metadata.MetadataProvider;
import org.metaagent.framework.core.agent.chat.message.part.AbstractMessagePartBuilder;
import org.metaagent.framework.core.agent.chat.message.part.MessagePart;
import org.metaagent.framework.core.agent.chat.message.part.MessagePartId;
import org.metaagent.framework.core.agent.chat.message.part.MessagePartIdValue;

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

        String callId,

        String toolName,

        String arguments,

        String response,

        ToolCallStatus status,

        Instant createdAt,

        Instant updatedAt,

        @JsonDeserialize(as = MapMetadataProvider.class)
        MetadataProvider metadata
) implements MessagePart {
    public static final String TYPE = "tool_call";

    public ToolCallMessagePart {
        id = id != null ? id : MessagePartId.next();

        Objects.requireNonNull(callId, "tool call id cannot be null");
        Objects.requireNonNull(toolName, "tool name cannot be null");
        Objects.requireNonNull(status, "tool call status cannot be null");
        Objects.requireNonNull(arguments, "tool call arguments cannot be null");
        if (status == ToolCallStatus.END || status == ToolCallStatus.ERROR) {
            Objects.requireNonNull(response, "tool call response cannot be null");
        }

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
        return "";
    }

    @Override
    public String toString() {
        return "ToolCall[" + callId + "] " + toolName + "(" + arguments + ") => " + response;
    }

    public enum ToolCallStatus {
        START,
        END,
        ERROR
    }


    public static class Builder extends AbstractMessagePartBuilder<Builder> {
        private String callId;
        private String toolName;
        private ToolCallStatus status;
        private String arguments;
        private String response;

        @Override
        protected Builder self() {
            return this;
        }

        public Builder callId(String callId) {
            this.callId = callId;
            return self();
        }

        public Builder toolName(String toolName) {
            this.toolName = toolName;
            return self();
        }

        public Builder status(ToolCallStatus status) {
            this.status = status;
            return self();
        }

        public Builder arguments(String arguments) {
            this.arguments = arguments;
            return self();
        }

        public Builder response(String response) {
            this.response = response;
            return self();
        }

        public ToolCallMessagePart build() {
            return new ToolCallMessagePart(id, callId, toolName, arguments, response, status, createdAt, updatedAt, metadata);
        }
    }
}
