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

package org.metaagent.framework.core.agents.chat.model.metadata;

import org.metaagent.framework.common.metadata.ClassMetadataProvider;
import org.metaagent.framework.common.metadata.MetadataProvider;
import org.springframework.ai.chat.metadata.RateLimit;
import org.springframework.ai.chat.metadata.Usage;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link ChatResponseMetadata}.
 *
 * @author vyckey
 */
public class DefaultChatResponseMetadata extends ClassMetadataProvider implements ChatResponseMetadata {
    private final String model;
    private final Usage tokenUsage;
    private final RateLimit rateLimit;
    private final String finishReason;

    private DefaultChatResponseMetadata(String model, Usage tokenUsage, RateLimit rateLimit, String finishReason) {
        this.model = model;
        this.tokenUsage = tokenUsage;
        this.rateLimit = rateLimit;
        this.finishReason = finishReason;
    }

    private DefaultChatResponseMetadata(Builder builder) {
        this.model = builder.model;
        this.tokenUsage = builder.tokenUsage;
        this.rateLimit = builder.rateLimit;
        this.finishReason = builder.finishReason;
        if (builder.extendProperties != null) {
            this.extendProperties.putAll(builder.extendProperties);
        }
    }

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public Usage getTokenUsage() {
        return tokenUsage;
    }

    @Override
    public RateLimit getRateLimit() {
        return rateLimit;
    }

    @Override
    public String getFinishReason() {
        return finishReason;
    }

    @Override
    public MetadataProvider copy() {
        return new DefaultChatResponseMetadata(model, tokenUsage, rateLimit, finishReason);
    }

    public static class Builder implements ChatResponseMetadata.Builder {
        private String model;
        private Usage tokenUsage;
        private RateLimit rateLimit;
        private String finishReason;
        private Map<String, Object> extendProperties;

        @Override
        public Builder model(String model) {
            this.model = model;
            return this;
        }

        @Override
        public Builder tokenUsage(Usage tokenUsage) {
            this.tokenUsage = tokenUsage;
            return this;
        }

        @Override
        public Builder rateLimit(RateLimit rateLimit) {
            this.rateLimit = rateLimit;
            return this;
        }

        @Override
        public Builder finishReason(String finishReason) {
            this.finishReason = finishReason;
            return this;
        }

        @Override
        public ChatResponseMetadata.Builder metadata(String key, Object value) {
            if (extendProperties == null) {
                extendProperties = new HashMap<>();
            }
            extendProperties.put(key, value);
            return this;
        }

        public Builder metadata(org.springframework.ai.chat.metadata.ChatResponseMetadata metadata) {
            this.model = metadata.getModel();
            this.tokenUsage = metadata.getUsage();
            this.rateLimit = metadata.getRateLimit();
            return this;
        }

        @Override
        public DefaultChatResponseMetadata build() {
            return new DefaultChatResponseMetadata(model, tokenUsage, rateLimit, finishReason);
        }
    }
}
