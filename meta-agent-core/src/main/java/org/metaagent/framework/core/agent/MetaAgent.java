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

import org.metaagent.framework.core.agent.ability.AgentAbility;
import org.metaagent.framework.core.agent.ability.AgentAbilityManager;
import org.metaagent.framework.core.agent.fallback.AgentFallbackStrategy;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.memory.Memory;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.metaagent.framework.core.agent.profile.AgentProfile;
import org.metaagent.framework.core.agent.state.AgentState;
import reactor.core.publisher.Flux;

import java.util.concurrent.CompletableFuture;

/**
 * Meta Agent, the basic class of all agents.
 *
 * @author vyckey
 */
public interface MetaAgent {
    /**
     * Gets agent name.
     *
     * @return the agent name.
     */
    default String getName() {
        return getAgentProfile().getName();
    }

    /**
     * Gets agent profile.
     *
     * @return the agent profile.
     */
    AgentProfile getAgentProfile();

    /**
     * Gets agent state.
     *
     * @return the agent state.
     */
    AgentState getAgentState();

    /**
     * Gets agent memory.
     *
     * @return the agent memory.
     */
    Memory getMemory();

    /**
     * Gets agent abilities manager.
     *
     * @return the ability manager.
     */
    AgentAbilityManager getAbilityManager();

    /**
     * Gets specialized agent ability by class type.
     *
     * @param abilityType the ability class type.
     * @param <T>         the ability type.
     * @return the agent ability.
     */
    default <T extends AgentAbility> T getAbility(Class<T> abilityType) {
        return getAbilityManager().getAbility(abilityType);
    }

    /**
     * Runs agent logic.
     *
     * @param context the agent execution context.
     * @param input   the agent input.
     * @return the agent output.
     */
    default AgentOutput run(AgentExecutionContext context, AgentInput input) {
        try {
            return step(context, input);
        } catch (Exception ex) {
            return getFallbackStrategy().fallback(this, context, input, ex);
        }
    }

    /**
     * Runs agent logic in a streaming way.
     *
     * @param context the agent execution context.
     * @param input   the agent input.
     * @return the streaming agent output.
     */
    default Flux<AgentOutput> runFlux(AgentExecutionContext context, AgentInput input) {
        throw new UnsupportedOperationException("Streaming run is not supported");
    }

    /**
     * Runs agent synchronously.
     *
     * @param context the agent execution context.
     * @param input   the agent input
     * @return the agent out.
     */
    default CompletableFuture<AgentOutput> runAsync(AgentExecutionContext context, AgentInput input) {
        return CompletableFuture.supplyAsync(() -> run(context, input));
    }

    /**
     * Start an agent step.
     *
     * @param context the agent execution context.
     * @param input   the agent input.
     * @return the agent output.
     */
    AgentOutput step(AgentExecutionContext context, AgentInput input);

    /**
     * Gets agent fallback strategy. It will be used to handle unexpected exceptions while running the agent.
     *
     * @return the fallback strategy.
     */
    AgentFallbackStrategy getFallbackStrategy();

    /**
     * Reset the agent to initial state.
     */
    void reset();
}
