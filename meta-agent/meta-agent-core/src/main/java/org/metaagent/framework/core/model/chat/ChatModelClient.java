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

package org.metaagent.framework.core.model.chat;

import com.google.common.collect.Lists;
import io.micrometer.observation.ObservationRegistry;
import org.metaagent.framework.core.model.chat.metadata.TokenUsage;
import org.metaagent.framework.core.tool.executor.ToolExecutor;
import org.metaagent.framework.core.tool.executor.ToolExecutorContext;
import org.metaagent.framework.core.tool.tools.spring.ToolCallbackUtils;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ChatModelClient {
    protected ChatModel chatModel;
    protected List<SystemMessage> systemMessages = new ArrayList<>(1);
    protected List<Message> historyMessages = new ArrayList<>();
    protected int lastTurnStartMessageCursor = 0;
    protected int lastTurnOutputMessageCursor = 0;
    protected ToolExecutor toolExecutor;
    protected ToolExecutorContext toolExecutorContext;
    protected TokenUsage totalUsage = TokenUsage.empty();

    public ChatModelClient(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public void setSystemMessages(List<SystemMessage> systemMessages) {
        Objects.requireNonNull(systemMessages, "systemMessages is required");
        this.systemMessages.clear();
        this.systemMessages.addAll(systemMessages);
    }

    public void setSystemMessage(SystemMessage... systemMessages) {
        setSystemMessages(List.of(systemMessages));
    }

    public void addSystemMessage(SystemMessage systemMessage) {
        this.systemMessages.add(systemMessage);
    }

    public void setHistoryMessages(List<Message> historyMessages) {
        Objects.requireNonNull(historyMessages, "historyMessages is required");
        this.historyMessages.clear();
        this.historyMessages.addAll(historyMessages);
    }

    public List<Message> lastTurnMessages() {
        return historyMessages.subList(lastTurnStartMessageCursor, historyMessages.size());
    }

    public List<Message> lastTurnOutputMessages() {
        return historyMessages.subList(lastTurnOutputMessageCursor, historyMessages.size());
    }

    public void setToolContext(ToolExecutorContext toolExecutorContext, ToolExecutor toolExecutor) {
        this.toolExecutorContext = toolExecutorContext;
        this.toolExecutor = toolExecutor;
    }

    public ChatModelResponse sendMessage(Message... messages) {
        return sendMessages(Arrays.asList(messages));
    }

    public ChatModelResponse sendMessages(List<Message> messages) {
        lastTurnStartMessageCursor = historyMessages.size();
        historyMessages.addAll(messages);
        lastTurnOutputMessageCursor = historyMessages.size();

        Prompt prompt = buildPrompt(chatModel.getDefaultOptions());
        ChatModelResponse response = call(prompt, null);
        this.totalUsage = this.totalUsage.accumulate(response.getMetadata().getUsage());
        return response;
    }

    public Flux<ChatModelResponse> sendMessages(Message... messages) {
        return sendMessageStream(Arrays.asList(messages));
    }

    public Flux<ChatModelResponse> sendMessageStream(List<Message> messages) {
        lastTurnStartMessageCursor = historyMessages.size();
        historyMessages.addAll(messages);
        lastTurnOutputMessageCursor = historyMessages.size();

        Prompt prompt = buildPrompt(chatModel.getDefaultOptions());
        return stream(prompt, null);
    }

    protected Prompt buildPrompt(ChatOptions chatOptions) {
        List<Message> messageList = Lists.newArrayList();
        messageList.addAll(systemMessages);
        messageList.addAll(historyMessages);

        if (toolExecutorContext != null) {
            chatOptions = ToolCallbackUtils.buildChatOptionsWithTools(
                    chatOptions, toolExecutor, toolExecutorContext, false
            );
        }
        return new Prompt(messageList, chatOptions);
    }

    protected ChatModelResponse call(Prompt prompt, ChatResponse previousResponse) {
        ChatModelResponse.Builder responseBuilder = ChatModelResponse.newBuilder().merge(previousResponse);
        ChatResponse response = chatModel.call(prompt);
        responseBuilder.merge(response);

        if (!response.hasToolCalls()) {
            Generation generation = response.getResult();
            historyMessages.add(generation.getOutput());
            return responseBuilder.build();
        }

        ToolCallingManager toolCallingManager = getToolCallingManager();
        ToolExecutionResult executionResult = toolCallingManager.executeToolCalls(prompt, response);
        if (executionResult.returnDirect()) {
            List<Generation> generations = ToolExecutionResult.buildGenerations(executionResult);
            for (Generation generation : generations) {
                historyMessages.add(generation.getOutput());
            }

            return responseBuilder.generations(generations).build();
        } else {
            List<org.springframework.ai.chat.messages.Message> conversationHistory
                    = executionResult.conversationHistory();
            List<Message> newMessages = conversationHistory.subList(prompt.getInstructions().size(), conversationHistory.size());
            historyMessages.addAll(newMessages);

            Prompt newPrompt = new Prompt(conversationHistory, prompt.getOptions());
            return call(newPrompt, responseBuilder.build());
        }
    }

    protected Flux<ChatModelResponse> stream(Prompt prompt, ChatResponse previousResponse) {
        return Flux.deferContextual(contextView -> {
            Flux<ChatModelResponse> chatResponseFlux = chatModel.stream(prompt)
                    .map(ChatResponseUtils::rebuildResponseIfRequired)
                    .switchMap(response -> {
                        ChatModelResponse.Builder responseBuilder = ChatModelResponse.newBuilder().merge(previousResponse);
                        responseBuilder.merge(response);
                        return Mono.just(responseBuilder.build());
                    });

            Flux<ChatModelResponse> flux = chatResponseFlux.flatMap(chatResponse -> {
                if (!chatResponse.hasToolCalls()) {
                    Generation generation = chatResponse.getResult();
                    historyMessages.add(generation.getOutput());
                    return Flux.just(chatResponse);
                }

                ChatModelResponse.Builder responseBuilder = ChatModelResponse.newBuilder().merge(chatResponse);
                ToolCallingManager toolCallingManager = getToolCallingManager();
                ToolExecutionResult executionResult = toolCallingManager.executeToolCalls(prompt, chatResponse);
                if (executionResult.returnDirect()) {
                    List<Generation> generations = ToolExecutionResult.buildGenerations(executionResult);
                    for (Generation generation : generations) {
                        historyMessages.add(generation.getOutput());
                    }

                    return Flux.just(responseBuilder.generations(generations).build());
                } else {
                    List<org.springframework.ai.chat.messages.Message> conversationHistory
                            = executionResult.conversationHistory();
                    List<Message> newMessages = conversationHistory.subList(prompt.getInstructions().size(), conversationHistory.size());
                    historyMessages.addAll(newMessages);

                    Prompt newPrompt = new Prompt(conversationHistory, prompt.getOptions());
                    return stream(newPrompt, responseBuilder.build());
                }
            });
            return flux;
        });
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
        this.systemMessages.clear();
        this.historyMessages.clear();
        this.lastTurnStartMessageCursor = 0;
        this.lastTurnOutputMessageCursor = 0;
        this.toolExecutorContext = null;
        this.totalUsage = TokenUsage.empty();
    }
}
