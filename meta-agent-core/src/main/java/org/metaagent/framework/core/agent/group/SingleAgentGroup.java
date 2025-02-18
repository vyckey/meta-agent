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

import java.util.List;
import java.util.Objects;

/**
 * description is here
 *
 * @author vyckey
 */
public class SingleAgentGroup implements AgentGroup {
    private final MetaAgent agent;

    public SingleAgentGroup(MetaAgent agent) {
        this.agent = Objects.requireNonNull(agent, "agent is required");
    }

    @Override
    public int getAgentCount() {
        return 1;
    }

    @Override
    public MetaAgent getAgent(String name) {
        return agent.getName().equals(name) ? agent : null;
    }

    @Override
    public List<MetaAgent> getAgents() {
        return List.of(agent);
    }

    @Override
    public void addAgent(MetaAgent agent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAgent(MetaAgent agent) {
        throw new UnsupportedOperationException();
    }
}
