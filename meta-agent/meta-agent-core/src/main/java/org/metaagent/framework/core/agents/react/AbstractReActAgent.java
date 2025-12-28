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

package org.metaagent.framework.core.agents.react;

import org.metaagent.framework.core.agent.AbstractAgent;
import org.metaagent.framework.core.agent.AgentExecutionContext;
import org.metaagent.framework.core.agent.action.Action;
import org.metaagent.framework.core.agent.action.DefaultActionExecutionContext;
import org.metaagent.framework.core.agent.action.actions.AgentFinishAction;
import org.metaagent.framework.core.agent.action.result.ActionResult;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.metaagent.framework.core.agent.output.observation.Observation;
import org.metaagent.framework.core.agent.output.thought.Thought;

/**
 * description is here
 *
 * @author vyckey
 */
public abstract class AbstractReActAgent<I, O>
        extends AbstractAgent<I, O> implements ReActAgent<I, O> {

    protected AbstractReActAgent(String name) {
        super(name);
    }

    @Override
    public AgentOutput<O> doStep(AgentInput<I> input) {
        int turnNum = agentState.getLoopCount() + 1;

        Thought thought = think(input);
        agentLogger.info("Thought #{}: {}", turnNum, thought.getText());

        Action action = thought.getProposalAction();
        if (action instanceof AgentFinishAction) {
            return generateOutput(input, thought, null);
        }

        agentLogger.info("Action #{}: {}", turnNum, action);
        ActionResult actionResult = act(input, action);

        Observation observation = observe(input, thought, actionResult);
        agentLogger.info("Observation #{}: {}", turnNum, observation.getText());
        return generateOutput(input, thought, observation);
    }

    @Override
    public ActionResult act(AgentInput<I> input, Action action) {
        AgentExecutionContext context = input.context();
        DefaultActionExecutionContext executionContext = DefaultActionExecutionContext.builder()
                .environment(context.getEnvironment())
                .toolManager(getToolManager())
                .actionExecutor(context.getActionExecutor())
                .actionHistory(agentState.getActionHistory())
                .build();
        return context.getActionExecutor().execute(executionContext, action);
    }
}
