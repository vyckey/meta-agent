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
 * AgentEventBus interface for publishing and subscribing to agent events.
 *
 * @author vyckey
 */
public interface AgentEventBus<E extends AgentEvent> extends AutoCloseable {
    /**
     * Get the global agent event bus.
     *
     * @return the global agent event bus
     */
    static AgentEventBus<AgentEvent> global() {
        return DefaultAgentEventBus.GLOBAL;
    }

    /**
     * Publish an agent event.
     *
     * @param event the agent event to publish
     */
    void publish(E event);

    /**
     * Subscribe to agent events.
     *
     * @param listener the agent event listener to subscribe
     */
    void subscribe(AgentEventListener<E> listener);

    /**
     * Unsubscribe from agent events.
     *
     * @param listener the agent event listener to unsubscribe
     */
    void unsubscribe(AgentEventListener<E> listener);

    /**
     * Unsubscribe all agent event listeners.
     */
    void unsubscribeAll();
}
