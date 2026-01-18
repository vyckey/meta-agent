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
 * Agent run listener registry.
 *
 * @author vyckey
 */
public interface AgentRunListenerRegistry<I, O> {
    /**
     * Returns the list of run listeners.
     *
     * @return the list of run listeners
     */
    List<AgentRunListener<I, O>> getRunListeners();

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
}
