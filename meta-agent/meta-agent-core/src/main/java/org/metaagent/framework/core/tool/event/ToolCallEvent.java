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

package org.metaagent.framework.core.tool.event;

import com.google.common.base.Preconditions;
import org.metaagent.framework.core.agent.MetaAgent;
import org.metaagent.framework.core.tool.Tool;

import java.time.Instant;
import java.util.Objects;

/**
 * ToolCallEvent represents a tool call event.
 *
 * @author vyckey
 */
public record ToolCallEvent(
        MetaAgent<?, ?> agent,
        String executionId,
        Tool<?, ?> tool,
        String toolArgs,
        Object toolInput,
        Instant occurredTime
) implements AgentToolEvent {
    public ToolCallEvent {
        Objects.requireNonNull(agent, "agent is required");
        Objects.requireNonNull(executionId, "executionId is required");
        Objects.requireNonNull(tool, "tool is required");
        Preconditions.checkArgument(toolArgs != null || toolInput != null, "toolArgs or toolInput is required");
        occurredTime = occurredTime != null ? occurredTime : Instant.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private MetaAgent<?, ?> agent;
        private String executionId;
        private Tool<?, ?> tool;
        private String toolArgs;
        private Object toolInput;
        private Instant occurredTime;

        public Builder agent(MetaAgent<?, ?> agent) {
            this.agent = agent;
            return this;
        }

        public Builder executionId(String executionId) {
            this.executionId = executionId;
            return this;
        }

        public Builder tool(Tool<?, ?> tool) {
            this.tool = tool;
            return this;
        }

        public Builder toolArgs(String toolArgs) {
            this.toolArgs = toolArgs;
            return this;
        }

        public Builder toolInput(Object toolInput) {
            this.toolInput = toolInput;
            return this;
        }

        public Builder occurredTime(Instant occurredTime) {
            this.occurredTime = occurredTime;
            return this;
        }

        public ToolCallEvent build() {
            return new ToolCallEvent(agent, executionId, tool, toolArgs, toolInput, occurredTime);
        }
    }
}
