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

package org.metaagent.framework.core.agents.chat.output;

import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agents.chat.ChatAgent;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * {@link ChatAgent} output.
 *
 * @author vyckey
 */
public class DefaultChatOutput implements ChatOutput {
    private final List<Message> messages;
    private final String thought;

    public DefaultChatOutput(Builder builder) {
        this.messages = Objects.requireNonNull(builder.messages, "messages is required");
        this.thought = builder.thought;
    }

    public DefaultChatOutput(List<Message> messages) {
        this.messages = Objects.requireNonNull(messages, "messages is required");
        this.thought = null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<Message> messages() {
        return messages;
    }

    public String thought() {
        return thought;
    }

    @Override
    public String toString() {
        return messages().stream().map(Message::toString).collect(Collectors.joining("\n"));
    }

    public static class Builder {
        private List<Message> messages;
        private String thought;

        private Builder() {
        }

        public Builder messages(List<Message> messages) {
            this.messages = messages;
            return this;
        }

        public Builder thought(String thought) {
            this.thought = thought;
            return this;
        }

        public DefaultChatOutput build() {
            return new DefaultChatOutput(this);
        }
    }

}
