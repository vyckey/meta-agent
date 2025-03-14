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

import org.metaagent.framework.core.agent.Agent;
import org.metaagent.framework.core.agent.AgentExecutionContext;
import org.metaagent.framework.core.agent.action.Action;
import org.metaagent.framework.core.agent.action.actions.AgentFinishAction;
import org.metaagent.framework.core.agent.action.result.ActionResult;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.metaagent.framework.core.agent.output.observation.Observation;
import org.metaagent.framework.core.agent.output.thought.Thought;

/**
 * Reasoning and acting agent.
 * <p>
 * Arxiv Paper: <a href="https://arxiv.org/abs/2210.03629">ReAct: Synergizing Reasoning and Acting in Language Models</a>
 *
 * @author vyckey
 */
public interface ReActAgent extends Agent {

    default AgentOutput step(AgentExecutionContext context, AgentInput input) {
        Thought thought = think(context, input);
        Action action = thought.getProposalAction();
        if (action instanceof AgentFinishAction) {
            return thought;
        }
        ActionResult actionResult = act(context, action);
        return observe(context, input, thought, actionResult);
    }

    Thought think(AgentExecutionContext context, AgentInput input);

    ActionResult act(AgentExecutionContext context, Action action);

    Observation observe(AgentExecutionContext context, AgentInput input, Thought thought, ActionResult actionResult);
}
