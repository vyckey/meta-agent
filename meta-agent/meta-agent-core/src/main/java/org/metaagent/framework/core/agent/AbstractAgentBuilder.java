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

package org.metaagent.framework.core.agent;

import org.metaagent.framework.core.agent.memory.Memory;
import org.metaagent.framework.core.agent.observability.AgentEventBus;
import org.metaagent.framework.core.agent.observability.event.AgentEvent;
import org.metaagent.framework.core.agent.profile.AgentProfile;
import org.metaagent.framework.core.agent.state.AgentState;
import org.metaagent.framework.core.tool.manager.ToolManager;

/**
 * AbstractAgentBuilder is an abstract class that provides a common interface for building agents.
 *
 * @param <A> the type of agent being built
 * @param <B> the type of builder
 * @param <I> the input type of agent
 * @param <O> the output type of agent
 * @author vyckey
 */
public abstract class AbstractAgentBuilder<A extends MetaAgent<I, O>, B, I, O> {
    protected AgentProfile profile;
    protected AgentState agentState;
    protected Memory memory;
    protected ToolManager toolManager;
    protected AgentEventBus<AgentEvent> agentEventBus;

    public B agentProfile(AgentProfile profile) {
        this.profile = profile;
        return self();
    }

    public B agentState(AgentState agentState) {
        this.agentState = agentState;
        return self();
    }

    public B memory(Memory memory) {
        this.memory = memory;
        return self();
    }

    public B toolManager(ToolManager toolManager) {
        this.toolManager = toolManager;
        return self();
    }

    public B agentEventBus(AgentEventBus<AgentEvent> agentEventBus) {
        this.agentEventBus = agentEventBus;
        return self();
    }

    /**
     * Returns the builder instance.
     *
     * @return The builder instance.
     */
    protected abstract B self();

    /**
     * Builds an agent.
     *
     * @return The built agent.
     */
    public abstract A build();
}
