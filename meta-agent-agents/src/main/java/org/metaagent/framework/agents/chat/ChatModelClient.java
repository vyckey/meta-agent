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
import io.micrometer.observation.ObservationRegistry;
import org.metaagent.framework.core.agent.chat.message.AssistantMessage;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.SystemMessage;
import org.metaagent.framework.core.model.chat.MessageConverter;
import org.metaagent.framework.core.tool.executor.ToolExecutorContext;
import org.metaagent.framework.core.tool.spring.ToolCallbackUtils;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.execution.DefaultToolExecutionExceptionProcessor;
import org.springframework.ai.tool.resolution.DelegatingToolCallbackResolver;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

public class ChatModelClient {
    protected ChatModel chatModel;
    protected SystemMessage systemMessage;
    protected List<Message> historyMessages = Lists.newArrayList();
    protected MessageConverter messageConverter = new MessageConverter(true);
    protected ToolExecutorContext toolExecutorContext;
    protected int lastMessageCursor = 0;
    protected ChatResponse chatResponse;

    public ChatModelClient(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public void setSystemMessage(SystemMessage systemMessage) {
        this.systemMessage = Objects.requireNonNull(systemMessage, "systemMessage is required");
    }

    public List<Message> getHistoryMessages() {
        return historyMessages;
    }

    public void setHistoryMessages(List<Message> historyMessages) {
        Objects.requireNonNull(historyMessages, "historyMessages is required");
        this.historyMessages = historyMessages;
    }

    public void addHistoryMessage(Message message) {
        this.historyMessages.add(message);
    }

    public void clearHistoryMessages() {
        this.historyMessages.clear();
    }

    public List<Message> getNewMessages() {
        return historyMessages.subList(lastMessageCursor, historyMessages.size());
    }

    public void setToolExecutorContext(ToolExecutorContext toolExecutorContext) {
        this.toolExecutorContext = toolExecutorContext;
    }

    public AssistantMessage sendMessage(Message message) {
        this.lastMessageCursor = this.historyMessages.size();
        this.historyMessages.add(message);

        Prompt prompt = buildPrompt(List.of(message), chatModel.getDefaultOptions());
        ChatResponse chatResponse = call(prompt, null);
        this.chatResponse = chatResponse;
        Generation generation = chatResponse.getResult();
        AssistantMessage outputMessage = (AssistantMessage) messageConverter.reverse(generation.getOutput());
        this.historyMessages.add(outputMessage);
        return outputMessage;
    }

    protected Prompt buildPrompt(List<Message> messages, ChatOptions chatOptions) {
        List<org.springframework.ai.chat.messages.Message> messageList = Lists.newArrayList();
        if (systemMessage != null) {
            messageList.add(messageConverter.convert(systemMessage));
        }
        for (Message historyMessage : historyMessages) {
            messageList.add(messageConverter.convert(historyMessage));
        }
        for (Message message : messages) {
            messageList.add(messageConverter.convert(message));
        }

        if (toolExecutorContext != null) {
            chatOptions = ToolCallbackUtils.buildChatOptionsWithTools(chatOptions,
                    toolExecutorContext.getToolManager(), toolExecutorContext.getToolContext(), false);
        }
        return new Prompt(messageList, chatOptions);
    }

    protected ChatResponse call(Prompt prompt, ChatResponse previousResponse) {
        ChatResponse response = chatModel.call(prompt);
        if (response.hasToolCalls()) {
            ToolCallingManager toolCallingManager = getToolCallingManager();
            ToolExecutionResult executionResult = toolCallingManager.executeToolCalls(prompt, response);
            if (executionResult.returnDirect()) {
                return ChatResponse.builder()
                        .from(response)
                        .generations(ToolExecutionResult.buildGenerations(executionResult))
                        .build();
            }

            List<org.springframework.ai.chat.messages.Message> conversationHistory
                    = executionResult.conversationHistory();
            conversationHistory.subList(prompt.getInstructions().size(), conversationHistory.size())
                    .forEach(message -> historyMessages.add(messageConverter.reverse(message)));
            Prompt newPrompt = new Prompt(conversationHistory, prompt.getOptions());
            return call(newPrompt, response);
        }
        return response;
    }

    protected ToolCallingManager getToolCallingManager() {
        Field managerField = ReflectionUtils.findField(chatModel.getClass(), "toolCallingManager", ToolCallingManager.class);
        if (managerField != null) {
            managerField.setAccessible(true);
            try {
                return (ToolCallingManager) managerField.get(chatModel);
            } catch (IllegalAccessException e) {
                // ignore and use default
            }
        }
        return new DefaultToolCallingManager(
                ObservationRegistry.NOOP,
                new DelegatingToolCallbackResolver(List.of()),
                new DefaultToolExecutionExceptionProcessor(true)
        );
    }

    public void reset() {
        this.systemMessage = null;
        this.clearHistoryMessages();
        this.toolExecutorContext = null;
        this.lastMessageCursor = 0;
        this.chatResponse = null;
    }
}
