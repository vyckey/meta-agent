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

package org.metaagent.framework.agents.chat;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.core.agent.AbstractAgent;
import org.metaagent.framework.core.agent.AgentExecutionContext;
import org.metaagent.framework.core.agent.AgentExecutionException;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.history.DefaultMessageHistory;
import org.metaagent.framework.core.agent.chat.message.history.MessageHistory;
import org.metaagent.framework.core.agent.input.message.AgentMessageInput;
import org.metaagent.framework.core.agent.output.message.AgentMessageOutput;
import org.metaagent.framework.core.agents.chat.ChatAgent;
import org.metaagent.framework.core.model.chat.MessageConverter;
import org.metaagent.framework.core.tool.DefaultToolContext;
import org.metaagent.framework.core.tool.spring.ToolCallbackUtils;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;
import java.util.Objects;

/**
 * LLM chat agent.
 *
 * @author vyckey
 */
public class LlmChatAgent extends AbstractAgent<AgentMessageInput, AgentMessageOutput> implements ChatAgent {
    protected final ChatModel chatModel;
    protected final ChatOptions chatOptions;
    protected MessageHistory messageHistory = new DefaultMessageHistory();
    protected MessageConverter messageConverter = MessageConverter.INSTANCE;
    protected int maxLoopCount = 3;

    public LlmChatAgent(String name, ChatModel chatModel, ChatOptions chatOptions) {
        super(name);
        this.chatModel = Objects.requireNonNull(chatModel, "chatModel is required");
        this.chatOptions = Objects.requireNonNull(chatOptions, "chatOptions is required");
    }

    @Override
    public MessageHistory getMessageHistory() {
        return messageHistory;
    }

    @Override
    protected AgentMessageOutput doStep(AgentMessageInput input) {
        AgentExecutionContext context = input.getContext();
        Prompt prompt = buildPrompt(context, input);
        for (int i = 0; i < maxLoopCount; i++) {
            ChatResponse chatResponse = chatModel.call(prompt);
            Generation result = chatResponse.getResult();
            AssistantMessage assistantMessage = result.getOutput();
            if (assistantMessage.hasToolCalls()) {
                List<AssistantMessage.ToolCall> toolCalls = assistantMessage.getToolCalls();
                DefaultToolContext toolContext = DefaultToolContext.builder()
                        .toolManager(context.getToolManager())
                        .toolCallTracker(getAgentState().getToolCallTracker())
                        .build();
                List<ToolResponseMessage.ToolResponse> toolResponses =
                        ToolCallbackUtils.callTools(context.getToolManager(), toolContext, toolCalls);
                ToolResponseMessage toolResponseMessage = new ToolResponseMessage(toolResponses);
                prompt.getInstructions().add(toolResponseMessage);
            } else {
                Message outputMessage = messageConverter.reverse(assistantMessage);
                AgentMessageOutput agentOutput = AgentMessageOutput.from(outputMessage);
                agentOutput.getMessages().forEach(messageHistory::appendMessage);
                return agentOutput;
            }
        }
        throw new AgentExecutionException("Exceed the agent max loop count " + maxLoopCount);
    }

    protected Prompt buildPrompt(AgentExecutionContext context, AgentMessageInput messageInput) {
        List<Message> inputMessages = messageInput.getMessages();
        List<org.springframework.ai.chat.messages.Message> messages = Lists.newArrayList();
        if (StringUtils.isNotEmpty(messageInput.getTopic())) {
            messages.add(new SystemMessage(messageInput.getTopic()));
        }
        inputMessages.stream().map(messageConverter::convert).forEach(messages::add);

        ChatOptions options = ToolCallbackUtils.buildChatOptionsWithTools(this.chatOptions,
                buildToolContext(messageInput), context.getToolExecutor());
        return new Prompt(messages, options);
    }

    @Override
    public void reset() {
        super.reset();
        getMessageHistory().clear();
    }
}
