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

package org.metaagent.framework.core.agent.observability.event;

import org.metaagent.framework.core.agent.MetaAgent;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.exception.ToolExecutionException;

import java.time.Instant;
import java.util.Objects;

/**
 * AgentToolErrorEvent is an event that occurs when an agent encounters an error while executing a tool.
 *
 * @author vyckey
 */
public record AgentToolErrorEvent(
        MetaAgent<?, ?> agent,
        Tool<?, ?> tool,
        String request,
        ToolExecutionException error,
        Instant occurredTime
) implements AgentToolEvent {
    public AgentToolErrorEvent {
        Objects.requireNonNull(agent, "agent is required");
        Objects.requireNonNull(tool, "tool is required");
        Objects.requireNonNull(request, "request is required");
        Objects.requireNonNull(error, "error is required");
        if (occurredTime == null) {
            occurredTime = Instant.now();
        }
    }
}
