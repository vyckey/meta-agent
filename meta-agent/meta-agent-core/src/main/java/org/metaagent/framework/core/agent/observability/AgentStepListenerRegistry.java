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

import java.util.List;

/**
 * AgentStepListenerRegistry is a registry for agent step listeners.
 *
 * @author vyckey
 */
public interface AgentStepListenerRegistry<I, O> {
    /**
     * Returns the list of step listeners.
     *
     * @return the list of step listeners
     */
    List<AgentStepListener<I, O>> getStepListeners();

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
}
