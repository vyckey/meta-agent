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
import org.metaagent.framework.core.model.ModelId;
import org.metaagent.framework.core.model.prompt.Prompt;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link ChatInput}.
 *
 * @author vyckey
 */
public record DefaultChatInput(
        List<Message> messages,
        ModelId modelId,
        Prompt systemPrompt,
        Boolean isThinkingEnabled,
        Boolean isStreamingEnabled
) implements ChatInput {

    public DefaultChatInput {
        Objects.requireNonNull(messages, "messages is required");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(ChatInput chatInput) {
        return new Builder(chatInput);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DefaultChatInput{");
        if (isThinkingEnabled != null) {
            sb.append("\nthinking: ").append(isThinkingEnabled);
        }
        if (isStreamingEnabled != null) {
            sb.append("\nstreaming: ").append(isStreamingEnabled);
        }
        String messages = messages().stream().map(m -> "- " + m).collect(Collectors.joining("\n"));
        sb.append("\nmessages:\n").append(messages);
        return sb.append("\n}").toString();
    }

    public static class Builder implements ChatInput.Builder {
        private List<Message> messages;
        private ModelId modelId;
        private Prompt systemPrompt;
        private Boolean isThinkingEnabled;
        private Boolean isStreamingEnabled;

        private Builder() {
        }

        public Builder(ChatInput chatInput) {
            this.messages = chatInput.messages();
            this.isThinkingEnabled = chatInput.isThinkingEnabled();
            this.isStreamingEnabled = chatInput.isStreamingEnabled();
        }

        @Override
        public Builder messages(List<Message> messages) {
            this.messages = messages;
            return this;
        }

        @Override
        public Builder messages(Message... messages) {
            return messages(List.of(messages));
        }

        @Override
        public ChatInput.Builder modelId(ModelId modelId) {
            this.modelId = modelId;
            return this;
        }

        @Override
        public ChatInput.Builder systemPrompt(Prompt systemPrompt) {
            this.systemPrompt = systemPrompt;
            return this;
        }

        @Override
        public Builder isThinkingEnabled(Boolean isThinkingEnabled) {
            this.isThinkingEnabled = isThinkingEnabled;
            return this;
        }

        @Override
        public Builder isStreamingEnabled(Boolean isStreamingEnabled) {
            this.isStreamingEnabled = isStreamingEnabled;
            return this;
        }

        public DefaultChatInput build() {
            return new DefaultChatInput(
                    messages,
                    modelId,
                    systemPrompt,
                    isThinkingEnabled,
                    isStreamingEnabled
            );
        }
    }
}
