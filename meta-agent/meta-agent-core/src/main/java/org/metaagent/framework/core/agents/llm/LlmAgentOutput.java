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

package org.metaagent.framework.core.agents.llm;

import com.google.common.base.Preconditions;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.part.MessagePart;
import org.metaagent.framework.core.agent.output.AgentStreamOutput;
import org.metaagent.framework.core.agents.chat.model.metadata.TokenUsage;
import reactor.core.publisher.Flux;

/**
 * Output of {@link LlmAgent}.
 *
 * @author vyckey
 */
public record LlmAgentOutput(
        Message message,

        Flux<MessagePart> stream,

        String finishReason,

        TokenUsage tokenUsage)
        implements AgentStreamOutput<MessagePart> {
    public LlmAgentOutput {
        Preconditions.checkArgument(message != null, "message cannot be null");
        Preconditions.checkArgument(tokenUsage != null, "tokenUsage cannot be null");
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }


    public static class Builder {
        private Message message;
        private Flux<MessagePart> stream;
        private String finishReason;
        private TokenUsage tokenUsage;

        private Builder() {
        }

        private Builder(LlmAgentOutput agentOutput) {
            this.message = agentOutput.message();
            this.stream = agentOutput.stream();
            this.finishReason = agentOutput.finishReason();
            this.tokenUsage = agentOutput.tokenUsage();
        }

        public Builder message(Message message) {
            this.message = message;
            return this;
        }

        public Builder stream(Flux<MessagePart> stream) {
            this.stream = stream;
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
            return new LlmAgentOutput(message, stream, finishReason, tokenUsage);
        }
    }
}
