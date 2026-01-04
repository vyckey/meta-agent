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

package org.metaagent.framework.core.agents.coordinator;

import org.metaagent.framework.core.agent.Agent;
import org.metaagent.framework.core.agent.group.AgentGroup;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.output.AgentOutput;

/**
 * CoordinateAgent is an interface that represents an agent responsible for coordinating.
 *
 * @author vyckey
 */
public interface CoordinateAgent<I, O> extends Agent<I, O> {
    /**
     * Gets the agent group managed by this coordinator.
     *
     * @return the agent group containing all managed agents
     */
    AgentGroup getAgentGroup();

    /**
     * Coordinates the execution by selecting an appropriate agent based on the input.
     * The selection strategy depends on the specific implementation.
     *
     * @param input the input to process
     * @return the output from the selected agent
     */
    @Override
    AgentOutput<O> step(AgentInput<I> input);
}
