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
import org.metaagent.framework.core.agent.chat.message.MessageInfo;
import org.metaagent.framework.core.agent.chat.message.part.MessagePart;
import org.metaagent.framework.core.agent.output.AgentStreamOutput;
import org.metaagent.framework.core.agents.llm.LlmStreamingAgent;
import reactor.core.publisher.Flux;

/**
 * Output of {@link LlmStreamingAgent}.
 *
 * @author vyckey
 * @see LlmStreamingAgent
 */
public record LlmAgentStreamOutput(
        MessageInfo messageInfo,

        Flux<MessagePart> stream
) implements AgentStreamOutput<MessagePart> {
    public LlmAgentStreamOutput {
        Preconditions.checkArgument(messageInfo != null, "messageInfo is required");
        Preconditions.checkArgument(stream != null, "stream is required");
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }


    public static class Builder {
        private MessageInfo messageInfo;
        private Flux<MessagePart> stream;

        private Builder() {
        }

        private Builder(LlmAgentStreamOutput agentOutput) {
            this.messageInfo = agentOutput.messageInfo();
            this.stream = agentOutput.stream();
        }

        public Builder messageInfo(MessageInfo messageInfo) {
            this.messageInfo = messageInfo;
            return this;
        }

        public Builder stream(Flux<MessagePart> stream) {
            this.stream = stream;
            return this;
        }

        public LlmAgentStreamOutput build() {
            return new LlmAgentStreamOutput(messageInfo, stream);
        }
    }
}
