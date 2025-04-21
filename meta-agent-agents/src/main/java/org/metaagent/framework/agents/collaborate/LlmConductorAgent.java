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

package org.metaagent.framework.agents.collaborate;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.NotImplementedException;
import org.metaagent.framework.core.agent.AbstractAgent;
import org.metaagent.framework.core.agent.Agent;
import org.metaagent.framework.core.agent.AgentExecutionContext;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.metaagent.framework.core.agents.cooperation.ConductorAgent;

import java.util.List;

/**
 * description is here
 *
 * @author vyckey
 */
public class LlmConductorAgent extends AbstractAgent implements ConductorAgent {
    protected final List<Agent> actorAgents;

    public LlmConductorAgent(String name, List<Agent> actorAgents) {
        super(name);
        this.actorAgents = actorAgents;
    }

    public LlmConductorAgent(String name) {
        this(name, Lists.newArrayList());
    }

    public void addActorAgent(Agent actorAgent) {
        this.actorAgents.add(actorAgent);
    }

    public void removeActorAgent(Agent actorAgent) {
        this.actorAgents.remove(actorAgent);
    }

    @Override
    public List<Agent> getActorAgents() {
        return actorAgents;
    }

    @Override
    protected AgentOutput doStep(AgentExecutionContext context, AgentInput input) {
        throw new NotImplementedException("not implement yet");
    }
}
