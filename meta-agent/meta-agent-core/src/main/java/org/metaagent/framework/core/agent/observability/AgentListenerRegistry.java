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

import org.metaagent.framework.core.agent.observability.event.AgentEvent;

/**
 * AgentListenerRegistry is a registry for agent listeners.
 *
 * @author vyckey
 */
public interface AgentListenerRegistry<I, O> {
    /**
     * Registers a run listener.
     *
     * @param listener the listener to register
     */
    void registerRunListener(AgentRunListener<I, O> listener);

    /**
     * Unregisters a run listener.
     *
     * @param listener the listener to unregister
     */
    void unregisterRunListener(AgentRunListener<I, O> listener);

    /**
     * Unregisters all run listeners.
     */
    void unregisterRunListeners();

    /**
     * Registers a step listener.
     *
     * @param listener the listener to register
     */
    void registerStepListener(AgentStepListener<I, O> listener);

    /**
     * Unregisters a step listener.
     *
     * @param listener the listener to unregister
     */
    void unregisterStepListener(AgentStepListener<I, O> listener);

    /**
     * Unregisters all step listeners.
     */
    void unregisterStepListeners();

    /**
     * Registers an event listener.
     *
     * @param listener the listener to register
     */
    void registerEventListener(AgentEventListener<AgentEvent> listener);

    /**
     * Unregisters an event listener.
     *
     * @param listener the listener to unregister
     */
    void unregisterEventListener(AgentEventListener<AgentEvent> listener);

    /**
     * Unregisters all event listeners.
     */
    void unregisterEventListeners();
}
