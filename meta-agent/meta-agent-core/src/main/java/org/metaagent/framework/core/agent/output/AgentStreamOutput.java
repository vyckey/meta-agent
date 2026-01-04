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
import org.metaagent.framework.common.metadata.MetadataProvider;
import reactor.core.publisher.Flux;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

/**
 * An agent output that contains a stream of outputs.
 *
 * @param <O> the type of the output
 * @param <S> the type of the stream element
 * @author vyckey
 */
public interface AgentStreamOutput<O, S> extends AgentOutput<O> {
    /**
     * Creates an agent stream output with the given stream.
     *
     * @param <O> the type of the output
     * @param <S> the type of the stream element
     * @return the agent output
     */
    static <O, S> AgentStreamOutput<O, S> create(Flux<S> stream) {
        return new DefaultAgentStreamOutput<>(stream);
    }

    /**
     * Creates an agent stream output with the given stream and metadata.
     *
     * @param <O> the type of the output
     * @param <S> the type of the stream element
     * @return the agent output
     */
    static <O, S> AgentStreamOutput<O, S> create(Flux<S> stream, MetadataProvider metadata) {
        return new DefaultAgentStreamOutput<>(stream, null, metadata);
    }

    /**
     * Returns the stream of the agent output.
     *
     * @return the stream of the agent output
     */
    Flux<S> stream();


    /**
     * Aggregates the stream outputs into the one agent output.
     */
    @FunctionalInterface
    interface Aggregator<S, O> {
        /**
         * Aggregate the stream outputs into the agent output.
         *
         * @param streamOutputs stream outputs
         * @return aggregated agent output
         */
        O aggregate(Iterable<S> streamOutputs);

        /**
         * Create an aggregator that reduces the stream of agent outputs into a single agent output.
         *
         * @param initialOutput the initial agent output to start with.
         * @param accumulator   the operator to accumulate output and stream output.
         * @param combiner      the binary operator to combine two agent outputs into a single agent output.
         * @param <O>           the type of the agent output.
         * @return An aggregator that reduces the stream of agent outputs into a single agent output.
         */
        static <S, O> Aggregator<S, O> reduce(O initialOutput,
                                              BiFunction<O, ? super S, O> accumulator,
                                              BinaryOperator<O> combiner) {
            return streamOutputs ->
                    Lists.newArrayList(streamOutputs).stream().reduce(initialOutput, accumulator, combiner);
        }

        /**
         * Create an aggregator that reduces the stream of agent outputs into a single agent output.
         *
         * @param initialOutput the initial agent output to start with.
         * @param accumulator   the operator to accumulate output and stream output.
         * @param <O>           the type of the agent output.
         * @return An aggregator that reduces the stream of agent outputs into a single agent output.
         */
        static <S, O> Aggregator<S, O> reduce(O initialOutput,
                                              BiFunction<O, ? super S, O> accumulator) {
            return streamOutputs -> {
                O fullOutput = initialOutput;
                for (S streamOutput : streamOutputs) {
                    fullOutput = accumulator.apply(fullOutput, streamOutput);
                }
                return fullOutput;
            };
        }
    }
}
