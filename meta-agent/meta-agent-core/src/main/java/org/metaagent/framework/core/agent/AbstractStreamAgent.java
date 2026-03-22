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
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.listener.AgentRunListener;
import org.metaagent.framework.core.agent.listener.AgentStepListener;
import org.metaagent.framework.core.agent.output.AgentStreamOutput;
import org.metaagent.framework.core.agent.profile.AgentProfile;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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
        extends AbstractAgent<I, O, C> implements StreamingAgent<I, O> {
    protected AbstractStreamAgent(String name) {
        super(name);
    }

    protected AbstractStreamAgent(AgentProfile profile) {
        super(profile);
    }

    protected AbstractStreamAgent(AbstractAgentBuilder<?, I, O, C> builder) {
        super(builder);
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
    public O runStream(I input) {
        AbstractStreamAgent<I, O, C, S> agent = this;

        I agentInput = preprocess(input);
        C stepContext = createStepContext(agentInput);
        List<AgentRunListener<I, O>> runListeners = getRunListenerRegistry().getRunListeners();

        O agentOutput = doRunStream(agentInput, stepContext);
        Flux<S> stream = agentOutput.stream()
                .doOnSubscribe(sub ->
                        notifyListeners(runListeners, listener -> listener.onAgentStart(agent, agentInput))
                )
                .onErrorResume(throwable -> {
                    if (throwable instanceof Exception ex) {
                        return fallbackStream(agentInput, ex);
                    }
                    return Flux.error(throwable);
                })
                .doOnComplete(() -> notifyListeners(runListeners, listener -> listener.onAgentOutput(agent, agentInput, agentOutput)))
                .doOnError(throwable -> {
                    if (throwable instanceof Exception ex) {
                        notifyListeners(runListeners, listener -> listener.onAgentException(agent, agentInput, ex));
                    } else {
                        AgentExecutionException ex = new AgentExecutionException("agent execution failed", throwable);
                        notifyListeners(runListeners, listener -> listener.onAgentException(agent, agentInput, ex));
                    }
                });
        stream = handleExceptionIfRequired(stream);
        return rebuildOutput(agentInput, agentOutput, stream);
    }

    /**
     * Run the streaming agent with looping.
     *
     * @param agentInput  The input to run the agent with
     * @param stepContext The step context
     * @return the output of the agent
     */
    protected O doRunStream(I agentInput, C stepContext) {
        AtomicReference<I> currentInputRef = new AtomicReference<>(agentInput);
        AtomicReference<O> lastOutputRef = new AtomicReference<>();

        AtomicBoolean isFirstStep = new AtomicBoolean(true);
        O firstAgentOutput = stepStream(currentInputRef.get(), stepContext);
        lastOutputRef.set(firstAgentOutput);

        Flux<S> stream = Flux
                .defer(() -> {
                    if (isFirstStep.get()) {
                        isFirstStep.set(false);
                        return firstAgentOutput.stream();
                    } else {
                        O agentOutput = stepStream(currentInputRef.get(), stepContext);
                        lastOutputRef.set(agentOutput);
                        return agentOutput.stream();
                    }
                })
                .repeat(() -> {
                    O lastAgentOutput = lastOutputRef.get();
                    I currentInput = currentInputRef.get();

                    if (shouldContinueLoop(currentInput, lastAgentOutput, stepContext)) {
                        // perform next step
                        I nextInput = buildNextStepInput(currentInput, lastAgentOutput);
                        currentInputRef.set(nextInput);
                        return true;
                    } else {
                        // stop loop
                        return false;
                    }
                });
        return rebuildOutput(agentInput, firstAgentOutput, stream);
    }

    /**
     * Fallback while stream execution failed.
     *
     * @param input The input for the agent.
     * @param ex    The exception that caused the fallback.
     * @return The fallback stream.
     */
    protected Flux<S> fallbackStream(I input, Exception ex) {
        return Flux.error(ex);
    }

    /**
     * Builds the next step input for the streaming agent.
     *
     * @param agentInput  The current input for the agent.
     * @param agentOutput The output from the current step.
     * @return The next step input for the agent.
     */
    protected I buildNextStepInput(I agentInput, O agentOutput) {
        return agentInput;
    }

    /**
     * Performs a single step of the streaming agent.
     *
     * @param agentInput  The input for the agent.
     * @param stepContext The step context for the agent.
     */
    protected O stepStream(I agentInput, C stepContext) {
        Agent<I, O, C> agent = this;
        List<AgentStepListener<I, O, C>> stepListeners = getStepListenerRegistry().getStepListeners();

        O agentOutput = doStepStream(agentInput, stepContext);
        Flux<S> stream = agentOutput.stream()
                .doOnSubscribe(sub -> notifyListeners(stepListeners, listener ->
                        listener.onAgentStepStart(agent, agentInput, stepContext)))
                .doOnComplete(() -> notifyListeners(stepListeners, listener ->
                        listener.onAgentStepFinish(agent, agentInput, agentOutput, stepContext)))
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
        return rebuildOutput(agentInput, agentOutput, stream);
    }

    /**
     * Performs a step of the agent with streaming.
     *
     * @param agentInput  The input for the agent.
     * @param stepContext The step context for the agent.
     * @return The stream output of the agent.
     */
    protected abstract O doStepStream(I agentInput, C stepContext);

    /**
     * Rebuilds the output of the agent with the given input, agent output, and stream.
     *
     * @param agentInput  The input for the agent.
     * @param agentOutput The output from the agent.
     * @param stream      The stream of outputs from the agent.
     * @return The rebuilt output of the agent.
     */
    protected abstract O rebuildOutput(I agentInput, O agentOutput, Flux<S> stream);

}
