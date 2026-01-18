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
import org.metaagent.framework.core.tool.tracker.ToolCallTracker;

/**
 * AgentState interface is used to hold the state of an agent.
 *
 * @author vyckey
 */
public interface AgentState {
    /**
     * Get the status of the agent.
     *
     * @return the status of the agent
     */
    AgentRunStatus getStatus();

    /**
     * Set the status of the agent.
     *
     * @param status the status of the agent
     */
    void setStatus(AgentRunStatus status);

    /**
     * Get the agent step state of the agent.
     *
     * @return the agent step state of the agent
     */
    AgentStepState getStepState();

    /**
     * Reset the agent step state.
     *
     * @return the current agent step state of the agent
     */
    AgentStepState resetStepState();

    /**
     * Get the action history of the agent.
     *
     * @return the action history of the agent
     */
    ActionHistory getActionHistory();

    /**
     * Get the tool call tracker of the agent.
     *
     * @return the tool call tracker of the agent
     */
    ToolCallTracker getToolCallTracker();

    /**
     * Reset the agent state.
     */
    void reset();
}
