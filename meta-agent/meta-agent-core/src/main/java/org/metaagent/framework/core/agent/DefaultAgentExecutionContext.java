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
import org.metaagent.framework.common.abort.AbortController;
import org.metaagent.framework.common.abort.AbortSignal;
import org.metaagent.framework.core.agent.action.executor.ActionExecutor;
import org.metaagent.framework.core.agent.action.executor.SyncActionExecutor;
import org.metaagent.framework.core.environment.Environment;
import org.metaagent.framework.core.tool.executor.DefaultToolExecutor;
import org.metaagent.framework.core.tool.executor.ToolExecutor;
import org.metaagent.framework.core.tool.listener.ToolExecuteListenerRegistry;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Default implementation of {@link AgentExecutionContext}.
 *
 * @author vyckey
 */
@Getter
public class DefaultAgentExecutionContext implements AgentExecutionContext {
    private final Environment environment;
    private final ToolExecutor toolExecutor;
    private final ToolExecuteListenerRegistry toolListenerRegistry;
    private final ActionExecutor actionExecutor;
    private final AbortSignal abortSignal;
    private final Executor executor;

    protected DefaultAgentExecutionContext(Builder builder) {
        this.environment = builder.environment;
        this.toolExecutor = builder.toolExecutor;
        this.toolListenerRegistry = builder.toolListenerRegistry;
        this.actionExecutor = builder.actionExecutor;
        this.abortSignal = builder.abortSignal;
        this.executor = builder.executor;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(AgentExecutionContext context) {
        return new Builder(context);
    }

    public static final class Builder implements AgentExecutionContextBuilder {
        private Environment environment;
        private ToolExecutor toolExecutor;
        private ToolExecuteListenerRegistry toolListenerRegistry;
        private ActionExecutor actionExecutor;
        private AbortSignal abortSignal;
        private Executor executor;

        private Builder() {
        }

        private Builder(AgentExecutionContext context) {
            this.environment = context.getEnvironment();
            this.actionExecutor = context.getActionExecutor();
            this.toolListenerRegistry = context.getToolListenerRegistry();
            this.executor = context.getExecutor();
            this.abortSignal = context.getAbortSignal();
            this.executor = context.getExecutor();
        }

        @Override
        public Builder environment(Environment environment) {
            this.environment = environment;
            return this;
        }

        @Override
        public Builder toolExecutor(ToolExecutor toolExecutor) {
            this.toolExecutor = toolExecutor;
            return this;
        }

        @Override
        public Builder toolListenerRegistry(ToolExecuteListenerRegistry toolExecuteListenerRegistry) {
            this.toolListenerRegistry = toolExecuteListenerRegistry;
            return this;
        }

        @Override
        public Builder actionExecutor(ActionExecutor actionExecutor) {
            this.actionExecutor = actionExecutor;
            return this;
        }

        @Override
        public AgentExecutionContextBuilder abortSignal(AbortSignal abortSignal) {
            this.abortSignal = abortSignal;
            return this;
        }

        @Override
        public Builder executor(Executor executor) {
            this.executor = executor;
            return this;
        }

        private void setDefault() {
            if (toolExecutor == null) {
                this.toolExecutor = DefaultToolExecutor.INSTANCE;
            }
            if (toolListenerRegistry == null) {
                this.toolListenerRegistry = ToolExecuteListenerRegistry.DEFAULT;
            }
            if (actionExecutor == null) {
                actionExecutor = SyncActionExecutor.INSTANCE;
            }
            if (abortSignal == null) {
                abortSignal = AbortController.global().signal();
            }
            if (executor == null) {
                executor = Executors.newSingleThreadExecutor();
            }
        }

        @Override
        public DefaultAgentExecutionContext build() {
            setDefault();
            return new DefaultAgentExecutionContext(this);
        }
    }
}
