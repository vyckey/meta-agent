/*
 * MIT License
 *
 * Copyright (c) 2025 MetaAgent
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

package org.metaagent.framework.core.agent;

import org.metaagent.framework.common.abort.AbortException;
import org.metaagent.framework.core.agent.exception.AgentExecutionException;
import org.metaagent.framework.core.agent.exception.AgentInterruptedException;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.listener.AgentRunListener;
import org.metaagent.framework.core.agent.listener.AgentRunListenerRegistry;
import org.metaagent.framework.core.agent.listener.DefaultAgentListenerRegistry;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.metaagent.framework.core.agent.profile.AgentProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Abstract {@link MetaAgent} implementation.
 *
 * @param <I> the type of agent input
 * @param <O> the type of agent output
 * @author vyckey
 */
public abstract class AbstractMetaAgent<I extends AgentInput, O extends AgentOutput>
        implements MetaAgent<I, O> {

    protected String agentName;
    protected AgentProfile profile;
    protected volatile boolean initialized = false;

    protected AgentLogger agentLogger;
    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected AgentRunListenerRegistry<I, O> runListenerRegistry;

    protected AbstractMetaAgent(String name) {
        this.agentName = name;
        this.agentLogger = AgentLogger.getLogger(name);
        this.runListenerRegistry = new DefaultAgentListenerRegistry<>();
    }

    protected AbstractMetaAgent(AgentProfile profile) {
        this(profile.getName());
        this.profile = profile;
    }

    protected AbstractMetaAgent(AbstractAgentBuilder<?, I, O, ?> builder) {
        this.profile = Objects.requireNonNull(builder.profile, "agent profile is required");
        this.agentName = builder.profile.getName();
        this.agentLogger = AgentLogger.getLogger(this.agentName);
        this.runListenerRegistry = builder.runListenerRegistry == null
                ? new DefaultAgentListenerRegistry<>() : builder.runListenerRegistry;
    }

    @Override
    public final String name() {
        return agentName;
    }

    @Override
    public AgentProfile profile() {
        return profile;
    }

    public AgentRunListenerRegistry<I, O> getRunListenerRegistry() {
        return runListenerRegistry;
    }

    /**
     * Initializes the agent if not initialized.
     */
    public void initialize() {
        if (initialized) {
            return;
        }
        synchronized (this) {
            if (initialized) {
                return;
            }
            doInitialize();
            initialized = true;
        }
    }

    protected void doInitialize() {
        // The child class should implement this method to initialize the agent.
    }

    protected <T> void notifyListeners(Iterable<T> listeners, Consumer<T> consumer) {
        for (T listener : listeners) {
            try {
                consumer.accept(listener);
            } catch (Exception e) {
                logger.error("Fail to invoke listener", e);
            }
        }
    }

    /**
     * Handles exceptions thrown by the callable.
     *
     * @param callable the callable to execute
     * @param <V>      the type of the callable result
     * @return the callable result
     */
    protected <V> V handleExceptionIfRequired(Callable<V> callable) {
        try {
            return callable.call();
        } catch (AbortException ex) {
            throw new AgentInterruptedException("agent is interrupted", ex);
        } catch (AgentExecutionException | AgentInterruptedException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AgentExecutionException("agent execution failed", ex);
        }
    }

    /**
     * Preprocess agent input before running.
     *
     * @param input the agent input
     * @return the preprocessed agent input
     */
    protected I preprocess(I input) {
        if (!initialized) {
            initialize();
        }
        return input;
    }

    @Override
    public O run(I agentInput) {
        I input = preprocess(agentInput);

        return handleExceptionIfRequired(() -> {
            List<AgentRunListener<I, O>> runListeners = getRunListenerRegistry().getRunListeners();
            try {
                notifyListeners(runListeners, listener -> listener.onAgentStart(this, input));

                O output = doRun(input);

                notifyListeners(runListeners, listener -> listener.onAgentOutput(this, input, output));
                return output;
            } catch (Exception ex) {
                notifyListeners(runListeners, listener -> listener.onAgentException(this, input, ex));
                throw ex;
            }
        });
    }

    /**
     * Performs agent run.
     *
     * @param input the agent input
     * @return the agent output
     */
    protected abstract O doRun(I input);

    @Override
    public void reset() {
        this.initialized = false;
    }

    @Override
    public String toString() {
        return this.getClass().getTypeName() + "{name=" + name() + "}";
    }
}
