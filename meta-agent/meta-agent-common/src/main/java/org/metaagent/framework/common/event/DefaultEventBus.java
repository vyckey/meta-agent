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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation of EventBus.
 * Provides thread-safe event publishing and subscription management.
 *
 * @param <E> the type of event
 * @param <L> the type of event listener
 * @author vyckey
 */
public class DefaultEventBus<E extends Event, L extends EventListener<E>> implements EventBus<E, L> {
    private static final Logger log = LoggerFactory.getLogger(DefaultEventBus.class);
    private final Set<L> listeners;
    private final ExecutorService threadPool;
    private volatile boolean closed = false;

    /**
     * Creates a new DefaultEventBus instance with custom listener storage.
     *
     * @param listeners  the set to store listeners (must be thread-safe)
     * @param threadPool the executor service to use for asynchronous event processing, or synchronous if null
     */
    protected DefaultEventBus(Set<L> listeners, ExecutorService threadPool) {
        this.listeners = Objects.requireNonNull(listeners, "listeners cannot be null");
        this.threadPool = threadPool;
    }

    /**
     * Creates a new DefaultEventBus instance.
     *
     * @param threadPool the executor service to use for asynchronous event processing, or synchronous if null
     */
    public DefaultEventBus(ExecutorService threadPool) {
        this(new CopyOnWriteArraySet<>(), threadPool);
    }

    /**
     * Creates a new DefaultEventBus instance.
     *
     */
    public DefaultEventBus() {
        this(new CopyOnWriteArraySet<>(), null);
    }

    /**
     * Checks if the event bus is closed.
     *
     * @return true if closed, false otherwise
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Returns the current number of registered listeners.
     *
     * @return the number of listeners
     */
    public int getListenerCount() {
        return listeners.size();
    }

    /**
     * Returns an unmodifiable view of the registered listeners.
     *
     * @return unmodifiable set of listeners
     */
    @Override
    public List<L> listeners() {
        return listeners.stream().toList();
    }

    @Override
    public void publish(E event) {
        if (closed) {
            throw new IllegalStateException("EventBus is already closed");
        }
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }

        notifyListeners(event);
    }

    protected void notifyListeners(E event) {
        if (threadPool != null) {
            threadPool.submit(() -> {
                for (L listener : listeners) {
                    try {
                        listener.onEvent(event);
                    } catch (Exception e) {
                        handleListenerException(listener, event, e);
                    }
                }
            });
        } else {
            for (L listener : listeners) {
                try {
                    listener.onEvent(event);
                } catch (Exception e) {
                    handleListenerException(listener, event, e);
                }
            }
        }
    }

    /**
     * Handles exceptions thrown by listeners during event publishing.
     * Can be overridden by subclasses to provide custom error handling.
     *
     * @param listener  the listener that threw the exception
     * @param event     the event being published
     * @param exception the exception that was thrown
     */
    protected void handleListenerException(L listener, E event, Exception exception) {
        log.error("Failed to publish event {} to listener {}", event, listener, exception);
    }

    @Override
    public void subscribe(L listener) {
        if (closed) {
            throw new IllegalStateException("EventBus is already closed");
        }

        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }

        listeners.add(listener);
    }

    @Override
    public void unsubscribe(L listener) {
        if (closed) {
            throw new IllegalStateException("EventBus is already closed");
        }
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }

        listeners.remove(listener);
    }

    @Override
    public void unsubscribeAll() {
        if (closed) {
            throw new IllegalStateException("EventBus is already closed");
        }
        listeners.clear();
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }

        closed = true;
        unsubscribeAll();

        if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}