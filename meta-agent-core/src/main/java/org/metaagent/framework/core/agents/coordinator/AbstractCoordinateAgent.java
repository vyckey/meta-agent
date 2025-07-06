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

import com.google.common.collect.Lists;
import org.metaagent.framework.core.agent.AbstractAgent;
import org.metaagent.framework.core.agent.Agent;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.output.AgentOutput;

import java.util.List;

/**
 * AbstractCoordinateAgent is an abstract class that implements the CoordinateAgent interface.
 * It provides a base implementation for agents that coordinate the execution of other agents.
 *
 * @author vyckey
 */
public abstract class AbstractCoordinateAgent<I extends AgentInput, O extends AgentOutput>
        extends AbstractAgent<I, O> implements CoordinateAgent<I, O> {
    protected final List<Agent<?, ?>> executeAgents = Lists.newArrayList();

    protected AbstractCoordinateAgent(String name) {
        super(name);
    }

    @Override
    public List<Agent<?, ?>> getExecuteAgents() {
        return executeAgents;
    }

    @Override
    public void addExecuteAgent(Agent<?, ?> executeAgent) {
        this.executeAgents.add(executeAgent);
    }

    @Override
    public void removeExecuteAgent(Agent<?, ?> executeAgent) {
        this.executeAgents.remove(executeAgent);
    }
}
