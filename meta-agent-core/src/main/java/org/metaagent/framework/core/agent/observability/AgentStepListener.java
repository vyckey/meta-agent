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

package org.metaagent.framework.core.agent.observability;

import org.metaagent.framework.core.agent.MetaAgent;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.output.AgentOutput;

/**
 * AgentStepListener is a listener that is called before the agent next loop.
 *
 * @author vyckey
 */
public interface AgentStepListener<I, O, S> {
    /**
     * Called before the agent next loop.
     *
     * @param agent the agent
     */
    default void onAgentNextLoop(MetaAgent<I, O, S> agent) {
    }

    /**
     * Called before the agent step.
     *
     * @param agent the agent
     * @param input the input
     */
    default void onAgentStepStart(MetaAgent<I, O, S> agent, AgentInput<I> input) {
    }

    /**
     * Called when an agent step is finished.
     *
     * @param agent  the agent
     * @param input  the input
     * @param output the output
     */
    default void onAgentStepFinish(MetaAgent<I, O, S> agent, AgentInput<I> input, AgentOutput<O> output) {
    }

    /**
     * Called when an agent step throws an exception.
     *
     * @param agent     the agent
     * @param input     the input
     * @param exception the exception
     */
    default void onAgentStepError(MetaAgent<I, O, S> agent, AgentInput<I> input, Exception exception) {
    }
}
