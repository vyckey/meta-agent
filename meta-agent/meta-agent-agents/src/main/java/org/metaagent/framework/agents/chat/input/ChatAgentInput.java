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

package org.metaagent.framework.agents.chat.input;

import com.google.common.base.Preconditions;
import org.apache.commons.collections.CollectionUtils;
import org.metaagent.framework.agents.chat.context.ChatAgentContext;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.session.SessionId;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.model.ModelId;
import org.metaagent.framework.core.model.prompt.PromptValue;

import java.util.List;
import java.util.Objects;

/**
 * Default implementation of {@link ChatAgentInput}.
 *
 * @author vyckey
 */
public record ChatAgentInput(
        ModelId modelId,
        List<PromptValue> systemPrompts,
        SessionId sessionId,
        List<Message> messages,
        Boolean thinking,
        ChatAgentContext context
) implements AgentInput {

    public ChatAgentInput {
        Objects.requireNonNull(messages, "messages is required");
        Preconditions.checkArgument(modelId != null, "modelId cannot be null");
        systemPrompts = systemPrompts != null ? systemPrompts : List.of();
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(messages), "messages cannot be empty");
        Preconditions.checkArgument(context != null, "context cannot be null");
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }


    public static class Builder {
        private ModelId modelId;
        private List<PromptValue> systemPrompts;
        private SessionId sessionId;
        private List<Message> messages;
        private Boolean thinking;
        private ChatAgentContext context;

        private Builder() {
        }

        public Builder(ChatAgentInput input) {
            this.modelId = input.modelId();
            this.systemPrompts = input.systemPrompts();
            this.sessionId = input.sessionId();
            this.messages = input.messages();
            this.thinking = input.thinking();
            this.context = input.context();
        }

        public Builder modelId(ModelId modelId) {
            this.modelId = modelId;
            return this;
        }

        public Builder systemPrompts(List<PromptValue> systemPrompts) {
            this.systemPrompts = systemPrompts;
            return this;
        }

        public Builder systemPrompts(PromptValue... systemPrompts) {
            this.systemPrompts = List.of(systemPrompts);
            return this;
        }

        public Builder sessionId(SessionId sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder message(Message message) {
            this.messages = List.of(message);
            return this;
        }

        public Builder messages(List<Message> messages) {
            this.messages = messages;
            return this;
        }

        public Builder thinking(boolean thinking) {
            this.thinking = thinking;
            return this;
        }

        public Builder context(ChatAgentContext context) {
            this.context = context;
            return this;
        }

        public ChatAgentInput build() {
            return new ChatAgentInput(
                    modelId,
                    systemPrompts,
                    sessionId,
                    messages,
                    thinking,
                    context
            );
        }
    }

}
