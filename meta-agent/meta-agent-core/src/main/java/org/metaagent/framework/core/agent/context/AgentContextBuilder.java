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

package org.metaagent.framework.core.agent.context;

import org.metaagent.framework.common.abort.AbortSignal;
import org.metaagent.framework.core.agent.event.AgentEventBus;

import java.util.concurrent.Executor;

/**
 * Builder class for {@link AgentContext}
 *
 * @author vyckey
 * @see AgentContext
 */
public interface AgentContextBuilder<Builder extends AgentContextBuilder<Builder>> {

    /**
     * Sets the agent event bus for the agent execution context
     *
     * @param agentEventBus the event bus for agent events
     * @return this builder instance for method chaining
     */
    Builder agentEventBus(AgentEventBus agentEventBus);

    /**
     * Sets the abort signal for the agent execution context
     *
     * @param abortSignal the signal used to abort agent execution
     * @return this builder instance for method chaining
     */
    Builder abortSignal(AbortSignal abortSignal);

    /**
     * Sets the executor for the agent execution context
     *
     * @param executor the executor service for asynchronous operations
     * @return this builder instance for method chaining
     */
    Builder executor(Executor executor);

    /**
     * Builds and returns the {@link AgentContext} instance
     *
     * @return a new {@link AgentContext} instance with the configured components
     */
    AgentContext build();
}
