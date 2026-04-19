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

import org.metaagent.framework.core.model.ModelId;
import org.metaagent.framework.core.model.prompt.PromptValue;

/**
 * Configuration options for context compaction operations.
 * <p>
 * This record defines the thresholds and behaviors for triggering context
 * compaction in LLM agents. Compaction is the process of summarizing or
 * pruning conversation history to fit within model token limits.
 * <p>
 * Two threshold mechanisms are supported:
 * <ul>
 *   <li><b>Proportion threshold (default):</b> Ratio of used tokens to available context size.
 *       Default is 1.0 (100% of available context). The available context size is calculated
 *       as {@code modelContextSize - reservedTokensToCompact}.</li>
 *   <li><b>Token threshold (optional):</b> Absolute token count that triggers compaction.</li>
 * </ul>
 * Either threshold being reached will trigger compaction.
 * <p>
 * An optional {@code modelId} can be specified to indicate which model should
 * be used for the compaction operation (e.g., a smaller/cheaper model for
 * summarization).
 * <p>
 * Use the {@link Builder} for fluent construction of options.
 *
 * @param tokenProportionToTrigger the proportion (0.0-1.0) of available context that triggers compaction (default: 1.0)
 * @param tokenThresholdToTrigger  the absolute token count that triggers compaction, or null to disable
 * @param modelId                  the optional model ID to use for compaction, or null to use default
 * @param appendedPrompt           the optional appended prompt to extend the compaction prompt
 * @param reservedTokensToCompact  tokens reserved for the compaction summary response
 * @author vyckey
 * @see Builder
 */
public record CompactionOptions(
        float tokenProportionToTrigger,
        Integer tokenThresholdToTrigger,
        ModelId modelId,
        PromptValue appendedPrompt,
        int reservedTokensToCompact,
        int reservedMessagesToKeep
) {
    /**
     * Creates a new {@code CompactOptions} with validation for field constraints.
     *
     * @throws IllegalArgumentException if tokenProportionToTrigger is not between 0.0 and 1.0
     * @throws IllegalArgumentException if tokenThresholdToTrigger is not null and is negative
     * @throws IllegalArgumentException if reservedTokensToCompact is negative
     */
    public CompactionOptions {
        if (tokenProportionToTrigger < 0.0f || tokenProportionToTrigger > 1.0f) {
            throw new IllegalArgumentException(
                    "tokenProportionToTrigger must be between 0.0 and 1.0, got: " + tokenProportionToTrigger);
        }
        if (tokenThresholdToTrigger != null && tokenThresholdToTrigger < 0) {
            throw new IllegalArgumentException(
                    "tokenThresholdToTrigger must be non-negative, got: " + tokenThresholdToTrigger);
        }
        if (reservedTokensToCompact < 0) {
            throw new IllegalArgumentException(
                    "reservedTokensToCompact must be non-negative, got: " + reservedTokensToCompact);
        }
        if (reservedMessagesToKeep < 0) {
            throw new IllegalArgumentException(
                    "reservedMessagesToKeep must be non-negative, got: " + reservedMessagesToKeep);
        }
    }

    /**
     * Checks if the current token count has reached the compaction threshold.
     * <p>
     * This method evaluates both the proportion threshold (against available context)
     * and the optional absolute token threshold. Compaction is recommended if either
     * threshold is exceeded.
     * <p>
     * The available context size for proportion calculation is:
     * {@code modelContextSize - reservedTokensToCompact}.
     *
     * @param currentTokenCount the current number of tokens in use
     * @param modelContextSize  the maximum context size of the model
     * @return {@code true} if compaction should be triggered, {@code false} otherwise
     * @throws IllegalArgumentException if currentTokenCount is negative
     * @throws IllegalArgumentException if available context (modelContextSize - reservedTokensToCompact) is not positive
     */
    public boolean isThresholdReached(int currentTokenCount, int modelContextSize) {
        if (currentTokenCount < 0) {
            throw new IllegalArgumentException("currentTokenCount must be non-negative, got: " + currentTokenCount);
        }

        if (tokenThresholdToTrigger != null && currentTokenCount >= tokenThresholdToTrigger) {
            return true;
        }

        int availableContextSize = modelContextSize - reservedTokensToCompact;
        if (availableContextSize <= 0) {
            throw new IllegalArgumentException(
                    "Available context size (modelContextSize - reservedTokensToCompact) must be positive, got: "
                            + availableContextSize + " (modelContextSize=" + modelContextSize
                            + ", reservedTokensToCompact=" + reservedTokensToCompact + ")");
        } else if (currentTokenCount >= availableContextSize) {
            return true;
        }
        float proportion = (float) currentTokenCount / availableContextSize;
        return proportion >= tokenProportionToTrigger;
    }

    /**
     * Returns a new {@link Builder} instance for constructing {@code CompactOptions}.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns a {@link Builder} initialized with the values from this instance.
     * Useful for creating a modified copy of existing options.
     *
     * @return a builder pre-populated with this option's values
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * Builder class for constructing {@link CompactionOptions} instances.
     * <p>
     * Provides a fluent API for setting configuration options.
     * <p>
     * Defaults:
     * <ul>
     *   <li>tokenProportionToTrigger: 1.0f (100% of available context)</li>
     *   <li>tokenThresholdToTrigger: null (disabled)</li>
     *   <li>pruneBeforeCompact: false</li>
     *   <li>reservedTokensToCompact: 1024</li>
     * </ul>
     */
    public static class Builder {
        private float tokenProportionToTrigger = 1.0f;
        private Integer tokenThresholdToTrigger;
        private ModelId modelId;
        private PromptValue appendedPrompt;
        private int reservedTokensToCompact = 1024;
        private int reservedMessagesToKeep = 10;

        private Builder() {
        }

        private Builder(CompactionOptions options) {
            this.tokenProportionToTrigger = options.tokenProportionToTrigger();
            this.tokenThresholdToTrigger = options.tokenThresholdToTrigger;
            this.modelId = options.modelId;
            this.appendedPrompt = options.appendedPrompt;
            this.reservedTokensToCompact = options.reservedTokensToCompact();
            this.reservedMessagesToKeep = options.reservedMessagesToKeep();
        }

        /**
         * Sets the proportion threshold that triggers compaction.
         * <p>
         * When the ratio of current tokens to available context size exceeds this value,
         * compaction will be triggered. The available context size is calculated as
         * {@code modelContextSize - reservedTokensToCompact}.
         * <p>
         * Value must be between 0.0 and 1.0. Default is 1.0 (100% of available context).
         *
         * @param tokenProportionToTrigger the proportion threshold (0.0-1.0)
         * @return this builder instance for method chaining
         */
        public Builder tokenProportionToTrigger(float tokenProportionToTrigger) {
            this.tokenProportionToTrigger = tokenProportionToTrigger;
            return this;
        }

        /**
         * Sets the absolute token threshold that triggers compaction.
         * <p>
         * When the current token count exceeds this value, compaction will be triggered.
         * <p>
         * Set to {@code null} to disable absolute token threshold (use only
         * proportion-based triggering).
         *
         * @param tokenThresholdToTrigger the token count threshold, or null to disable
         * @return this builder instance for method chaining
         */
        public Builder tokenThresholdToTrigger(Integer tokenThresholdToTrigger) {
            this.tokenThresholdToTrigger = tokenThresholdToTrigger;
            return this;
        }

        /**
         * Sets the model ID to use for compaction.
         * <p>
         * When specified, this model will be used for the compaction/summarization
         * operation. This allows using a different (e.g., smaller/cheaper) model
         * for compaction than the main conversation model.
         * <p>
         * If not set (null), the default model will be used.
         *
         * @param modelId the model ID to use for compaction, or null for default
         * @return this builder instance for method chaining
         */
        public Builder modelId(ModelId modelId) {
            this.modelId = modelId;
            return this;
        }

        /**
         * Sets the appended prompt to use for compaction.
         * <p>
         * When specified, this prompt will be appended to the compaction input
         * before compaction. This allows adding additional instructions to the
         * compaction process.
         * <p>
         * If not set (null), no appended prompt will be used.
         *
         * @param appendedPrompt the appended prompt to use for compaction, or null for none
         * @return this builder instance for method chaining
         */
        public Builder appendedPrompt(PromptValue appendedPrompt) {
            this.appendedPrompt = appendedPrompt;
            return this;
        }

        /**
         * Sets the number of tokens to reserve for the compaction response.
         * <p>
         * This ensures the model has enough tokens available to generate
         * the summary message during compaction.
         *
         * @param reservedTokensToCompact tokens to reserve for compaction response
         * @return this builder instance for method chaining
         */
        public Builder reservedTokensToCompact(int reservedTokensToCompact) {
            this.reservedTokensToCompact = reservedTokensToCompact;
            return this;
        }

        public Builder reservedMessagesToKeep(int reservedMessagesToKeep) {
            this.reservedMessagesToKeep = reservedMessagesToKeep;
            return this;
        }

        public CompactionOptions build() {
            return new CompactionOptions(
                    tokenProportionToTrigger,
                    tokenThresholdToTrigger,
                    modelId,
                    appendedPrompt,
                    reservedTokensToCompact,
                    reservedMessagesToKeep
            );
        }
    }
}
