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

package org.metaagent.framework.core.agents.llm.context;

import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.model.ModelId;
import org.metaagent.framework.core.model.prompt.PromptValue;
import org.metaagent.framework.core.tool.executor.ToolExecutorContext;
import org.metaagent.framework.core.tool.manager.ToolManager;

import java.util.List;
import java.util.Objects;

/**
 * A cache class that holds context information for LLM (Large Language Model) queries.
 * <p>
 * This record encapsulates all the contextual data required for executing LLM queries,
 * including the model identifier, system prompts, messages to prepend, and tool-related
 * configurations. It is used to pass cached context between different components of
 * the LLM agent system.
 * <p>
 * The cache is immutable and can be created using the {@link Builder} class for
 * flexible and readable construction.
 *
 * @param modelId             the identifier of the LLM model to use
 * @param systemPrompts       the list of system prompts to set the behavior/context
 * @param prependMessages     the list of messages to prepend to the conversation
 * @param toolManager         the manager for handling available tools
 * @param toolExecutorContext the context for tool execution
 * @author vyckey
 * @see Builder
 */
public record LlmContextCache(
        ModelId modelId,
        List<PromptValue> systemPrompts,
        List<Message> prependMessages,
        ToolManager toolManager,
        ToolExecutorContext toolExecutorContext
) {
    /**
     * Creates a new {@code LlmContextCache} with validation for required fields.
     */
    public LlmContextCache {
        Objects.requireNonNull(modelId, "modelId cannot be null");
        systemPrompts = systemPrompts != null ? List.copyOf(systemPrompts) : List.of();
        prependMessages = prependMessages != null ? List.copyOf(prependMessages) : List.of();
        toolManager = toolManager != null ? toolManager : ToolManager.create();
        toolExecutorContext = toolExecutorContext != null ? toolExecutorContext : ToolExecutorContext.create();
    }

    private LlmContextCache(Builder builder) {
        this(
                builder.modelId,
                builder.systemPrompts,
                builder.prependMessages,
                builder.toolManager,
                builder.toolExecutorContext
        );
    }

    /**
     * Returns a new {@link Builder} instance for constructing {@code LlmContextCache}.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns a {@link Builder} initialized with the values from this instance.
     * Useful for creating a modified copy of an existing cache.
     *
     * @return a builder pre-populated with this cache's values
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * Builder class for constructing {@link LlmContextCache} instances.
     * <p>
     * Provides a fluent API for setting optional and required fields.
     * Required fields must be set before calling {@link #build()}.
     */
    public static class Builder {
        private ModelId modelId;
        private List<PromptValue> systemPrompts;
        private List<Message> prependMessages;
        private ToolManager toolManager;
        private ToolExecutorContext toolExecutorContext;

        private Builder() {
        }

        private Builder(LlmContextCache cache) {
            this.modelId = cache.modelId;
            this.systemPrompts = cache.systemPrompts;
            this.prependMessages = cache.prependMessages;
            this.toolManager = cache.toolManager;
            this.toolExecutorContext = cache.toolExecutorContext;
        }

        /**
         * Sets the model identifier for the LLM query.
         *
         * @param modelId the identifier of the LLM model to use
         * @return this builder instance for method chaining
         */
        public Builder modelId(ModelId modelId) {
            this.modelId = modelId;
            return this;
        }

        /**
         * Sets the system prompts for the LLM query.
         *
         * @param systemPrompts the list of system prompts
         * @return this builder instance for method chaining
         */
        public Builder systemPrompts(List<PromptValue> systemPrompts) {
            this.systemPrompts = systemPrompts;
            return this;
        }

        /**
         * Sets a single system prompt for the LLM query.
         *
         * @param systemPrompt the system prompt
         * @return this builder instance for method chaining
         */
        public Builder systemPrompt(PromptValue systemPrompt) {
            this.systemPrompts = List.of(systemPrompt);
            return this;
        }

        /**
         * Sets the messages to prepend to the conversation.
         *
         * @param prependMessages the list of messages to prepend
         * @return this builder instance for method chaining
         */
        public Builder prependMessages(List<Message> prependMessages) {
            this.prependMessages = prependMessages;
            return this;
        }

        /**
         * Sets a single message to prepend to the conversation.
         *
         * @param prependMessage the message to prepend
         * @return this builder instance for method chaining
         */
        public Builder prependMessage(Message prependMessage) {
            this.prependMessages = List.of(prependMessage);
            return this;
        }

        /**
         * Sets the tool manager for handling available tools.
         *
         * @param toolManager the tool manager
         * @return this builder instance for method chaining
         */
        public Builder toolManager(ToolManager toolManager) {
            this.toolManager = toolManager;
            return this;
        }

        /**
         * Sets the tool executor context.
         *
         * @param toolExecutorContext the tool executor context
         * @return this builder instance for method chaining
         */
        public Builder toolExecutorContext(ToolExecutorContext toolExecutorContext) {
            this.toolExecutorContext = toolExecutorContext;
            return this;
        }

        /**
         * Builds and returns a new {@link LlmContextCache} instance.
         *
         * @return a new LlmContextCache instance
         * @throws NullPointerException if modelId is null
         */
        public LlmContextCache build() {
            return new LlmContextCache(this);
        }
    }
}
