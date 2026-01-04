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

package org.metaagent.framework.core.agent.profile;

import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.common.metadata.MetadataProvider;

import java.util.Objects;

/**
 * Default implementation of {@link AgentProfile}.
 *
 * @author vyckey
 */
public record DefaultAgentProfile(
        String name,
        String description,
        MetadataProvider metadata
) implements AgentProfile {

    public DefaultAgentProfile {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("agent name is empty");
        }
        description = StringUtils.defaultIfEmpty(description, null);
        Objects.requireNonNull(metadata, "agent metadata is required");
    }

    public static Builder<?> builder() {
        return new Builder<>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public MetadataProvider getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        if (StringUtils.isEmpty(description)) {
            return name;
        }
        return name + ": " + description;
    }

    public static class Builder<B extends Builder<B>> implements AgentProfile.Builder {
        private String name;
        private String description;
        private MetadataProvider metadata;

        protected Builder<B> self() {
            return this;
        }

        @Override
        public Builder<B> name(String name) {
            this.name = name;
            return self();
        }

        @Override
        public Builder<B> description(String description) {
            this.description = description;
            return this;
        }

        @Override
        public Builder<B> metadata(MetadataProvider metadata) {
            this.metadata = metadata;
            return this;
        }

        @Override
        public Builder<B> metadata(String key, Object value) {
            if (metadata == null) {
                metadata = MetadataProvider.create();
            }
            metadata.setProperty(key, value);
            return this;
        }

        @Override
        public AgentProfile build() {
            if (metadata == null) {
                metadata = MetadataProvider.empty();
            }
            return new DefaultAgentProfile(name, description, metadata);
        }
    }
}
