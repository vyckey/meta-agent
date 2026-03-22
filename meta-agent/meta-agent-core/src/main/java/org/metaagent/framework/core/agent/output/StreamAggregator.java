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

package org.metaagent.framework.core.agent.output;

import com.google.common.collect.Lists;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

/**
 * Aggregates the stream outputs into the one output.
 *
 * @author vyckey
 */
@FunctionalInterface
public
interface StreamAggregator<S, O> {
    /**
     * Aggregate the stream outputs into the output.
     *
     * @param streamOutputs stream outputs
     * @return aggregated output
     */
    O aggregate(Iterable<S> streamOutputs);

    /**
     * Create an aggregator that reduces the stream outputs of into an output.
     *
     * @param initialOutput the initial output to start with.
     * @param accumulator   the operator to accumulate output and stream output.
     * @param combiner      the binary operator to combine two outputs into an output.
     * @param <O>           the type of the output.
     * @return An aggregator that reduces the stream of outputs into an output.
     */
    static <S, O> StreamAggregator<S, O> reduce(O initialOutput,
                                                BiFunction<O, ? super S, O> accumulator,
                                                BinaryOperator<O> combiner) {
        return streamOutputs ->
                Lists.newArrayList(streamOutputs).stream().reduce(initialOutput, accumulator, combiner);
    }

    /**
     * Create an aggregator that reduces the stream of outputs into a single output.
     *
     * @param initialOutput the initial output to start with.
     * @param accumulator   the operator to accumulate output and stream output.
     * @param <O>           the type of the output.
     * @return An aggregator that reduces the stream of outputs into a single output.
     */
    static <S, O> StreamAggregator<S, O> reduce(O initialOutput,
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
