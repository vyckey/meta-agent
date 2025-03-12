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
public abstract class AbstractReActAgent extends AbstractAgent implements ReActAgent {

    protected AbstractReActAgent(String name) {
        super(name);
    }

    @Override
    public AgentOutput doExecute(AgentExecutionContext context, AgentInput input) {
        int turnNum = context.getAgentState().getLoopCount() + 1;

        Thought thought = think(context, input);
        agentLogger.info("Thought #{}: {}", turnNum, thought.getText());

        Action action = thought.getProposalAction();
        if (action instanceof AgentFinishAction) {
            return thought;
        }

        agentLogger.info("Action #{}: {}", turnNum, action);
        ActionResult actionResult = act(context, action);

        Observation observation = observe(context, input, thought, actionResult);
        agentLogger.info("Observation #{}: {}", turnNum, observation.getText());
        return observation;
    }

    @Override
    public ActionResult act(AgentExecutionContext context, Action action) {
        DefaultActionExecutionContext executionContext = DefaultActionExecutionContext.builder()
                .environment(context.getEnvironment())
                .toolManager(context.getToolManager())
                .actionExecutor(context.getActionExecutor())
                .actionHistory(context.getAgentState().getActionHistory())
                .build();
        return context.getActionExecutor().execute(executionContext, action);
    }
}
