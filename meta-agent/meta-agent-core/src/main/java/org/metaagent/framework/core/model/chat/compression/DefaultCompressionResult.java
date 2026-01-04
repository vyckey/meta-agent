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

package org.metaagent.framework.core.model.chat.compression;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.model.ResultMetadata;

import java.util.List;
import java.util.Objects;

/**
 * DefaultCompressionResult is a default implementation of {@link CompressionResult}.
 *
 * @author vyckey
 */
public record DefaultCompressionResult(
        boolean compressed,
        float compressionRatio,
        Message summary,
        List<Message> removedMessages,
        List<Message> retainedMessages
) implements CompressionResult {
    public DefaultCompressionResult {
        if (compressed) {
            Objects.requireNonNull(summary, "summary is required");
            Objects.requireNonNull(removedMessages, "removedMessages is required");
            Objects.requireNonNull(retainedMessages, "retainedMessages is required");
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean isCompressed() {
        return compressed;
    }

    @Override
    public float getCompressionRatio() {
        return compressionRatio;
    }

    @Override
    public Message getSummary() {
        return summary;
    }

    @Override
    public List<Message> getRemovedMessages() {
        return removedMessages;
    }

    @Override
    public List<Message> getRetainedMessages() {
        return retainedMessages;
    }

    @Override
    public ResultMetadata getMetadata() {
        return new ResultMetadata() {
        };
    }


    public static class Builder {
        private boolean compressed;
        private float compressionRatio = 1.0f;
        private Message summary;
        private List<Message> removedMessages;
        private List<Message> retainedMessages;

        private Builder() {
        }

        public Builder compressed(boolean compressed) {
            this.compressed = compressed;
            return this;
        }

        public Builder compressionRatio(float compressionRatio) {
            this.compressionRatio = compressionRatio;
            return this;
        }

        public Builder summary(Message summary) {
            this.summary = summary;
            return this;
        }

        public Builder removedMessages(List<Message> removedMessages) {
            this.removedMessages = removedMessages;
            return this;
        }

        public Builder retainedMessages(List<Message> retainedMessages) {
            this.retainedMessages = retainedMessages;
            return this;
        }

        public DefaultCompressionResult build() {
            return new DefaultCompressionResult(compressed, compressionRatio, summary, removedMessages, retainedMessages);
        }
    }
}
