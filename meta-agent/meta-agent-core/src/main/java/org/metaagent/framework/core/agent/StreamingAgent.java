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
 * @author vyckey
 */
public interface StreamingAgent<I extends AgentInput, O extends AgentStreamOutput<?>> extends MetaAgent<I, O> {

    /**
     * Run the agent with input.
     * <p>
     * The implementation of method can choose to run in streaming mode or non-streaming mode.
     * <ul>
     * <li>Streaming mode (default): The agent runs in streaming mode and returns a non-null value from {@link AgentStreamOutput#stream()}.</li>
     * <li>Non-streaming mode: The agent runs in non-streaming mode and returns a null value from {@link AgentStreamOutput#stream()}.</li>
     * </ul>
     *
     * @param agentInput the agent input.
     * @return the final agent output.
     */
    @Override
    default O run(I agentInput) {
        return runStream(agentInput);
    }

    /**
     * Run the agent in streaming mode.
     *
     * @param agentInput the agent input.
     * @return the final agent output.
     */
    O runStream(I agentInput);

}
