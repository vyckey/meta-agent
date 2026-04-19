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

import java.nio.file.Path;
import java.util.Objects;

/**
 * Configuration options for the pruning operation.
 *
 * @param charThresholdToPrune the char length threshold per tool output that triggers pruning
 * @param charKeepWhenPrune    the char length to keep when pruning
 * @param prunedOutputPath     the output path of pruned tool contents
 * @author vyckey
 * @see Builder
 */
public record PruningOptions(
        int charThresholdToPrune,
        Integer charKeepWhenPrune,
        Path prunedOutputPath
) {
    /**
     * Creates a new {@code PruningOptions} with validation.
     *
     * @throws IllegalArgumentException if charThresholdToPrune is negative
     */
    public PruningOptions {
        if (charThresholdToPrune < 0) {
            throw new IllegalArgumentException(
                    "charThresholdToPrune must be non-negative, got: " + charThresholdToPrune);
        }
        if (charKeepWhenPrune != null && charKeepWhenPrune < 0) {
            throw new IllegalArgumentException(
                    "charKeepWhenPrune must be non-negative, got: " + charKeepWhenPrune);
        }
        Objects.requireNonNull(prunedOutputPath, "prunedOutputPath cannot be null");
    }

    /**
     * Returns a new {@link Builder} instance.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns a {@link Builder} initialized with this instance's values.
     *
     * @return a builder pre-populated with this options
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * Builder for constructing {@link PruningOptions}.
     */
    public static class Builder {
        private Integer charThresholdToPrune = 2000;
        private Integer charKeepWhenPrune;
        private Path prunedOutputPath;

        private Builder() {
        }

        private Builder(PruningOptions options) {
            this.charThresholdToPrune = options.charThresholdToPrune;
            this.charKeepWhenPrune = options.charKeepWhenPrune;
            this.prunedOutputPath = options.prunedOutputPath;
        }

        public Builder charThresholdToPrune(Integer charThresholdToPrune) {
            this.charThresholdToPrune = charThresholdToPrune;
            return this;
        }

        public Builder charKeepWhenPrune(Integer charKeepWhenPrune) {
            this.charKeepWhenPrune = charKeepWhenPrune;
            return this;
        }

        public Builder prunedOutputPath(Path prunedOutputPath) {
            this.prunedOutputPath = prunedOutputPath;
            return this;
        }

        public PruningOptions build() {
            return new PruningOptions(
                    charThresholdToPrune,
                    charKeepWhenPrune,
                    prunedOutputPath
            );
        }
    }
}