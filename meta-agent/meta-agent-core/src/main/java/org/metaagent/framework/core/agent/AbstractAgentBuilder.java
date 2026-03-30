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

package org.metaagent.framework.core.agent;

import org.metaagent.framework.core.agent.context.AgentStepContext;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.listener.AgentRunListenerRegistry;
import org.metaagent.framework.core.agent.listener.AgentStepListenerRegistry;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.metaagent.framework.core.agent.profile.AgentProfile;

/**
 * AbstractAgentBuilder is an abstract class that provides a common interface for building agents.
 *
 * @param <B> the type of builder
 * @param <I> the input type of agent
 * @param <O> the output type of agent
 * @author vyckey
 */
public abstract class AbstractAgentBuilder<B, I extends AgentInput, O extends AgentOutput, C extends AgentStepContext> {
    protected AgentProfile profile;
    protected AgentRunListenerRegistry<I, O> runListenerRegistry;
    protected AgentStepListenerRegistry<I, O, C> stepListenerRegistry;

    public B agentProfile(AgentProfile profile) {
        this.profile = profile;
        return self();
    }

    public B runListenerRegistry(AgentRunListenerRegistry<I, O> runListenerRegistry) {
        this.runListenerRegistry = runListenerRegistry;
        return self();
    }

    public B stepListenerRegistry(AgentStepListenerRegistry<I, O, C> stepListenerRegistry) {
        this.stepListenerRegistry = stepListenerRegistry;
        return self();
    }

    /**
     * Returns the builder instance.
     *
     * @return The builder instance.
     */
    protected abstract B self();

    /**
     * Builds an agent.
     *
     * @return The built agent.
     */
    public abstract MetaAgent<I, O> build();
}
