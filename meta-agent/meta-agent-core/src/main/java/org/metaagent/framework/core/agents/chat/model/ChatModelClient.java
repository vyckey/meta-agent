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

package org.metaagent.framework.core.agents.chat.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.micrometer.observation.ObservationRegistry;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.part.MessagePart;
import org.metaagent.framework.core.agent.chat.message.part.ToolCallResultMessagePart;
import org.metaagent.framework.core.agents.chat.model.metadata.ChatResponseMetadata;
import org.metaagent.framework.core.agents.chat.model.metadata.DefaultChatResponseMetadata;
import org.metaagent.framework.core.agents.chat.model.metadata.TokenUsage;
import org.metaagent.framework.core.model.ModelId;
import org.metaagent.framework.core.model.chat.ChatModelInstance;
import org.metaagent.framework.core.model.chat.ChatResponseUtils;
import org.metaagent.framework.core.model.chat.message.MessageConverter;
import org.metaagent.framework.core.tool.executor.ToolExecutor;
import org.metaagent.framework.core.tool.executor.ToolExecutorContext;
import org.metaagent.framework.core.tool.tools.spring.ToolCallbackUtils;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
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
import java.util.Map;
import java.util.Objects;

/**
 * ChatModelClient is a client for interacting with a chat model.
 *
 * @author vyckey
 */
public class ChatModelClient implements AutoCloseable {
    private ChatModelInstance chatModel;
    private List<SystemMessage> systemMessages;
    private List<org.springframework.ai.chat.messages.Message> historyMessages;
    private final MessageConverter messageConverter = new MessageConverter(true);
    private ToolExecutor toolExecutor;
    private ToolExecutorContext toolExecutorContext;
    private final Map<ModelId, TokenUsage> tokenUsages = Maps.newHashMap();

    public ChatModelClient(ChatModelInstance chatModel) {
        this.chatModel = chatModel;
    }

    public ChatModelInstance getChatModel() {
        return Objects.requireNonNull(chatModel, "chatModel cannot be null");
    }

    public void setChatModel(ChatModelInstance chatModel) {
        this.chatModel = Objects.requireNonNull(chatModel, "chatModel cannot be null");
    }

    public List<SystemMessage> getSystemMessages() {
        return systemMessages;
    }

    public void setSystemMessages(List<SystemMessage> systemMessages) {
        this.systemMessages = Objects.requireNonNull(systemMessages, "systemMessages is required");
    }

    public void setSystemMessage(SystemMessage... systemMessages) {
        setSystemMessages(List.of(systemMessages));
    }

    public List<org.springframework.ai.chat.messages.Message> getHistoryMessages() {
        return historyMessages;
    }

    public void setHistoryMessages(List<org.springframework.ai.chat.messages.Message> historyMessages) {
        this.historyMessages = Objects.requireNonNull(historyMessages, "historyMessages is required");
    }

    public void setToolContext(ToolExecutorContext toolExecutorContext, ToolExecutor toolExecutor) {
        this.toolExecutorContext = toolExecutorContext;
        this.toolExecutor = toolExecutor;
    }

    public Flux<ChatStreamResponse> sendMessages(Message... messages) {
        return sendMessageStream(Arrays.asList(messages));
    }

    public ChatModelResponse sendMessages(List<Message> messages) {
        messages.stream().map(messageConverter::convert).flatMap(List::stream).forEach(historyMessages::add);

        Prompt prompt = buildPrompt(getChatModel().getRuntime().getDefaultOptions());
        return call(prompt, null);
    }

    public Flux<ChatStreamResponse> sendMessageStream(List<Message> messages) {
        messages.stream().map(messageConverter::convert).flatMap(List::stream).forEach(historyMessages::add);
        Prompt prompt = buildPrompt(getChatModel().getRuntime().getDefaultOptions());

        return stream(prompt, null);
    }

    protected Prompt buildPrompt(ChatOptions chatOptions) {
        List<org.springframework.ai.chat.messages.Message> messageList = Lists.newArrayList();
        messageList.addAll(systemMessages);
        messageList.addAll(historyMessages);

        if (toolExecutorContext != null) {
            chatOptions = ToolCallbackUtils.buildChatOptionsWithTools(
                    chatOptions, toolExecutor, toolExecutorContext, false
            );
        }
        return new Prompt(messageList, chatOptions);
    }

    private ChatResponse mergeResponse(ChatResponse chatResponse, ChatResponseMetadata previousResponseMetadata) {
        if (previousResponseMetadata == null) {
            return chatResponse;
        }

        TokenUsage accumulatedTokenUsage = TokenUsage.empty()
                .accumulate(chatResponse.getMetadata().getUsage())
                .accumulate(previousResponseMetadata.getTokenUsage());
        return ChatResponse.builder()
                .from(chatResponse)
                .metadata(ChatResponseUtils
                        .newBuilder(chatResponse.getMetadata())
                        .usage(accumulatedTokenUsage)
                        .build())
                .build();
    }

    protected ChatModelResponse call(Prompt prompt, ChatModelResponse previousResponse) {
        ChatResponse response = getChatModel().getRuntime().call(prompt);
        response = mergeResponse(response, previousResponse != null ? previousResponse.metadata() : null);

        DefaultChatResponseMetadata.Builder metadataBuilder = ChatResponseMetadata.builder()
                .metadata(response.getMetadata());
        List<MessagePart> messageParts = new ArrayList<>();
        for (Generation generation : response.getResults()) {
            metadataBuilder.finishReason(generation.getMetadata().getFinishReason());
            messageParts.addAll(messageConverter.convert(generation.getOutput()));
        }

        if (!response.hasToolCalls()) {
            response.getResults().forEach(generation -> historyMessages.add(generation.getOutput()));
            return new ChatModelResponse(messageParts, metadataBuilder.build());
        }

        ToolCallingManager toolCallingManager = getToolCallingManager();
        ToolExecutionResult executionResult = toolCallingManager.executeToolCalls(prompt, response);
        messageParts.addAll(buildToolCallResultMessageParts(executionResult));

        if (executionResult.returnDirect()) {
            List<Generation> generations = ToolExecutionResult.buildGenerations(executionResult);
            generations.forEach(generation -> historyMessages.add(generation.getOutput()));

            return new ChatModelResponse(messageParts, metadataBuilder.build());
        } else {
            List<org.springframework.ai.chat.messages.Message> conversationHistory
                    = executionResult.conversationHistory();
            var newMessages = conversationHistory.subList(prompt.getInstructions().size(), conversationHistory.size());
            historyMessages.addAll(newMessages);

            Prompt newPrompt = new Prompt(conversationHistory, prompt.getOptions());
            ChatModelResponse currentResponse = new ChatModelResponse(messageParts, metadataBuilder.build());
            return call(newPrompt, currentResponse);
        }
    }

    protected Flux<ChatStreamResponse> stream(Prompt prompt, ChatStreamResponse previousResponse) {
        return Flux.deferContextual(contextView -> {
            Flux<ChatResponse> chatResponseFlux = getChatModel().getRuntime().stream(prompt)
                    .map(ChatResponseUtils::rebuildResponseIfRequired)
                    .switchMap(response -> {
                        ChatResponse mergedResponse = mergeResponse(
                                response, previousResponse != null ? previousResponse.responseMetadata() : null);
                        return Mono.just(mergedResponse);
                    });

            return chatResponseFlux.flatMap(chatResponse -> {
                DefaultChatResponseMetadata.Builder metadataBuilder = ChatResponseMetadata.builder()
                        .metadata(chatResponse.getMetadata());
                List<ChatStreamResponse> streamResponses = new ArrayList<>();
                for (Generation generation : chatResponse.getResults()) {
                    metadataBuilder.finishReason(generation.getMetadata().getFinishReason());
                    for (MessagePart messagePart : messageConverter.convert(generation.getOutput())) {
                        streamResponses.add(new ChatStreamResponse(messagePart, metadataBuilder.build()));
                    }
                }

                if (!chatResponse.hasToolCalls()) {
                    chatResponse.getResults().forEach(generation -> historyMessages.add(generation.getOutput()));
                    return Flux.fromIterable(streamResponses);
                }

                ToolCallingManager toolCallingManager = getToolCallingManager();
                ToolExecutionResult executionResult = toolCallingManager.executeToolCalls(prompt, chatResponse);
                buildToolCallResultMessageParts(executionResult).stream()
                        .map(message -> new ChatStreamResponse(message, metadataBuilder.build()))
                        .forEach(streamResponses::add);

                if (executionResult.returnDirect()) {
                    List<Generation> generations = ToolExecutionResult.buildGenerations(executionResult);
                    generations.forEach(generation -> historyMessages.add(generation.getOutput()));

                    return Flux.fromIterable(streamResponses);
                } else {
                    List<org.springframework.ai.chat.messages.Message> conversationHistory
                            = executionResult.conversationHistory();
                    var newMessages = conversationHistory.subList(prompt.getInstructions().size(), conversationHistory.size());
                    historyMessages.addAll(newMessages);

                    Prompt newPrompt = new Prompt(conversationHistory, prompt.getOptions());
                    Flux<ChatStreamResponse> currentStream = Flux.fromIterable(streamResponses);
                    Flux<ChatStreamResponse> continuation = stream(newPrompt, streamResponses.get(streamResponses.size() - 1));
                    return Flux.concat(currentStream, continuation);
                }
            });
        });
    }

    private List<ToolCallResultMessagePart> buildToolCallResultMessageParts(ToolExecutionResult toolExecutionResult) {
        var conversationHistory = toolExecutionResult.conversationHistory();
        List<ToolCallResultMessagePart> messageParts = new ArrayList<>();
        if (conversationHistory.get(conversationHistory.size() - 1) instanceof ToolResponseMessage toolResponseMessage) {
            for (ToolResponseMessage.ToolResponse toolResponse : toolResponseMessage.getResponses()) {
                ToolCallResultMessagePart messagePart = ToolCallResultMessagePart.builder()
                        .toolResponse(new ToolCallResultMessagePart.ToolResponse(
                                toolResponse.id(),
                                toolResponse.name(),
                                toolResponse.responseData()
                        ))
                        .build();
                messageParts.add(messagePart);
            }
        }
        return messageParts;
    }

    protected ToolCallingManager getToolCallingManager() {
        Field managerField = ReflectionUtils.findField(getChatModel().getClass(), "toolCallingManager", ToolCallingManager.class);
        if (managerField != null) {
            managerField.setAccessible(true);
            try {
                return (ToolCallingManager) managerField.get(getChatModel());
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
        this.toolExecutorContext = null;
        this.tokenUsages.clear();
    }

    @Override
    public void close() {
        reset();
    }
}
