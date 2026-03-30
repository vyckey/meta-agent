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

package org.metaagent.framework.core.model.chat;

import org.metaagent.framework.core.model.ModelId;
import org.metaagent.framework.core.model.chat.config.ModelApiConfig;

import java.time.Instant;
import java.util.Objects;

/**
 * Default, record-based implementation of {@link ChatModelInfo}.
 *
 * <p>This immutable record provides a straightforward data holder for chat
 * model metadata. Use the nested {@link Builder} to construct instances.
 */
public record DefaultChatModelInfo(
        ModelId id,
        String name,
        String family,
        ModelApiConfig apiConfig,
        int contextSize,
        Instant cutOffDate
) implements ChatModelInfo {
    /**
     * Canonical compact constructor to validate inputs.
     */
    public DefaultChatModelInfo {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        if (contextSize <= 0) {
            throw new IllegalArgumentException("contextSize must be positive");
        }
    }

    /**
     * Create a new fluent builder for {@link DefaultChatModelInfo}.
     *
     * @return a new {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public ModelId getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFamily() {
        return family;
    }

    @Override
    public ModelApiConfig getApiConfig() {
        return apiConfig;
    }

    @Override
    public int getContextSize() {
        return contextSize;
    }

    @Override
    public Instant getCutOffDate() {
        return cutOffDate;
    }

    /**
     * Fluent builder for {@link DefaultChatModelInfo}.
     */
    public static final class Builder {
        private ModelId id;
        private String name;
        private String family;
        private ModelApiConfig apiConfig;
        private int contextSize = 0;
        private Instant cutOffDate;

        public Builder() {
        }

        public Builder id(String providerId, String modelId) {
            this.id = ModelId.of(providerId, modelId);
            return this;
        }

        public Builder id(ModelId id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder family(String family) {
            this.family = family;
            return this;
        }

        public Builder apiConfig(ModelApiConfig apiConfig) {
            this.apiConfig = apiConfig;
            return this;
        }

        public Builder contextSize(int contextSize) {
            this.contextSize = contextSize;
            return this;
        }

        public Builder cutOffDate(Instant cutOffDate) {
            this.cutOffDate = cutOffDate;
            return this;
        }

        /**
         * Build the {@link DefaultChatModelInfo} instance.
         *
         * @return a new DefaultChatModelInfo
         */
        public DefaultChatModelInfo build() {
            return new DefaultChatModelInfo(id, name, family, apiConfig, contextSize, cutOffDate);
        }
    }
}
