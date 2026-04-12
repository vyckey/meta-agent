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

import org.metaagent.framework.core.agent.context.AgentStepContext;
import org.metaagent.framework.core.agent.fallback.AgentFallbackStrategy;
import org.metaagent.framework.core.agent.fallback.FastFailAgentFallbackStrategy;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.listener.AgentStepListener;
import org.metaagent.framework.core.agent.listener.AgentStepListenerRegistry;
import org.metaagent.framework.core.agent.listener.DefaultAgentListenerRegistry;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.metaagent.framework.core.agent.profile.AgentProfile;

import java.util.List;

/**
 * Abstract {@link Agent} implementation.
 *
 * @param <I> the type of agent input
 * @param <O> the type of agent output
 * @author vyckey
 */
public abstract class AbstractAgent<I extends AgentInput, O extends AgentOutput, C extends AgentStepContext>
        extends AbstractMetaAgent<I, O> implements Agent<I, O, C> {
    protected AgentStepListenerRegistry<I, O, C> stepListenerRegistry;

    protected AbstractAgent(String name) {
        super(name);
        this.stepListenerRegistry = new DefaultAgentListenerRegistry<>();
    }

    protected AbstractAgent(AgentProfile profile) {
        this(profile.getName());
        this.profile = profile;
    }

    protected AbstractAgent(AbstractAgentBuilder<?, I, O, C> builder) {
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

    /**
     * Determines whether the agent should continue looping.
     *
     * @param agentInput  the agent input
     * @param agentOutput the agent output which can be null
     * @param stepContext the agent step context
     * @return true if the agent should continue looping, false otherwise
     */
    protected abstract boolean shouldContinueLoop(I agentInput, O agentOutput, C stepContext);

    @Override
    protected O doRun(I agentInput) {
        return handleExceptionIfRequired(() -> {
            C stepContext = createStepContext(agentInput);

            O agentOutput;
            do {
                try {
                    agentOutput = step(agentInput, stepContext);
                } catch (Exception ex) {
                    agentOutput = getFallbackStrategy().fallback(this, agentInput, ex);
                } finally {
                    stepContext.getLoopCounter().incrementAndGet();
                }
            } while (shouldContinueLoop(agentInput, agentOutput, stepContext));
            return agentOutput;
        });
    }

    /**
     * Creates a new step context for the agent execution.
     *
     * @param agentInput the agent input
     * @return a new step context instance
     */
    public abstract C createStepContext(I agentInput);

    @Override
    public O step(I agentInput, C stepContext) {
        List<AgentStepListener<I, O, C>> stepListeners = getStepListenerRegistry().getStepListeners();
        try {
            notifyListeners(stepListeners, listener ->
                    listener.onAgentStepStart(this, agentInput, stepContext)
            );
            O agentOutput = doStep(agentInput, stepContext);
            notifyListeners(stepListeners, listener ->
                    listener.onAgentStepFinish(this, agentInput, agentOutput, stepContext)
            );
            return agentOutput;
        } catch (Exception ex) {
            notifyListeners(stepListeners, listener ->
                    listener.onAgentStepError(this, agentInput, ex, stepContext)
            );
            throw ex;
        }
    }

    /**
     * Performs agent step.
     *
     * @param agentInput  the agent input
     * @param stepContext the agent step context
     * @return the agent output
     */
    protected abstract O doStep(I agentInput, C stepContext);

}
