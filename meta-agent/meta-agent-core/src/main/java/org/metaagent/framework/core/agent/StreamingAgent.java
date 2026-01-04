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

package org.metaagent.framework.core.agent;

import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.output.AgentStreamOutput;

/**
 * The streaming agent abstraction.
 *
 * @param <I> the type of agent input
 * @param <O> the type of agent output
 * @param <S> the type of agent stream output
 * @author vyckey
 */
public interface StreamingAgent<I, O, S> extends Agent<I, O> {
    /**
     * Runs agent logic in a streaming way.
     *
     * @param input the agent input.
     * @return the streaming agent output.
     */
    default AgentStreamOutput<O, S> runStream(AgentInput<I> input) {
        return stepStream(input);
    }

    /**
     * Runs agent logic in a streaming way.
     *
     * @param input the agent input.
     * @return the streaming agent output.
     */
    default AgentStreamOutput<O, S> runStream(I input) {
        return runStream(AgentInput.builder(input).context(newExecutionContext()).build());
    }

    /**
     * Start an agent step.
     *
     * @param input the agent input.
     * @return the agent output.
     */
    default AgentStreamOutput<O, S> stepStream(AgentInput<I> input) {
        throw new UnsupportedOperationException("Streaming is not supported");
    }

    /**
     * Get the agent stream output aggregator.
     *
     * @return the agent stream output aggregator.
     */
    default AgentStreamOutput.Aggregator<S, O> getStreamOutputAggregator() {
        throw new UnsupportedOperationException("Streaming is not supported");
    }
}
