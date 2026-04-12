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

import org.apache.commons.lang3.NotImplementedException;
import org.metaagent.framework.core.agent.context.AgentStepContext;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.output.AgentStreamOutput;
import reactor.core.publisher.Flux;

/**
 * The streaming agent abstraction.
 *
 * @param <I> the type of agent input
 * @param <O> the type of agent output
 * @param <C> the type of agent step context
 * @param <S> <S> the type of agent stream output
 * @author vyckey
 */
public interface StreamingAgent<
        I extends AgentInput,
        O extends AgentStreamOutput<S>,
        C extends AgentStepContext,
        S> extends MetaAgent<I, O> {

    /**
     * Run the agent in streaming mode.
     *
     * @param agentInput the agent input.
     * @return the final agent output.
     */
    @Override
    default O run(I agentInput) {
        C stepContext = createStepContext(agentInput);
        Flux<S> stream = runStream(agentInput, stepContext);
        return buildAgentOutput(agentInput, stepContext, stream);
    }

    /**
     * Builds the agent output.
     *
     * @param agentInput  the agent input
     * @param stepContext the agent step context
     * @param stream      the agent stream output
     * @return the agent output
     */
    private O buildAgentOutput(I agentInput, C stepContext, Flux<S> stream) {
        throw new NotImplementedException("Not implemented");
    }

    /**
     * Determines whether the agent should continue looping.
     *
     * @param agentInput  the agent input
     * @param stepContext the agent step context
     * @return true if the agent should continue looping, false otherwise
     */
    private boolean shouldContinueLoop(I agentInput, C stepContext) {
        throw new NotImplementedException("Not implemented");
    }

    /**
     * The agent will execute a loop step until the agent should exit.
     *
     * @param agentInput  the agent input.
     * @param stepContext the agent step context.
     * @return the agent stream output.
     */
    default Flux<S> runStream(I agentInput, C stepContext) {
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
     * Creates a new step context for the agent execution.
     *
     * @param agentInput the agent input
     * @return a new step context instance
     */
    C createStepContext(I agentInput);

    /**
     * Builds the next input for the agent.
     *
     * @param agentInput  the agent input
     * @param stepContext the agent step context
     * @return the next input
     */
    private I buildNextInput(I agentInput, C stepContext) {
        return agentInput;
    }

    /**
     * Performs an agent step execution.
     *
     * @param agentInput  the agent input.
     * @param stepContext the agent step context.
     * @return the agent stream output.
     */
    Flux<S> stepStream(I agentInput, C stepContext);

}
