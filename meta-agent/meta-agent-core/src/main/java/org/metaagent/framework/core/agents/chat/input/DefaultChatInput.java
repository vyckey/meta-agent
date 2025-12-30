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

package org.metaagent.framework.core.agents.chat.input;

import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agents.chat.ChatAgent;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * {@link ChatAgent} input
 *
 * @author vyckey
 */
public class DefaultChatInput implements ChatInput {
    private final List<Message> messages;
    private final Boolean isThinkingEnabled;
    private final Boolean isStreamingEnabled;

    public DefaultChatInput(List<Message> messages, Boolean isThinkingEnabled, Boolean isStreamingEnabled) {
        this.messages = Objects.requireNonNull(messages, "messages is required");
        this.isThinkingEnabled = isThinkingEnabled;
        this.isStreamingEnabled = isStreamingEnabled;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public List<Message> messages() {
        return messages;
    }

    @Override
    public Boolean isThinkingEnabled() {
        return isThinkingEnabled;
    }

    @Override
    public Boolean isStreamingEnabled() {
        return isStreamingEnabled;
    }

    @Override
    public String toString() {
        return messages().stream().map(Message::getContent).collect(Collectors.joining("\n"));
    }

    public static class Builder {
        private List<Message> messages;
        private Boolean isThinkingEnabled;
        private Boolean isStreamingEnabled;

        private Builder() {
        }

        public Builder messages(List<Message> messages) {
            this.messages = messages;
            return this;
        }

        public Builder messages(Message... messages) {
            return messages(List.of(messages));
        }

        public Builder thinkingEnabled(Boolean thinkingEnabled) {
            this.isThinkingEnabled = thinkingEnabled;
            return this;
        }

        public Builder streamingEnabled(Boolean streamingEnabled) {
            this.isStreamingEnabled = streamingEnabled;
            return this;
        }

        public DefaultChatInput build() {
            return new DefaultChatInput(
                    messages,
                    isThinkingEnabled,
                    isStreamingEnabled
            );
        }
    }
}
