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

package org.metaagent.framework.core.agent.observability;

import com.google.common.collect.Lists;
import org.metaagent.framework.core.agent.observability.event.AgentEvent;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * DefaultAgentListenerRegistry is a default implementation of {@link AgentListenerRegistry}.
 *
 * @author vyckey
 */
public record DefaultAgentListenerRegistry<I, O>(
        List<AgentEventListener<AgentEvent>> eventListeners,
        List<AgentRunListener<I, O>> runListeners,
        List<AgentStepListener<I, O>> stepListeners
) implements AgentListenerRegistry<I, O> {
    public DefaultAgentListenerRegistry {
        Objects.requireNonNull(eventListeners, "eventListeners is required");
        Objects.requireNonNull(runListeners, "runListeners is required");
        Objects.requireNonNull(stepListeners, "stepListeners is required");
    }

    public DefaultAgentListenerRegistry() {
        this(Lists.newCopyOnWriteArrayList(), Lists.newCopyOnWriteArrayList(), Lists.newCopyOnWriteArrayList());
    }

    @Override
    public List<AgentEventListener<AgentEvent>> getEventListener() {
        return Collections.unmodifiableList(eventListeners);
    }

    @Override
    public void registerEventListener(AgentEventListener<AgentEvent> listener) {
        this.eventListeners.add(listener);
    }

    @Override
    public void unregisterEventListener(AgentEventListener<AgentEvent> listener) {
        this.eventListeners.remove(listener);
    }

    @Override
    public void unregisterEventListeners() {
        this.eventListeners.clear();
    }

    @Override
    public List<AgentRunListener<I, O>> getRunListeners() {
        return Collections.unmodifiableList(runListeners);
    }

    @Override
    public void registerRunListener(AgentRunListener<I, O> listener) {
        this.runListeners.add(listener);
    }

    @Override
    public void unregisterRunListener(AgentRunListener<I, O> listener) {
        this.runListeners.remove(listener);
    }

    @Override
    public void unregisterRunListeners() {
        this.runListeners.clear();
    }

    @Override
    public List<AgentStepListener<I, O>> getStepListeners() {
        return Collections.unmodifiableList(stepListeners);
    }

    @Override
    public void registerStepListener(AgentStepListener<I, O> listener) {
        this.stepListeners.add(listener);
    }

    @Override
    public void unregisterStepListener(AgentStepListener<I, O> listener) {
        this.stepListeners.remove(listener);
    }

    @Override
    public void unregisterStepListeners() {
        this.stepListeners.clear();
    }
}
