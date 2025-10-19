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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Default implementation of the AgentGroup interface.
 *
 * @author vyckey
 */
public class DefaultAgentGroup implements AgentGroup {
    private final Map<String, MetaAgent<?, ?, ?>> agents;

    public DefaultAgentGroup(Map<String, MetaAgent<?, ?, ?>> agents) {
        this.agents = Objects.requireNonNull(agents);
    }

    public DefaultAgentGroup() {
        this.agents = new HashMap<>();
    }

    @Override
    public int getAgentCount() {
        return agents.size();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I, O, S> MetaAgent<I, O, S> getAgent(String name) {
        return (MetaAgent<I, O, S>) agents.get(name);
    }

    @Override
    public Map<String, MetaAgent<?, ?, ?>> getAgents() {
        return Collections.unmodifiableMap(agents);
    }

    @Override
    public void addAgent(MetaAgent<?, ?, ?> agent) {
        Objects.requireNonNull(agent, "agent is required");
        agents.put(agent.getName(), agent);
    }

    @Override
    public void removeAgent(MetaAgent<?, ?, ?> agent) {
        Objects.requireNonNull(agent, "agent is required");
        if (!agents.containsKey(agent.getName())) {
            throw new IllegalArgumentException("Agent " + agent.getName() + " not found in the group");
        }
        agents.remove(agent.getName());
    }
}
