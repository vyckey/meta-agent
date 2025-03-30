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
import org.metaagent.framework.core.agent.goal.Goal;
import org.metaagent.framework.core.agent.state.AgentState;
import org.metaagent.framework.core.agent.state.DefaultAgentState;
import org.metaagent.framework.core.environment.Environment;
import org.metaagent.framework.core.tool.manager.DefaultToolManager;
import org.metaagent.framework.core.tool.manager.ToolManager;
import org.metaagent.framework.core.tool.tracker.DefaultToolCallTracker;
import org.metaagent.framework.core.tool.tracker.ToolCallTracker;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * description is here
 *
 * @author vyckey
 */
@Getter
public class DefaultAgentExecutionContext implements AgentExecutionContext {
    private final Goal goal;
    private final Environment environment;
    private final ToolManager toolManager;
    private final ToolCallTracker toolCallTracker;
    private final AgentState agentState;
    private final ActionExecutor actionExecutor;
    private final Executor executor;

    protected DefaultAgentExecutionContext(Builder builder) {
        this.goal = builder.goal;
        this.environment = builder.environment;
        this.toolManager = builder.toolManager;
        this.toolCallTracker = builder.toolCallTracker;
        this.agentState = builder.agentState;
        this.actionExecutor = builder.actionExecutor;
        this.executor = builder.executor;
    }

    @Override
    public void reset() {
        toolCallTracker.clear();
        agentState.reset();
    }

    public static Builder builder(Goal goal) {
        return new Builder(goal);
    }

    public static Builder builder(AgentExecutionContext context) {
        return new Builder(context);
    }

    public static final class Builder {
        private final Goal goal;
        private Environment environment;
        private ToolManager toolManager;
        private ToolCallTracker toolCallTracker;
        private ActionExecutor actionExecutor;
        private AgentState agentState;
        private Executor executor;

        private Builder(Goal goal) {
            this.goal = goal;
        }

        private Builder(AgentExecutionContext context) {
            this.goal = context.getGoal();
            this.environment = context.getEnvironment();
            this.toolManager = context.getToolManager();
            this.actionExecutor = context.getActionExecutor();
            this.agentState = context.getAgentState();
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

        public Builder toolCallTracker(ToolCallTracker toolCallTracker) {
            this.toolCallTracker = toolCallTracker;
            return this;
        }

        public Builder actionExecutor(ActionExecutor actionExecutor) {
            this.actionExecutor = actionExecutor;
            return this;
        }

        public Builder agentState(AgentState agentState) {
            this.agentState = agentState;
            return this;
        }

        private void setDefault() {
            if (agentState == null) {
                this.agentState = new DefaultAgentState();
            }
            if (toolManager == null) {
                this.toolManager = DefaultToolManager.getInstance();
            }
            if (toolCallTracker == null) {
                this.toolCallTracker = new DefaultToolCallTracker();
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
