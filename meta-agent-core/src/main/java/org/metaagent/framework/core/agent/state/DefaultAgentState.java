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

package org.metaagent.framework.core.agent.state;

import org.metaagent.framework.core.agent.action.history.ActionHistory;
import org.metaagent.framework.core.agent.action.history.DefaultActionHistory;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.metaagent.framework.core.tool.tracker.DefaultToolCallTracker;
import org.metaagent.framework.core.tool.tracker.ToolCallTracker;

import java.util.concurrent.TimeoutException;

/**
 * description is here
 *
 * @author vyckey
 */
public class DefaultAgentState implements AgentState {
    protected AgentRunStatus status = AgentRunStatus.NOT_STARTED;
    protected ActionHistory actionHistory;
    protected ToolCallTracker toolCallTracker;
    protected Exception exception;
    protected int loopCount = 0;
    protected int retryCount = 0;

    protected DefaultAgentState(Builder builder) {
        this.actionHistory = builder.actionHistory;
        this.toolCallTracker = builder.toolCallTracker;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public AgentRunStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(AgentRunStatus status) {
        this.status = status;
    }

    @Override
    public int getLoopCount() {
        return loopCount;
    }

    @Override
    public int incrLoopCount() {
        return ++loopCount;
    }

    @Override
    public int getRetryCount() {
        return retryCount;
    }

    @Override
    public int incrRetryCount() {
        return ++retryCount;
    }

    @Override
    public ActionHistory getActionHistory() {
        return actionHistory;
    }

    @Override
    public ToolCallTracker getToolCallTracker() {
        return toolCallTracker;
    }

    @Override
    public Exception getLastException() {
        return exception;
    }

    @Override
    public void setLastException(Exception ex) {
        this.exception = ex;
        if (ex == null) return;
        if (ex instanceof TimeoutException) {
            this.status = AgentRunStatus.TIMEOUT;
        } else if (ex instanceof InterruptedException) {
            this.status = AgentRunStatus.INTERRUPTED;
        } else {
            this.status = AgentRunStatus.FAILED;
        }
    }

    @Override
    public void reset() {
        this.status = AgentRunStatus.NOT_STARTED;
        this.actionHistory.clearAll();
        this.toolCallTracker.clear();
        this.exception = null;
        this.loopCount = 0;
        this.retryCount = 0;
    }

    public static class Builder {
        private ActionHistory actionHistory;
        private ToolCallTracker toolCallTracker;

        private Builder() {
        }

        public Builder actionHistory(ActionHistory actionHistory) {
            this.actionHistory = actionHistory;
            return this;
        }

        public Builder toolCallTracker(ToolCallTracker toolCallTracker) {
            this.toolCallTracker = toolCallTracker;
            return this;
        }

        public DefaultAgentState build() {
            if (actionHistory == null) {
                actionHistory = new DefaultActionHistory();
            }
            if (toolCallTracker == null) {
                toolCallTracker = new DefaultToolCallTracker();
            }
            return new DefaultAgentState(this);
        }
    }
}
