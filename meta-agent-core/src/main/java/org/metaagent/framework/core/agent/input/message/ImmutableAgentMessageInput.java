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

package org.metaagent.framework.core.agent.input.message;

import lombok.Getter;
import org.metaagent.framework.core.agent.AgentExecutionContext;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.input.AbstractAgentInput;

import java.util.List;
import java.util.Objects;

/**
 * description is here
 *
 * @author vyckey
 */
@Getter
public class ImmutableAgentMessageInput extends AbstractAgentInput implements AgentMessageInput {
    private final String topic;
    private final List<Message> messages;

    public ImmutableAgentMessageInput(String topic, List<Message> messages) {
        this.topic = topic;
        this.messages = Objects.requireNonNull(messages, "messages is required");
    }

    public static ImmutableAgentMessageInput.Builder builder() {
        return new ImmutableAgentMessageInput.Builder();
    }


    public static class Builder {
        private AgentExecutionContext context;
        private List<Message> messages;
        private String topic;

        public Builder context(AgentExecutionContext context) {
            this.context = context;
            return this;
        }

        public Builder messages(List<Message> messages) {
            this.messages = messages;
            return this;
        }

        public Builder topic(String topic) {
            this.topic = topic;
            return this;
        }

        public AgentMessageInput build() {
            ImmutableAgentMessageInput messageInput = new ImmutableAgentMessageInput(topic, messages);
            if (context != null) {
                messageInput.context = context;
            }
            return messageInput;
        }
    }
}
