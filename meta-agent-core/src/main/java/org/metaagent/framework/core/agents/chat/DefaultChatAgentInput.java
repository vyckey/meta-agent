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

package org.metaagent.framework.core.agents.chat;

import org.apache.commons.collections.CollectionUtils;
import org.metaagent.framework.core.agent.AgentExecutionContext;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.input.AbstractAgentInput;
import org.metaagent.framework.core.common.metadata.MapMetadataProvider;
import org.metaagent.framework.core.common.metadata.MetadataProvider;

import java.util.List;
import java.util.Objects;

/**
 * Default implementation of {@link ChatAgentInput}.
 *
 * @author vyckey
 */
public record DefaultChatAgentInput(
        AgentExecutionContext context,
        List<Message> messages,
        MetadataProvider metadata) implements ChatAgentInput {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractAgentInput.Builder<ChatAgentInput.Builder>
            implements ChatAgentInput.Builder {
        private List<Message> messages;
        private MetadataProvider metadata = new MapMetadataProvider();

        @Override
        protected Builder self() {
            return this;
        }

        public Builder messages(List<Message> messages) {
            this.messages = Objects.requireNonNull(messages, "messages is required");
            return this;
        }

        @Override
        public Builder messages(Message... messages) {
            this.messages = List.of(messages);
            return this;
        }

        @Override
        public ChatAgentInput.Builder withOption(String key, Object value) {
            if (metadata == null) {
                metadata = MetadataProvider.create();
            }
            metadata.setProperty(key, value);
            return this;
        }

        @Override
        public DefaultChatAgentInput build() {
            if (context == null) {
                context = AgentExecutionContext.create();
            }
            if (metadata == null) {
                metadata = MetadataProvider.empty();
            }
            if (CollectionUtils.isEmpty(messages)) {
                throw new IllegalArgumentException("At least one message needs to be provided");
            }
            return new DefaultChatAgentInput(context, messages, metadata);
        }
    }

}
