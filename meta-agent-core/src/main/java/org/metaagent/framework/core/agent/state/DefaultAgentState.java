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
import org.metaagent.framework.core.agent.output.AgentOutput;

import java.util.concurrent.TimeoutException;

/**
 * description is here
 *
 * @author vyckey
 */
public class DefaultAgentState implements AgentState {
    protected AgentRunStatus status = AgentRunStatus.NOT_STARTED;
    protected ActionHistory actionHistory;
    protected AgentOutput agentOutput;
    protected Exception exception;
    protected int loopCount = 0;
    protected int retryCount = 0;

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
    public void incrLoopCount() {
        loopCount++;
    }

    @Override
    public int getRetryCount() {
        return retryCount;
    }

    @Override
    public void incrRetryCount() {
        retryCount++;
    }

    @Override
    public ActionHistory getActionHistory() {
        return actionHistory;
    }

    @Override
    public AgentOutput getAgentOutput() {
        return agentOutput;
    }

    @Override
    public void setAgentOutput(AgentOutput output) {
        this.agentOutput = output;
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
        this.agentOutput = null;
        this.exception = null;
        this.loopCount = 0;
        this.retryCount = 0;
    }
}
