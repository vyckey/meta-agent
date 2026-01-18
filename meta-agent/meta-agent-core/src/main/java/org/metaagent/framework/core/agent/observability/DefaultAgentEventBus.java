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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * DefaultAgentEventBus is a default implementation of the AgentEventBus interface.
 *
 * @author vyckey
 */
public class DefaultAgentEventBus<E extends AgentEvent> implements AgentEventBus<E> {
    static final DefaultAgentEventBus<AgentEvent> GLOBAL = new DefaultAgentEventBus<>(new ThreadPoolExecutor(
            1, 1,
            60L, java.util.concurrent.TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(1000),
            r -> new Thread(r, "GlobalAgentEventBus-Thread")
    ));

    private final List<AgentEventListener<E>> eventListeners = Lists.newCopyOnWriteArrayList();
    private final ExecutorService threadPool;

    public DefaultAgentEventBus(ExecutorService threadPool) {
        this.threadPool = Objects.requireNonNull(threadPool, "threadPool is required");
    }

    public DefaultAgentEventBus() {
        this(ForkJoinPool.commonPool());
    }

    @Override
    public void publish(E event) {
        notifyListeners(event);
    }

    private void notifyListeners(E event) {
        for (AgentEventListener<E> listener : eventListeners) {
            if (listener.accept(event)) {
                threadPool.submit(() -> listener.onAgentEvent(event));
            }
        }
    }

    @Override
    public void subscribe(AgentEventListener<E> listener) {
        Objects.requireNonNull(listener, "Listener cannot be null");
        if (!eventListeners.contains(listener)) {
            eventListeners.add(listener);
        }
    }

    @Override
    public void unsubscribe(AgentEventListener<E> listener) {
        eventListeners.remove(listener);
    }

    @Override
    public void unsubscribeAll() {
        eventListeners.clear();
    }

    @Override
    public void close() throws Exception {
        eventListeners.clear();
    }
}
