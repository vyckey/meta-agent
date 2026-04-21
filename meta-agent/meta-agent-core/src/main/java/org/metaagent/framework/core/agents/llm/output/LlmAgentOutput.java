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

package org.metaagent.framework.core.agents.llm.output;

import com.google.common.base.Preconditions;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.metaagent.framework.core.agents.llm.LlmAgent;
import org.metaagent.framework.core.model.chat.metadata.TokenUsage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Output of {@link LlmAgent}.
 *
 * @author vyckey
 * @see LlmAgent
 */
public record LlmAgentOutput(
        List<Message> messages,

        String finishReason,

        TokenUsage tokenUsage
) implements AgentOutput {
    public LlmAgentOutput {
        Preconditions.checkArgument(messages != null, "messages is required");
        Preconditions.checkArgument(tokenUsage != null, "tokenUsage is required");
    }

    public static Builder builder() {
        return new Builder();
    }

    public Message message() {
        return messages.isEmpty() ? null : messages.get(messages.size() - 1);
    }

    public Builder toBuilder() {
        return new Builder(this);
    }


    public static class Builder {
        private List<Message> messages;
        private String finishReason;
        private TokenUsage tokenUsage;

        private Builder() {
        }

        private Builder(LlmAgentOutput agentOutput) {
            this.messages = agentOutput.messages();
            this.finishReason = agentOutput.finishReason();
            this.tokenUsage = agentOutput.tokenUsage();
        }

        public Builder message(Message message) {
            this.messages = List.of(message);
            return this;
        }

        public Builder messages(List<Message> messages) {
            this.messages = messages;
            return this;
        }

        public Builder addMessage(Message message) {
            if (this.messages == null) {
                this.messages = new ArrayList<>();
            }
            this.messages.add(message);
            return this;
        }

        public Builder finishReason(String finishReason) {
            this.finishReason = finishReason;
            return this;
        }

        public Builder tokenUsage(TokenUsage tokenUsage) {
            this.tokenUsage = tokenUsage;
            return this;
        }

        public LlmAgentOutput build() {
            return new LlmAgentOutput(Collections.unmodifiableList(messages), finishReason, tokenUsage);
        }
    }
}
