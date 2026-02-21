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

import org.metaagent.framework.common.metadata.MetadataProvider;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.part.MessagePart;
import reactor.core.publisher.Flux;

/**
 * Default implementation of {@link ChatOutput}.
 *
 * @author vyckey
 */
public record DefaultChatOutput(
        Message message,
        Flux<MessagePart> stream,
        MetadataProvider metadata
) implements ChatOutput {

    public DefaultChatOutput {
        if (message == null && stream == null) {
            throw new IllegalArgumentException("message or stream is required");
        }
        if (metadata == null) {
            metadata = MetadataProvider.empty();
        }
    }

    public DefaultChatOutput(Message message) {
        this(message, null, MetadataProvider.empty());
    }

    public DefaultChatOutput(Flux<MessagePart> stream) {
        this(null, stream, MetadataProvider.empty());
    }

    public DefaultChatOutput(Builder builder) {
        this(builder.message, builder.stream, builder.metadata);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(ChatOutput chatOutput) {
        return new Builder(chatOutput);
    }

    @Override
    public Flux<MessagePart> stream() {
        return stream;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DefaultChatOutput{");
        if (stream != null) {
            return sb.append("stream=true").append("}").toString();
        }
        sb.append("\nmessages:\n").append(message.content());
        return sb.append("\n}").toString();
    }

    public static class Builder implements ChatOutput.Builder {
        private Message message;
        private Flux<MessagePart> stream;
        private MetadataProvider metadata;

        private Builder() {
        }

        private Builder(ChatOutput chatOutput) {
            this.message = chatOutput.message();
            this.stream = chatOutput.stream();
            this.metadata = chatOutput.metadata();
        }

        @Override
        public Builder message(Message message) {
            this.message = message;
            return this;
        }

        @Override
        public Builder stream(Flux<MessagePart> stream) {
            this.stream = stream;
            return this;
        }

        @Override
        public Builder metadata(MetadataProvider metadata) {
            this.metadata = metadata;
            return this;
        }

        @Override
        public DefaultChatOutput build() {
            return new DefaultChatOutput(this);
        }
    }

}
