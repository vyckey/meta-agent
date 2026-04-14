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

package org.metaagent.framework.core.agents.llm;

import org.metaagent.framework.core.agent.AbstractAgent;
import org.metaagent.framework.core.agent.chat.message.part.MessagePart;
import org.metaagent.framework.core.agents.llm.context.LlmAgentContext;
import org.metaagent.framework.core.agents.llm.context.LlmAgentStepContext;
import org.metaagent.framework.core.agents.llm.input.LlmAgentInput;
import org.metaagent.framework.core.agents.llm.message.ToolCallMessagePart;
import org.metaagent.framework.core.agents.llm.output.LlmAgentOutput;
import org.metaagent.framework.core.model.chat.ChatModelInstance;
import org.metaagent.framework.core.model.provider.ModelProviderUtils;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

/**
 * LlmAgent is an agent that uses a Large Language Model (LLM) to generate responses to user input.
 *
 * @author vyckey
 * @see LlmAgentInput
 * @see LlmAgentOutput
 */
public class LlmAgent extends AbstractAgent<LlmAgentInput, LlmAgentOutput, LlmAgentStepContext> {
    private final LlmStreamingAgent streamingAgent;

    public LlmAgent(String name) {
        super(name);
        streamingAgent = new LlmStreamingAgent(name);
    }

    @Override
    public LlmAgentStepContext createStepContext(LlmAgentInput agentInput) {
        return streamingAgent.createStepContext(agentInput);
    }

    /**
     * Determines whether the agent should continue looping.
     * <p>
     * Continue if:
     * 1. There are tool results in the step context (meaning tool calls were executed)
     * 2. Max iterations not reached
     * 3. Token limit not exceeded
     */
    @Override
    protected boolean shouldContinueLoop(LlmAgentInput input, LlmAgentOutput output, LlmAgentStepContext stepContext) {
        // Check max iterations
        int currentIteration = stepContext.getLoopCounter().get();
        if (input.maxSteps() != null && input.maxSteps() > 0 && currentIteration >= input.maxSteps()) {
            return false;
        }

        return !stepContext.isFinished();
    }

    @Override
    protected LlmAgentOutput doStep(LlmAgentInput agentInput, LlmAgentStepContext stepContext) {
        LlmAgentContext agentContext = agentInput.context();
        ChatModelInstance modelInstance = ModelProviderUtils.getChatModel(agentContext.modelProviderRegistry(), agentInput.modelId());

        Prompt prompt = streamingAgent.buildPrompt(agentInput, stepContext);

        ChatResponse chatResponse = modelInstance.getRuntime().call(prompt);
        List<MessagePart> outputMessageParts = streamingAgent.parseOutputMessageParts(chatResponse, stepContext, agentInput.messagePartIdGenerator());
        if (chatResponse.hasToolCalls()) {
            List<ToolCallMessagePart> toolCallMessageParts = streamingAgent.executeToolCalls(
                    chatResponse, agentInput.messagePartIdGenerator(), agentContext, stepContext
            );
            outputMessageParts.addAll(toolCallMessageParts);
        }

        stepContext.addTokenUsage(chatResponse.getMetadata().getUsage());
        stepContext.addOutputMessageParts(outputMessageParts);

        return LlmAgentOutput.builder()
                .message(stepContext.getOutputMessage())
                .finishReason(stepContext.getFinishReason())
                .tokenUsage(stepContext.getTokenUsage())
                .build();
    }

    @Override
    public void close() {
    }

}
