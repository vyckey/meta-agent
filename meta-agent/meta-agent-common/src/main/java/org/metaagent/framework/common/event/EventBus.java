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

package org.metaagent.framework.common.event;

import java.util.List;

/**
 * EventBus interface for publishing and subscribing to specialized type events.
 *
 * @param <E> the type of event
 * @param <L> the type of event listener
 * @author vyckey
 * @see Event
 * @see EventListener
 */
public interface EventBus<E extends Event, L extends EventListener<E>> extends AutoCloseable {
    /**
     * Create a new EventBus instance.
     *
     * @param <E> the type of event
     * @param <L> the type of event listener
     * @return a new EventBus instance
     */
    static <E extends Event, L extends EventListener<E>> EventBus<E, L> create() {
        return new DefaultEventBus<>();
    }

    /**
     * Publish an event.
     *
     * @param event the event to publish
     */
    void publish(E event);

    /**
     * Get all event listeners.
     *
     * @return the list of event listeners
     */
    List<L> listeners();

    /**
     * Subscribe to events.
     *
     * @param listener the event listener to subscribe
     */
    void subscribe(L listener);

    /**
     * Unsubscribe from events.
     *
     * @param listener the event listener to unsubscribe
     */
    void unsubscribe(L listener);

    /**
     * Unsubscribe all event listeners.
     */
    void unsubscribeAll();
}
