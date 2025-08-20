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

package org.metaagent.framework.core.agent.input;

import org.metaagent.framework.core.agent.AgentExecutionContext;
import org.metaagent.framework.core.common.metadata.MetadataProvider;

import java.util.Objects;

/**
 * Default implementation of {@link AgentInput}.
 *
 * @author vyckey
 */
public record DefaultAgentInput<I>(
        I input,
        AgentExecutionContext context,
        MetadataProvider metadata
) implements AgentInput<I> {
    public DefaultAgentInput(I input, AgentExecutionContext context, MetadataProvider metadata) {
        this.input = Objects.requireNonNull(input, "input is required");
        this.context = Objects.requireNonNull(context, "context is required");
        this.metadata = Objects.requireNonNull(metadata, "metadata is required");
    }

    public static <I> Builder<I> builder(I input) {
        return new Builder<>(input);
    }

    public static class Builder<I> implements AgentInput.Builder<I> {
        private I input;
        private AgentExecutionContext context;
        private MetadataProvider metadata;

        private Builder(I input) {
            this.input = input;
        }

        @Override
        public AgentInput.Builder<I> input(I input) {
            this.input = input;
            return this;
        }

        @Override
        public AgentInput.Builder<I> context(AgentExecutionContext context) {
            this.context = Objects.requireNonNull(context, "context is required");
            return this;
        }

        @Override
        public AgentInput.Builder<I> metadata(MetadataProvider metadata) {
            this.metadata = Objects.requireNonNull(metadata, "metadata is required");
            return this;
        }

        @Override
        public AgentInput<I> build() {
            if (context == null) {
                context = AgentExecutionContext.create();
            }
            if (metadata == null) {
                metadata = MetadataProvider.empty();
            }
            return new DefaultAgentInput<>(input, context, metadata);
        }
    }
}
