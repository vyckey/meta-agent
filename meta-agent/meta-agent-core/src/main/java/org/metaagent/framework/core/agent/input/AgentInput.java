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

import org.metaagent.framework.common.metadata.MetadataProvider;
import org.metaagent.framework.core.agent.AgentExecutionContext;

/**
 * AgentInput is an interface that represents the input for an agent.
 *
 * @author vyckey
 */
public interface AgentInput<I> {
    /**
     * Returns the agent input builder.
     *
     * @return the agent input builder
     */
    static <I> Builder<I> builder(I input) {
        return DefaultAgentInput.builder(input);
    }

    /**
     * Gets the input of the agent.
     *
     * @return the input
     */
    I input();

    /**
     * Gets the context of the agent execution.
     *
     * @return the AgentExecutionContext associated with this input
     */
    AgentExecutionContext context();

    /**
     * Gets the metadata of agent input.
     *
     * @return the metadata.
     */
    default MetadataProvider metadata() {
        return MetadataProvider.empty();
    }


    /**
     * Builder class for AgentInput.
     */
    interface Builder<I> {
        /**
         * Sets the input of agent input.
         *
         * @param input the input.
         * @return the builder
         */
        Builder<I> input(I input);

        /**
         * Sets the context of the agent execution.
         *
         * @param context the AgentExecutionContext associated with this input
         * @return the builder
         */
        Builder<I> context(AgentExecutionContext context);

        /**
         * Sets the metadata of agent input.
         *
         * @param metadata the metadata.
         * @return the builder
         */
        Builder<I> metadata(MetadataProvider metadata);

        /**
         * Builds an AgentInput instance.
         *
         * @return the AgentInput instance
         */
        AgentInput<I> build();
    }
}
