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

import org.metaagent.framework.core.agent.loop.AgentLoopControlStrategy;
import org.metaagent.framework.core.agent.loop.MaxLoopCountAgentLoopControl;
import org.metaagent.framework.core.tool.manager.DefaultToolManager;
import org.metaagent.framework.core.tool.manager.ToolManager;

/**
 * Abstract {@link Agent} implementation.
 *
 * @author vyckey
 */
public abstract class AbstractAgent<
        AgentInput extends org.metaagent.framework.core.agent.input.AgentInput,
        AgentOutput extends org.metaagent.framework.core.agent.output.AgentOutput>
        extends AbstractMetaAgent<AgentInput, AgentOutput> implements Agent<AgentInput, AgentOutput> {
    protected ToolManager toolManager = new DefaultToolManager();

    protected AbstractAgent(String name) {
        super(name);
    }

    @Override
    public ToolManager getToolManager() {
        return toolManager;
    }

    @Override
    public AgentLoopControlStrategy<AgentInput, AgentOutput> getLoopControlStrategy() {
        return new MaxLoopCountAgentLoopControl<>(1);
    }

    @Override
    protected AgentOutput doRun(AgentInput input) {
        return Agent.super.run(input);
    }
}
