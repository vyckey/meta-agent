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
 * AbstractAgentInput is an abstract class that implements the AgentInput interface.
 *
 * @author vyckey
 */
public class AbstractAgentInput implements AgentInput {
    protected AgentExecutionContext context;
    protected MetadataProvider metadata = MetadataProvider.empty();

    protected AbstractAgentInput(AgentExecutionContext context) {
        this.context = Objects.requireNonNull(context, "context is required");
    }

    protected AbstractAgentInput(Builder<?> builder) {
        this.context = Objects.requireNonNull(builder.context, "context is required");
        this.metadata = Objects.requireNonNull(builder.metadata, "metadata is required");
    }

    @Override
    public AgentExecutionContext context() {
        return context;
    }

    @Override
    public MetadataProvider metadata() {
        return metadata;
    }

    public static abstract class Builder<B extends AgentInput.Builder<B>> implements AgentInput.Builder<B> {
        protected AgentExecutionContext context;
        protected MetadataProvider metadata;

        protected Builder() {
        }

        protected abstract B self();

        @Override
        public B context(AgentExecutionContext context) {
            this.context = Objects.requireNonNull(context, "context is required");
            return self();
        }

        @Override
        public B metadata(MetadataProvider metadata) {
            this.metadata = Objects.requireNonNull(metadata, "metadata is required");
            return self();
        }

        @Override
        public abstract AgentInput build();
    }
}
