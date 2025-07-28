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

package org.metaagent.framework.core.agents.task.decompose;

import lombok.Getter;
import org.metaagent.framework.core.agent.AgentExecutionContext;
import org.metaagent.framework.core.agent.input.AbstractAgentInput;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.task.Task;
import org.metaagent.framework.core.common.metadata.MapMetadataProvider;

import java.util.Objects;

/**
 * Input for the Task Decompose Agent.
 *
 * @author vyckey
 */
@Getter
public class TaskDecomposeInput extends AbstractAgentInput implements AgentInput {
    private final Task task;
    private final String taskContext;

    protected TaskDecomposeInput(Builder builder) {
        super(builder);
        this.task = Objects.requireNonNull(builder.task, "task is required");
        this.taskContext = builder.taskContext;
    }

    public TaskDecomposeInput(Task task) {
        this(builder().task(task));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractAgentInput.Builder<Builder> {
        private Task task;
        private String taskContext;

        @Override
        protected Builder self() {
            return this;
        }

        public Builder task(Task task) {
            this.task = task;
            return this;
        }

        public Builder taskContext(String taskContext) {
            this.taskContext = taskContext;
            return this;
        }

        public TaskDecomposeInput build() {
            if (context == null) {
                context = AgentExecutionContext.create();
            }
            if (metadata == null) {
                metadata = new MapMetadataProvider();
            }
            return new TaskDecomposeInput(this);
        }
    }
}
