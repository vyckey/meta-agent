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

import org.metaagent.framework.core.agent.AgentExecutionContext;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.input.AbstractAgentInput;
import org.metaagent.framework.core.common.metadata.MapMetadataProvider;
import org.metaagent.framework.core.common.metadata.MetadataProvider;

import java.util.List;
import java.util.Objects;

/**
 * Default implementation of {@link AgentChatInput}.
 *
 * @author vyckey
 */
public record DefaultAgentChatInput(
        AgentExecutionContext context,
        List<Message> messages,
        MetadataProvider metadata) implements AgentChatInput {

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public AgentExecutionContext getContext() {
        return context;
    }

    public static class Builder extends AbstractAgentInput.Builder<Builder> {
        private AgentExecutionContext context;
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

        public Builder messages(Message... messages) {
            this.messages = List.of(messages);
            return this;
        }

        public Builder deepThinkEnabled(boolean deepThinkEnabled) {
            this.metadata.setProperty(AgentChatInput.OPTION_DEEP_THINK_ENABLED, deepThinkEnabled);
            return this;
        }

        public Builder searchEnabled(boolean searchEnabled) {
            this.metadata.setProperty(AgentChatInput.OPTION_SEARCH_ENABLED, searchEnabled);
            return this;
        }

        @Override
        public DefaultAgentChatInput build() {
            if (context == null) {
                context = AgentExecutionContext.create();
            }
            if (metadata == null) {
                metadata = new MapMetadataProvider();
            }
            if (messages == null) {
                messages = List.of();
            }
            return new DefaultAgentChatInput(context, messages, metadata);
        }
    }

}
