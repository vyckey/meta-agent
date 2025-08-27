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

package org.metaagent.framework.core.agent.output;

import reactor.core.publisher.Flux;

import java.util.function.BiFunction;

/**
 * AgentStreamOutputAggregator
 *
 * @author vyckey
 */
public interface AgentStreamOutputAggregator<S, O> {
    /**
     * Create an agent stream output aggregator.
     *
     * @param initialState the initial state of the agent stream output aggregator
     * @param reducer      the reducer function
     * @param <S>          the type of the stream output
     * @param <O>          the type of the agent output
     * @return the agent stream output aggregator
     */
    static <S, O> AgentStreamOutputAggregator<S, O> reduce(AgentOutput<O> initialState,
                                                           BiFunction<AgentOutput<O>, AgentOutput<S>, AgentOutput<O>> reducer) {
        return new DefaultAgentStreamOutputAggregator<>(initialState, reducer);
    }

    /**
     * Initial state of the stream output.
     *
     * @return initial state of the stream output
     */
    AgentOutput<O> initialState();

    /**
     * Aggregate the stream output into the agent output.
     *
     * @param agentOutput  agent output
     * @param streamOutput stream output
     * @return aggregated agent output
     */
    AgentOutput<O> aggregate(AgentOutput<O> agentOutput, AgentOutput<S> streamOutput);

    /**
     * Aggregate the stream output into the agent output.
     *
     * @param stream stream output
     * @return aggregated agent output
     */
    default AgentOutput<O> aggregate(Flux<AgentOutput<S>> stream) {
        return stream.reduce(initialState(), this::aggregate).block();
    }
}
