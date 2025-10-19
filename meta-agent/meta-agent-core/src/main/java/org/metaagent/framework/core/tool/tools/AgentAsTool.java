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

package org.metaagent.framework.core.tool.tools;

import org.metaagent.framework.core.agent.MetaAgent;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.exception.ToolExecutionException;

/**
 * AgentAsTool is a tool that can be used to execute an Agent. The agent is treated as a tool.
 *
 * @param <I> the input type
 * @param <O> the output type
 * @author vyckey
 */
public interface AgentAsTool<I, O, S> extends Tool<AgentInput<I>, AgentOutput<O>> {
    /**
     * Get the agent.
     *
     * @return the agent
     */
    MetaAgent<I, O, S> getAgent();

    /**
     * Get the tool's name.
     *
     * @return the tool name
     */
    @Override
    default String getName() {
        return getAgent().getName();
    }

    @Override
    default AgentOutput<O> run(ToolContext context, AgentInput<I> input) throws ToolExecutionException {
        return getAgent().run(input);
    }
}
