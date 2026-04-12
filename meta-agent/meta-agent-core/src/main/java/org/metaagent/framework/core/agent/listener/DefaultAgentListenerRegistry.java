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

package org.metaagent.framework.core.agent.listener;

import com.google.common.collect.Lists;
import org.metaagent.framework.core.agent.context.AgentStepContext;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.output.AgentOutput;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * DefaultAgentListenerRegistry is a default implementation of {@link AgentRunListenerRegistry} and {@link AgentStepListenerRegistry}.
 *
 * @author vyckey
 */
public record DefaultAgentListenerRegistry<
        I extends AgentInput,
        O extends AgentOutput,
        C extends AgentStepContext>(
        List<AgentRunListener<I, O>> runListeners,
        List<AgentStepListener<I, O, C>> stepListeners
) implements AgentRunListenerRegistry<I, O>, AgentStepListenerRegistry<I, O, C> {
    public DefaultAgentListenerRegistry {
        Objects.requireNonNull(runListeners, "runListeners is required");
        Objects.requireNonNull(stepListeners, "stepListeners is required");
    }

    public DefaultAgentListenerRegistry() {
        this(Lists.newCopyOnWriteArrayList(), Lists.newCopyOnWriteArrayList());
    }

    @Override
    public List<AgentRunListener<I, O>> getRunListeners() {
        return Collections.unmodifiableList(runListeners);
    }

    @Override
    public void registerRunListener(AgentRunListener<I, O> listener) {
        Objects.requireNonNull(listener, "run listener is required");
        runListeners.add(listener);
    }

    @Override
    public void unregisterRunListener(AgentRunListener<I, O> listener) {
        Objects.requireNonNull(listener, "run listener is required");
        runListeners.remove(listener);
    }

    @Override
    public void unregisterRunListeners() {
        runListeners.clear();
    }

    @Override
    public List<AgentStepListener<I, O, C>> getStepListeners() {
        return Collections.unmodifiableList(stepListeners);
    }

    @Override
    public void registerStepListener(AgentStepListener<I, O, C> listener) {
        Objects.requireNonNull(listener, "step listener is required");
        stepListeners.add(listener);
    }

    @Override
    public void unregisterStepListener(AgentStepListener<I, O, C> listener) {
        Objects.requireNonNull(listener, "step listener is required");
        stepListeners.remove(listener);
    }

    @Override
    public void unregisterStepListeners() {
        stepListeners.clear();
    }
}
