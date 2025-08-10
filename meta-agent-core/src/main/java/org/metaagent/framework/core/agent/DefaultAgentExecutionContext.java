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

import lombok.Getter;
import org.metaagent.framework.core.agent.action.executor.ActionExecutor;
import org.metaagent.framework.core.agent.action.executor.SyncActionExecutor;
import org.metaagent.framework.core.environment.Environment;
import org.metaagent.framework.core.tool.executor.DefaultToolExecutor;
import org.metaagent.framework.core.tool.executor.ToolExecutor;
import org.metaagent.framework.core.tool.manager.ToolManager;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * description is here
 *
 * @author vyckey
 */
@Getter
public class DefaultAgentExecutionContext implements AgentExecutionContext {
    private final Environment environment;
    private final ToolManager toolManager;
    private final ToolExecutor toolExecutor;
    private final ActionExecutor actionExecutor;
    private final Executor executor;

    protected DefaultAgentExecutionContext(Builder builder) {
        this.environment = builder.environment;
        this.toolManager = builder.toolManager;
        this.toolExecutor = builder.toolExecutor;
        this.actionExecutor = builder.actionExecutor;
        this.executor = builder.executor;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(AgentExecutionContext context) {
        return new Builder(context);
    }

    public static final class Builder {
        private Environment environment;
        private ToolManager toolManager;
        private ToolExecutor toolExecutor;
        private ActionExecutor actionExecutor;
        private Executor executor;

        private Builder() {
        }

        private Builder(AgentExecutionContext context) {
            this.environment = context.getEnvironment();
            this.toolManager = context.getToolManager();
            this.actionExecutor = context.getActionExecutor();
            this.executor = context.getExecutor();
        }

        public Builder environment(Environment environment) {
            this.environment = environment;
            return this;
        }

        public Builder executor(Executor executor) {
            this.executor = executor;
            return this;
        }

        public Builder toolManager(ToolManager toolManager) {
            this.toolManager = toolManager;
            return this;
        }

        public Builder toolExecutor(ToolExecutor toolExecutor) {
            this.toolExecutor = toolExecutor;
            return this;
        }

        public Builder actionExecutor(ActionExecutor actionExecutor) {
            this.actionExecutor = actionExecutor;
            return this;
        }

        private void setDefault() {
            if (toolManager == null) {
                this.toolManager = ToolManager.create();
            }
            if (toolExecutor == null) {
                this.toolExecutor = DefaultToolExecutor.INSTANCE;
            }
            if (actionExecutor == null) {
                actionExecutor = SyncActionExecutor.INSTANCE;
            }
            if (executor == null) {
                executor = Executors.newSingleThreadExecutor();
            }
        }

        public DefaultAgentExecutionContext build() {
            setDefault();
            return new DefaultAgentExecutionContext(this);
        }
    }
}
