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

import org.metaagent.framework.common.abort.AbortController;
import org.metaagent.framework.common.abort.AbortSignal;
import org.metaagent.framework.core.agent.event.AgentEventBus;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * AbstractAgentContextBuilder is an abstract class that provides a common implementation for building an {@link AgentContext}.
 *
 * @author vyckey
 */
public abstract class AbstractAgentContextBuilder<Builder extends AgentContextBuilder<Builder>>
        implements AgentContextBuilder<Builder> {
    protected AgentEventBus agentEventBus;
    protected AbortSignal abortSignal;
    protected Executor executor;

    protected AbstractAgentContextBuilder() {
    }

    protected AbstractAgentContextBuilder(AgentContext context) {
        this.agentEventBus = context.agentEventBus();
        this.abortSignal = context.abortSignal();
        this.executor = context.executor();
    }

    protected abstract Builder self();

    @Override
    public Builder agentEventBus(AgentEventBus agentEventBus) {
        this.agentEventBus = agentEventBus;
        return self();
    }

    @Override
    public Builder abortSignal(AbortSignal abortSignal) {
        this.abortSignal = abortSignal;
        return self();
    }

    @Override
    public Builder executor(Executor executor) {
        this.executor = executor;
        return self();
    }

    protected Builder withDefaults() {
        if (agentEventBus == null) {
            agentEventBus = AgentEventBus.create();
        }
        if (abortSignal == null) {
            abortSignal = AbortController.global().signal();
        }
        if (executor == null) {
            executor = ForkJoinPool.commonPool();
        }
        return self();
    }

    @Override
    public abstract AgentContext build();
}
