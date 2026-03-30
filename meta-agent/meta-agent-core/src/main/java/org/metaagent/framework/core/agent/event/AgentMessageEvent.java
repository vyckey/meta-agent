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

package org.metaagent.framework.core.agent.event;

import org.metaagent.framework.common.metadata.MetadataProvider;
import org.metaagent.framework.core.agent.MetaAgent;
import org.metaagent.framework.core.agent.chat.message.MessageInfo;
import org.metaagent.framework.core.agent.chat.message.part.MessagePart;

import java.time.Instant;
import java.util.Objects;

/**
 * AgentMessageEvent is an event that occurs when an agent sends a message.
 *
 * @author vyckey
 */
public record AgentMessageEvent(
        MetaAgent<?, ?> agent,
        MessageInfo messageInfo,
        MessagePart messagePart,
        MetadataProvider metadata,
        Instant occurredTime
) implements AgentEvent {
    public AgentMessageEvent {
        Objects.requireNonNull(agent, "agent is required");
        Objects.requireNonNull(messageInfo, "messageInfo is required");
        Objects.requireNonNull(messagePart, "messagePart is required");
        if (metadata == null) {
            metadata = MetadataProvider.empty();
        }
        if (occurredTime == null) {
            occurredTime = Instant.now();
        }
    }

    public static Builder builder() {
        return new Builder();
    }


    public static class Builder {
        private MetaAgent<?, ?> agent;
        private MessageInfo messageInfo;
        private MessagePart messagePart;
        private MetadataProvider metadata;
        private Instant occurredTime;

        public Builder agent(MetaAgent<?, ?> agent) {
            this.agent = agent;
            return this;
        }

        public Builder messageInfo(MessageInfo messageInfo) {
            this.messageInfo = messageInfo;
            return this;
        }

        public Builder messagePart(MessagePart messagePart) {
            this.messagePart = messagePart;
            return this;
        }

        public Builder metadata(MetadataProvider metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder occurredTime(Instant occurredTime) {
            this.occurredTime = occurredTime;
            return this;
        }

        public AgentMessageEvent build() {
            return new AgentMessageEvent(agent, messageInfo, messagePart, metadata, occurredTime);
        }
    }
}
