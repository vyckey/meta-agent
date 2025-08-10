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

import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.common.metadata.MetadataProvider;

import java.util.List;

public record DefaultChatAgentOutput(
        List<Message> messages,
        String thoughtProcess,
        MetadataProvider metadata) implements ChatAgentOutput {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<Message> messages;
        private String thoughtProcess;
        private MetadataProvider metadata;

        public Builder messages(List<Message> messages) {
            this.messages = messages;
            return this;
        }

        public Builder messages(Message... messages) {
            this.messages = List.of(messages);
            return this;
        }

        public Builder thoughtProcess(String thoughtProcess) {
            this.thoughtProcess = thoughtProcess;
            return this;
        }

        public Builder metadata(MetadataProvider metadata) {
            this.metadata = metadata;
            return this;
        }

        public DefaultChatAgentOutput build() {
            if (messages == null) {
                messages = List.of();
            }
            if (metadata == null) {
                metadata = MetadataProvider.empty();
            }
            return new DefaultChatAgentOutput(messages, thoughtProcess, metadata);
        }
    }
}
