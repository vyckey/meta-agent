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

import org.metaagent.framework.common.abort.AbortSignal;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.MessageId;
import org.metaagent.framework.core.agents.llm.context.LlmContextCache;
import org.metaagent.framework.core.model.provider.ModelProviderRegistry;

import java.util.List;
import java.util.Objects;

/**
 * Input data for the compaction operation.
 * <p>
 * This record encapsulates all the data required to perform context compaction,
 * including the conversation messages, compaction options, and context cache.
 * Compaction summarizes conversation history to fit within model token limits.
 * <p>
 * Use the {@link Builder} for fluent construction of inputs.
 *
 * @param messages              the conversation messages to compact
 * @param userMessageId         the ID of the current user message being processed
 * @param options               the compaction options with thresholds and behaviors
 * @param modelProviderRegistry the model provider registry
 * @param contextCache          the LLM context cache for the compaction operation
 * @param abortSignal           the abort signal
 * @author vyckey
 * @see Builder
 * @see ContextCompactionService#compact(CompactionInput)
 */
public record CompactionInput(
        List<Message> messages,
        MessageId userMessageId,
        CompactionOptions options,
        ModelProviderRegistry modelProviderRegistry,
        LlmContextCache contextCache,
        AbortSignal abortSignal
) {
    /**
     * Creates a new {@code CompactionInput} with validation for required fields.
     *
     * @throws NullPointerException if messages, compactPrompt, or options is null
     */
    public CompactionInput {
        Objects.requireNonNull(messages, "messages cannot be null");
        Objects.requireNonNull(options, "options cannot be null");
    }

    private CompactionInput(Builder builder) {
        this(
                builder.messages,
                builder.userMessageId,
                builder.options,
                builder.modelProviderRegistry,
                builder.contextCache,
                builder.abortSignal
        );
    }

    /**
     * Returns a new {@link Builder} instance for constructing {@code CompactionInput}.
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
     * Builder class for constructing {@link CompactionInput} instances.
     * <p>
     * Provides a fluent API for setting input fields. Required fields must be
     * set before calling {@link #build()}.
     */
    public static class Builder {
        private List<Message> messages;
        private MessageId userMessageId;
        private CompactionOptions options;
        private ModelProviderRegistry modelProviderRegistry;
        private LlmContextCache contextCache;
        private AbortSignal abortSignal;

        private Builder() {
        }

        private Builder(CompactionInput input) {
            this.messages = input.messages;
            this.userMessageId = input.userMessageId;
            this.options = input.options;
            this.modelProviderRegistry = input.modelProviderRegistry;
            this.contextCache = input.contextCache;
            this.abortSignal = input.abortSignal;
        }

        /**
         * Sets the conversation messages to compact.
         *
         * @param messages the list of messages (required)
         * @return this builder instance for method chaining
         */
        public Builder messages(List<Message> messages) {
            this.messages = messages;
            return this;
        }

        /**
         * Sets the ID of the current user message being processed.
         *
         * @param userMessageId the user message ID
         * @return this builder instance for method chaining
         */
        public Builder userMessageId(MessageId userMessageId) {
            this.userMessageId = userMessageId;
            return this;
        }

        /**
         * Sets the compaction options with thresholds and behaviors.
         *
         * @param options the compaction options (required)
         * @return this builder instance for method chaining
         */
        public Builder options(CompactionOptions options) {
            this.options = options;
            return this;
        }

        /**
         * Sets the model provider registry.
         *
         * @param modelProviderRegistry the model provider registry
         * @return this builder instance for method chaining
         */
        public Builder modelProviderRegistry(ModelProviderRegistry modelProviderRegistry) {
            this.modelProviderRegistry = modelProviderRegistry;
            return this;
        }

        /**
         * Sets the LLM context cache for the compaction operation.
         *
         * @param contextCache the context cache
         * @return this builder instance for method chaining
         */
        public Builder contextCache(LlmContextCache contextCache) {
            this.contextCache = contextCache;
            return this;
        }

        /**
         * Sets the abort signal.
         *
         * @param abortSignal the abort signal
         * @return this builder instance for method chaining
         */
        public Builder abortSignal(AbortSignal abortSignal) {
            this.abortSignal = abortSignal;
            return this;
        }

        /**
         * Builds and returns a new {@link CompactionInput} instance.
         *
         * @return a new CompactionInput instance
         * @throws NullPointerException if any required field is null
         */
        public CompactionInput build() {
            return new CompactionInput(this);
        }
    }
}
