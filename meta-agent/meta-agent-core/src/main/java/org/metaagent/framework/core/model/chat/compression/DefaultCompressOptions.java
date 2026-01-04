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

/**
 * DefaultCompressOptions represents the default options for compressing messages.
 *
 * @author vyckey
 */
public record DefaultCompressOptions(
        int maxTokens,
        int reservedMessagesCount,
        String compressPrompt) implements CompressOptions {

    private DefaultCompressOptions(Builder builder) {
        this(builder.maxTokens, builder.reservedMessagesCount, builder.compressPrompt);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public int getMaxTokens() {
        return maxTokens;
    }

    @Override
    public int getReservedMessageCount() {
        return reservedMessagesCount;
    }

    @Override
    public String getCompressPrompt() {
        return compressPrompt;
    }


    public static class Builder {
        private int maxTokens;
        private int reservedMessagesCount;
        private String compressPrompt;

        private Builder() {
        }

        public Builder maxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder reservedMessagesCount(int reservedMessagesCount) {
            this.reservedMessagesCount = reservedMessagesCount;
            return this;
        }

        public Builder compressPrompt(String compressPrompt) {
            this.compressPrompt = compressPrompt;
            return this;
        }

        public DefaultCompressOptions build() {
            return new DefaultCompressOptions(this);
        }
    }
}
