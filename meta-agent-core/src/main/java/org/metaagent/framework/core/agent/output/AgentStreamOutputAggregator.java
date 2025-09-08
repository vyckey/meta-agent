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

import com.google.common.collect.Lists;
import reactor.core.publisher.Flux;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

/**
 * AgentStreamOutputAggregator
 *
 * @author vyckey
 */
@FunctionalInterface
public interface AgentStreamOutputAggregator<S, O> {
    /**
     * Create an AgentStreamOutputAggregator that reduces the stream of agent outputs into a single agent output.
     *
     * @param initialOutput the initial agent output to start with.
     * @param accumulator   the operator to accumulate output and stream output.
     * @param <S>           the type of the stream output.
     * @param <O>           the type of the agent output.
     * @return An AgentStreamOutputAggregator that reduces the stream of agent outputs into a single agent output.
     */
    static <S, O> AgentStreamOutputAggregator<S, O> reduce(AgentOutput<O> initialOutput,
                                                           BiFunction<AgentOutput<O>, AgentOutput<S>, AgentOutput<O>> accumulator) {
        return streamOutputs -> {
            AgentOutput<O> fullOutput = initialOutput;
            for (AgentOutput<S> streamOutput : streamOutputs) {
                fullOutput = accumulator.apply(fullOutput, streamOutput);
            }
            return fullOutput;
        };
    }

    /**
     * Create an AgentStreamOutputAggregator that reduces the stream of agent outputs into a single agent output.
     *
     * @param initialOutput the initial agent output to start with.
     * @param accumulator   the operator to accumulate output and stream output.
     * @param combiner      the binary operator to combine two agent outputs into a single agent output.
     * @param <S>           the type of the stream output.
     * @param <O>           the type of the agent output.
     * @return An AgentStreamOutputAggregator that reduces the stream of agent outputs into a single agent output.
     */
    static <S, O> AgentStreamOutputAggregator<S, O> reduce(AgentOutput<O> initialOutput,
                                                           BiFunction<AgentOutput<O>, AgentOutput<S>, AgentOutput<O>> accumulator,
                                                           BinaryOperator<AgentOutput<O>> combiner) {
        return streamOutputs ->
                Lists.newArrayList(streamOutputs).stream().reduce(initialOutput, accumulator, combiner);
    }

    /**
     * Aggregate the stream outputs into the agent output.
     *
     * @param streamOutputs stream outputs
     * @return aggregated agent output
     */
    AgentOutput<O> aggregate(Iterable<AgentOutput<S>> streamOutputs);

    /**
     * Aggregate the stream output into the agent output.
     *
     * @param stream stream output
     * @return aggregated agent output
     */
    default AgentOutput<O> aggregate(Flux<AgentOutput<S>> stream) {
        return aggregate(stream.collectList().block());
    }
}
