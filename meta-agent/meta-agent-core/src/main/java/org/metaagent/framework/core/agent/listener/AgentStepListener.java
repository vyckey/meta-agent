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

package org.metaagent.framework.core.agent.listener;

import org.metaagent.framework.core.agent.MetaAgent;
import org.metaagent.framework.core.agent.context.AgentStepContext;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.output.AgentOutput;

/**
 * AgentStepListener is a listener that is called before the agent next loop.
 *
 * @param <I> the type of agent input
 * @param <O> the type of agent output
 * @param <C> the type of agent step context
 * @author vyckey
 */
public interface AgentStepListener<
        I extends AgentInput,
        O extends AgentOutput,
        C extends AgentStepContext> {

    /**
     * Called before the agent step.
     *
     * @param agent       the agent
     * @param agentInput  the input
     * @param stepContext the step context
     */
    default void onAgentStepStart(MetaAgent<I, O> agent, I agentInput, C stepContext) {
    }

    /**
     * Called when an agent step is finished.
     *
     * @param agent       the agent
     * @param agentInput  the input
     * @param agentOutput the output
     * @param stepContext the step context
     */
    default void onAgentStepFinish(MetaAgent<I, O> agent, I agentInput, O agentOutput, C stepContext) {
    }

    /**
     * Called when an agent step throws an exception.
     *
     * @param agent       the agent
     * @param agentInput  the input
     * @param exception   the exception
     * @param stepContext the step context
     */
    default void onAgentStepError(MetaAgent<I, O> agent, I agentInput, Exception exception, C stepContext) {
    }
}
