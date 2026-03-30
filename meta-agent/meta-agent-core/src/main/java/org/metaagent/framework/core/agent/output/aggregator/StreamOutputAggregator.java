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

package org.metaagent.framework.core.agent.output.aggregator;

import com.google.common.collect.Lists;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

/**
 * Aggregates the stream outputs into the one result.
 *
 * @param <S> the type of the stream output.
 * @param <R> the type of the aggregated output.
 * @author vyckey
 */
@FunctionalInterface
public interface StreamOutputAggregator<S, R> {
    /**
     * Aggregate the stream outputs into the output.
     *
     * @param streamOutputs stream outputs
     * @return aggregated output
     */
    R aggregate(Iterable<S> streamOutputs);

    /**
     * Create an aggregator that reduces the stream outputs of into an output.
     *
     * @param initialOutput the initial output to start with.
     * @param accumulator   the operator to accumulate output and stream output.
     * @param combiner      the binary operator to combine two outputs into an output.
     * @param <S>           the type of the stream output.
     * @param <R>           the type of the aggregated output.
     * @return An aggregator that reduces the stream of outputs into an output.
     */
    static <S, R> StreamOutputAggregator<S, R> reduce(R initialOutput,
                                                      BiFunction<R, ? super S, R> accumulator,
                                                      BinaryOperator<R> combiner) {
        return streamOutputs ->
                Lists.newArrayList(streamOutputs).stream().reduce(initialOutput, accumulator, combiner);
    }

    /**
     * Create an aggregator that reduces the stream of outputs into a single output.
     *
     * @param initialOutput the initial output to start with.
     * @param accumulator   the operator to accumulate output and stream output.
     * @param <S>           the type of the stream output.
     * @param <R>           the type of the aggregated output.
     * @return An aggregator that reduces the stream of outputs into a single output.
     */
    static <S, R> StreamOutputAggregator<S, R> reduce(R initialOutput,
                                                      BiFunction<R, ? super S, R> accumulator) {
        return streamOutputs -> {
            R fullOutput = initialOutput;
            for (S streamOutput : streamOutputs) {
                fullOutput = accumulator.apply(fullOutput, streamOutput);
            }
            return fullOutput;
        };
    }
}
