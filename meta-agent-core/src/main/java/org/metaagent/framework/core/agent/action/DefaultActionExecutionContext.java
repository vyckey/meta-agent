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

package org.metaagent.framework.core.agent.action;

import lombok.Getter;
import org.metaagent.framework.core.agent.action.executor.ActionExecutor;
import org.metaagent.framework.core.agent.action.executor.SyncActionExecutor;
import org.metaagent.framework.core.agent.action.history.ActionHistory;
import org.metaagent.framework.core.agent.action.history.DefaultActionHistory;
import org.metaagent.framework.core.environment.Environment;
import org.metaagent.framework.core.tool.manager.DefaultToolManager;
import org.metaagent.framework.core.tool.manager.ToolManager;

/**
 * description is here
 *
 * @author vyckey
 */
@Getter
public class DefaultActionExecutionContext implements ActionExecuteContext {
    protected final Environment environment;
    protected final ToolManager toolManager;
    protected final ActionExecutor actionExecutor;
    protected final ActionHistory actionHistory;

    protected DefaultActionExecutionContext(Environment environment,
                                            ToolManager toolManager,
                                            ActionExecutor actionExecutor,
                                            ActionHistory actionHistory) {
        this.environment = environment;
        this.toolManager = toolManager;
        this.actionExecutor = actionExecutor;
        this.actionHistory = actionHistory;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(ActionExecuteContext context) {
        return new Builder(context);
    }

    public static final class Builder {
        private Environment environment;
        private ToolManager toolManager;
        private ActionExecutor actionExecutor;
        private ActionHistory actionHistory;

        private Builder() {
        }

        private Builder(ActionExecuteContext context) {
            this.environment = context.getEnvironment();
            this.toolManager = context.getToolManager();
            this.actionExecutor = context.getActionExecutor();
            this.actionHistory = context.getActionHistory();
        }

        public Builder environment(Environment environment) {
            this.environment = environment;
            return this;
        }

        public Builder toolManager(ToolManager toolManager) {
            this.toolManager = toolManager;
            return this;
        }

        public Builder actionExecutor(ActionExecutor actionExecutor) {
            this.actionExecutor = actionExecutor;
            return this;
        }

        public Builder actionHistory(ActionHistory actionHistory) {
            this.actionHistory = actionHistory;
            return this;
        }

        private void setDefault() {
            if (toolManager == null) {
                this.toolManager = DefaultToolManager.getInstance();
            }
            if (actionExecutor == null) {
                actionExecutor = SyncActionExecutor.INSTANCE;
            }
            if (actionHistory == null) {
                actionHistory = new DefaultActionHistory();
            }
        }

        public DefaultActionExecutionContext build() {
            setDefault();
            return new DefaultActionExecutionContext(environment, toolManager, actionExecutor, actionHistory);
        }
    }
}
