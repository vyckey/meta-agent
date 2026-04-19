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

package org.metaagent.framework.core.agents.llm.compaction;

import org.metaagent.framework.core.agent.chat.message.Message;

import java.util.List;
import java.util.Objects;

/**
 * Input data for the pruning operation.
 *
 * @param messages the conversation messages to prune
 * @param options  the pruning configuration
 * @author vyckey
 * @see Builder
 * @see ContextCompactionService#prune(PruningInput)
 */
public record PruningInput(
        List<Message> messages,
        PruningOptions options
) {
    /**
     * Creates a new {@code PruningInput} with validation for required fields.
     *
     * @throws NullPointerException if messages or options is null
     */
    public PruningInput {
        Objects.requireNonNull(messages, "messages cannot be null");
        Objects.requireNonNull(options, "options cannot be null");
    }

    private PruningInput(Builder builder) {
        this(
                builder.messages,
                builder.options
        );
    }

    /**
     * Returns a new {@link Builder} instance for constructing {@code PruningInput}.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns a {@link Builder} initialized with the values from this instance.
     * Useful for creating a modified copy of existing input.
     *
     * @return a builder pre-populated with this input's values
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * Builder class for constructing {@link PruningInput} instances.
     */
    public static class Builder {
        private List<Message> messages;
        private PruningOptions options;

        private Builder() {
        }

        private Builder(PruningInput input) {
            this.messages = input.messages;
            this.options = input.options;
        }

        public Builder messages(List<Message> messages) {
            this.messages = messages;
            return this;
        }

        public Builder config(PruningOptions options) {
            this.options = options;
            return this;
        }

        public PruningInput build() {
            return new PruningInput(this);
        }
    }
}
