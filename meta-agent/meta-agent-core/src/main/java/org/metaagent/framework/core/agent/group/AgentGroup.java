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

package org.metaagent.framework.core.agent.group;

import org.metaagent.framework.core.agent.MetaAgent;

import java.util.Map;

/**
 * AgentGroup is an interface that represents a group of agents.
 *
 * @author vyckey
 */
public interface AgentGroup {
    /**
     * Creates a new instance of AgentGroup.
     *
     * @return a new AgentGroup instance
     */
    static AgentGroup create() {
        return new DefaultAgentGroup();
    }

    /**
     * Gets agent count in the group.
     *
     * @return the number of agents in the group
     */
    int getAgentCount();

    /**
     * Gets the agent by name.
     *
     * @param name the name of the agent
     * @param <I>  the type of AgentInput
     * @param <O>  the type of AgentOutput
     * @return the agent with the specified name
     */
    <I, O> MetaAgent<I, O> getAgent(String name);

    /**
     * Gets all agents in the group.
     *
     * @return a map of agent names to agents
     */
    Map<String, MetaAgent<?, ?>> getAgents();

    /**
     * Adds an agent to the group.
     *
     * @param agent the agent to add
     */
    void addAgent(MetaAgent<?, ?> agent);

    /**
     * Removes an agent from the group.
     *
     * @param agent the agent to remove
     */
    void removeAgent(MetaAgent<?, ?> agent);
}
