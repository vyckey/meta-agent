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

package org.metaagent.framework.core.agent.event;

import org.metaagent.framework.common.event.DefaultEventBus;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * DefaultAgentEventBus is a default implementation of the AgentEventBus interface.
 *
 * @author vyckey
 */
public class DefaultAgentEventBus extends DefaultEventBus<AgentEvent, AgentEventListener> implements AgentEventBus {
    public DefaultAgentEventBus(ExecutorService threadPool) {
        super(threadPool);
    }

    public DefaultAgentEventBus() {
        super();
    }

    public static DefaultAgentEventBus create(int corePoolSize) {
        return new DefaultAgentEventBus(new ThreadPoolExecutor(
                corePoolSize, corePoolSize,
                60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1000),
                r -> new Thread(r, "AgentEventBus-Thread")
        ));
    }
}
