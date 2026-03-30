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
import org.metaagent.framework.core.agent.context.AgentStepContext;
import org.metaagent.framework.core.agent.exception.AgentExecutionException;
import org.metaagent.framework.core.agent.exception.AgentInterruptedException;
import org.metaagent.framework.core.agent.fallback.AgentFallbackStrategy;
import org.metaagent.framework.core.agent.fallback.FastFailAgentFallbackStrategy;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.listener.AgentRunListener;
import org.metaagent.framework.core.agent.listener.AgentStepListener;
import org.metaagent.framework.core.agent.listener.AgentStepListenerRegistry;
import org.metaagent.framework.core.agent.listener.DefaultAgentListenerRegistry;
import org.metaagent.framework.core.agent.output.AgentStreamOutput;
import org.metaagent.framework.core.agent.profile.AgentProfile;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Abstract {@link StreamingAgent} implementation.
 *
 * @author vyckey
 */
public abstract class AbstractStreamAgent<
        I extends AgentInput,
        O extends AgentStreamOutput<S>,
        C extends AgentStepContext,
        S>
        extends AbstractMetaAgent<I, O> implements StreamingAgent<I, O, C, S> {
    protected AgentStepListenerRegistry<I, O, C> stepListenerRegistry;

    protected AbstractStreamAgent(String name) {
        super(name);
        this.stepListenerRegistry = new DefaultAgentListenerRegistry<>();
    }

    protected AbstractStreamAgent(AgentProfile profile) {
        super(profile);
    }

    protected AbstractStreamAgent(AbstractAgentBuilder<?, I, O, C> builder) {
        super(builder);
        this.stepListenerRegistry = builder.stepListenerRegistry != null
                ? builder.stepListenerRegistry : new DefaultAgentListenerRegistry<>();
    }

    public AgentStepListenerRegistry<I, O, C> getStepListenerRegistry() {
        return stepListenerRegistry;
    }

    public AgentFallbackStrategy<Agent<I, O, C>, I, O> getFallbackStrategy() {
        return new FastFailAgentFallbackStrategy<>();
    }

    protected Flux<S> handleExceptionIfRequired(Flux<S> stream) {
        return stream
                .doOnError(throwable -> {
                    if (throwable instanceof AbortException ex) {
                        throw ex;
                    } else if (throwable instanceof AgentExecutionException ex) {
                        throw ex;
                    } else if (throwable instanceof AgentInterruptedException ie) {
                        throw ie;
                    } else {
                        throw new AgentExecutionException("agent execution failed", throwable);
                    }
                });
    }

    /**
     * Run the agent in streaming mode.
     *
     * @param input The input to run the agent with
     * @return the output of the agent
     */
    @Override
    public O run(I input) {
        I agentInput = preprocess(input);
        C stepContext = createStepContext(agentInput);

        Flux<S> stream = runStream(agentInput, stepContext);
        return buildAgentOutput(agentInput, stepContext, stream);
    }

    @Override
    protected final O doRun(I input) {
        throw new UnsupportedOperationException("doRun is not supported for streaming agent");
    }

    /**
     * Builds the agent output.
     *
     * @param agentInput  the agent input
     * @param stepContext the agent step context
     * @param stream      the agent stream output
     * @return the agent output
     */
    protected abstract O buildAgentOutput(I agentInput, C stepContext, Flux<S> stream);

    /**
     * Determines whether the agent should continue looping.
     *
     * @param agentInput  the agent input
     * @param stepContext the agent step context
     * @return true if the agent should continue looping, false otherwise
     */
    protected abstract boolean shouldContinueLoop(I agentInput, C stepContext);

    /**
     * Fallback while stream execution failed.
     *
     * @param agentInput The input for the agent.
     * @param ex         The exception that caused the fallback.
     * @return The fallback stream.
     */
    protected Flux<S> fallbackStream(I agentInput, Exception ex) {
        return Flux.error(ex);
    }

    @Override
    public Flux<S> runStream(I agentInput, C stepContext) {
        AbstractStreamAgent<I, O, C, S> agent = this;

        List<AgentRunListener<I, O>> runListeners = getRunListenerRegistry().getRunListeners();

        Flux<S> stream = doRunStream(agentInput, stepContext);
        Flux<S> wrappedStream = stream
                .doOnSubscribe(sub ->
                        notifyListeners(runListeners, listener -> listener.onAgentStart(agent, agentInput))
                )
                .onErrorResume(throwable -> {
                    if (throwable instanceof Exception ex) {
                        return fallbackStream(agentInput, ex);
                    }
                    return Flux.error(throwable);
                })
                .doOnComplete(() -> notifyListeners(runListeners, listener -> {
                    O agentOutput = buildAgentOutput(agentInput, stepContext, stream);
                    listener.onAgentOutput(agent, agentInput, agentOutput);
                }))
                .doOnError(throwable -> {
                    if (throwable instanceof Exception ex) {
                        notifyListeners(runListeners, listener -> listener.onAgentException(agent, agentInput, ex));
                    } else {
                        AgentExecutionException ex = new AgentExecutionException("agent execution failed", throwable);
                        notifyListeners(runListeners, listener -> listener.onAgentException(agent, agentInput, ex));
                    }
                });
        return handleExceptionIfRequired(wrappedStream);
    }

    /**
     * Run the streaming agent with looping.
     *
     * @param agentInput  The input to run the agent with
     * @param stepContext The step context
     * @return the output of the agent
     */
    protected Flux<S> doRunStream(I agentInput, C stepContext) {
        return stepStream(agentInput, stepContext)
                .concatWith(Flux.defer(() -> {
                    if (shouldContinueLoop(agentInput, stepContext)) {
                        stepContext.getLoopCounter().incrementAndGet();
                        return stepStream(buildNextInput(agentInput, stepContext), stepContext);
                    } else {
                        return Flux.empty();
                    }
                }));
    }

    /**
     * Builds the next input for the agent.
     *
     * @param agentInput  the agent input
     * @param stepContext the agent step context
     * @return the next input
     */
    protected I buildNextInput(I agentInput, C stepContext) {
        return agentInput;
    }

    /**
     * Performs a single step of the streaming agent.
     *
     * @param agentInput  The input for the agent.
     * @param stepContext The step context for the agent.
     */
    public Flux<S> stepStream(I agentInput, C stepContext) {
        MetaAgent<I, O> agent = this;
        List<AgentStepListener<I, O, C>> stepListeners = getStepListenerRegistry().getStepListeners();

        Flux<S> stream = doStepStream(agentInput, stepContext);
        return stream
                .doOnSubscribe(sub -> notifyListeners(stepListeners, listener ->
                        listener.onAgentStepStart(agent, agentInput, stepContext)))
                .doOnComplete(() -> notifyListeners(stepListeners, listener -> {
                    O agentOutput = buildAgentOutput(agentInput, stepContext, stream);
                    listener.onAgentStepFinish(agent, agentInput, agentOutput, stepContext);
                }))
                .doOnError(throwable -> {
                    Exception exception;
                    if (throwable instanceof Exception ex) {
                        exception = ex;
                    } else {
                        exception = new AgentExecutionException("agent execution failed", throwable);
                    }
                    notifyListeners(stepListeners, listener ->
                            listener.onAgentStepError(agent, agentInput, exception, stepContext)
                    );
                });
    }

    /**
     * Performs a step of the agent with streaming.
     *
     * @param agentInput  The input for the agent.
     * @param stepContext The step context for the agent.
     * @return The stream output of the agent.
     */
    protected abstract Flux<S> doStepStream(I agentInput, C stepContext);

}
