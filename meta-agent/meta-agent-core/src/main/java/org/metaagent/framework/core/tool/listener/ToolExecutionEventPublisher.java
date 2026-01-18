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

package org.metaagent.framework.core.tool.listener;

import org.metaagent.framework.common.json.JsonObjectMapper;
import org.metaagent.framework.core.agent.Agent;
import org.metaagent.framework.core.agent.observability.AgentEventBus;
import org.metaagent.framework.core.agent.observability.event.AgentEvent;
import org.metaagent.framework.core.agent.observability.event.AgentToolErrorEvent;
import org.metaagent.framework.core.agent.observability.event.AgentToolRequestEvent;
import org.metaagent.framework.core.agent.observability.event.AgentToolResponseEvent;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.exception.ToolExecutionException;

import java.util.Objects;

/**
 * ToolExecutionEventPublisher is an event publisher for tool execution events.
 *
 * @author vyckey
 */
public class ToolExecutionEventPublisher<I, O> implements ToolExecutionListener {
    private final AgentEventBus<AgentEvent> agentEventBus;

    public ToolExecutionEventPublisher(AgentEventBus<AgentEvent> agentEventBus) {
        this.agentEventBus = Objects.requireNonNull(agentEventBus, "agentEventBus is required");
    }

    @Override
    public void onToolInputRequest(Tool<?, ?> tool, ToolContext toolContext, String input) {
        Agent<?, ?> agent = toolContext.getAgent();
        if (agent != null) {
            agentEventBus.publish(new AgentToolRequestEvent(agent, tool, input));
        }
    }

    @Override
    public <I> void onToolException(Tool<I, ?> tool, ToolContext toolContext, I input, ToolExecutionException exception) {
        Agent<?, ?> agent = toolContext.getAgent();
        if (agent != null) {
            String inputJson = JsonObjectMapper.CAMEL_CASE.toJson(input);
            agentEventBus.publish(new AgentToolErrorEvent(agent, tool, inputJson, exception));
        }
    }

    @Override
    public void onToolResponse(Tool<?, ?> tool, ToolContext toolContext, String input, String output) {
        Agent<?, ?> agent = toolContext.getAgent();
        if (agent != null) {
            agentEventBus.publish(new AgentToolResponseEvent(agent, tool, input, output));
        }
    }
}
