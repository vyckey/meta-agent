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

import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Default implementation of {@link AgentStreamOutputAggregator}.
 *
 * @param <S> The type of stream output.
 * @param <O> The type of agent output.
 * @author vyckey
 */
public class DefaultAgentStreamOutputAggregator<S, O> implements AgentStreamOutputAggregator<S, O> {
    private final AgentOutput<O> initialState;
    private final BiFunction<AgentOutput<O>, AgentOutput<S>, AgentOutput<O>> reducer;

    public DefaultAgentStreamOutputAggregator(AgentOutput<O> initialState,
                                              BiFunction<AgentOutput<O>, AgentOutput<S>, AgentOutput<O>> reducer) {
        this.initialState = initialState;
        this.reducer = Objects.requireNonNull(reducer, "Reducer cannot be null");
    }

    @Override
    public AgentOutput<O> initialState() {
        return initialState;
    }

    @Override
    public AgentOutput<O> aggregate(AgentOutput<O> agentOutput, AgentOutput<S> streamOutput) {
        return reducer.apply(agentOutput, streamOutput);
    }
}
