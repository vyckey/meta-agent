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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.micrometer.observation.ObservationRegistry;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.metaagent.framework.common.metadata.MetadataProvider;
import org.metaagent.framework.core.agent.AbstractStreamAgent;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.RoleMessage;
import org.metaagent.framework.core.agent.chat.message.RoleMessageInfo;
import org.metaagent.framework.core.agent.chat.message.part.MessagePart;
import org.metaagent.framework.core.agent.chat.message.part.MessagePartId;
import org.metaagent.framework.core.agents.chat.model.metadata.TokenUsage;
import org.metaagent.framework.core.agents.llm.message.LlmFinishMessagePart;
import org.metaagent.framework.core.agents.llm.message.LlmStartMessagePart;
import org.metaagent.framework.core.agents.llm.message.MessageConverter;
import org.metaagent.framework.core.agents.llm.message.ToolCallMessagePart;
import org.metaagent.framework.core.model.chat.ChatModelInstance;
import org.metaagent.framework.core.model.chat.ChatResponseUtils;
import org.metaagent.framework.core.tool.tools.spring.ToolCallbackUtils;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.execution.DefaultToolExecutionExceptionProcessor;
import org.springframework.ai.tool.resolution.DelegatingToolCallbackResolver;
import org.springframework.util.ReflectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * LlmAgent is an agent that uses a Large Language Model (LLM) to generate responses to user input.
 * It can also execute tools in response to user input.
 *
 * @author vyckey
 * @see LlmAgentInput
 * @see LlmAgentOutput
 */
public class LlmAgent extends AbstractStreamAgent<LlmAgentInput, LlmAgentOutput, LlmAgentStepContext, MessagePart> {
    private final MessageConverter messageConverter = new MessageConverter(MessagePartId::next, true);

    public LlmAgent(String name) {
        super(name);
    }

    @Override
    protected LlmAgentStepContext createStepContext(LlmAgentInput agentInput) {
        List<SystemMessage> systemMessages = Collections.emptyList();
        if (CollectionUtils.isNotEmpty(agentInput.systemPrompts())) {
            systemMessages = agentInput.systemPrompts().stream()
                    .map(prompt -> new SystemMessage(prompt.text()))
                    .toList();
        }

        List<org.springframework.ai.chat.messages.Message> historyMessages = Collections.emptyList();
        if (CollectionUtils.isNotEmpty(agentInput.messages())) {
            historyMessages = agentInput.messages().stream()
                    .map(messageConverter::convert)
                    .flatMap(List::stream)
                    .toList();
        }

        LlmAgentStepContext stepContext = new LlmAgentStepContext(systemMessages, historyMessages);
        stepContext.setOutputMessageInfo(RoleMessageInfo.assistant().build());

        // Add tool calls to step context
        agentInput.messages().stream().map(Message::parts)
                .flatMap(List::stream)
                .filter(messagePart -> messagePart instanceof ToolCallMessagePart)
                .forEach(messagePart -> stepContext.addToolCallMessage((ToolCallMessagePart) messagePart));

        return stepContext;
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
    public boolean shouldContinueLoop(LlmAgentInput input, LlmAgentOutput output, LlmAgentStepContext stepContext) {
        // Check max iterations
        int currentIteration = stepContext.getLoopCounter().get();
        if (input.maxSteps() != null && input.maxSteps() > 0 && currentIteration >= input.maxSteps()) {
            return false;
        }

        return !stepContext.isFinished();
    }

    protected ChatOptions buildChatOptions(LlmAgentInput agentInput, ChatOptions defaultOptions) {
        LlmAgentContext agentContext = agentInput.context();
        ChatOptions chatOptions = ToolCallingChatOptions.builder().build();
        if (agentContext.getToolExecutorContext() != null) {
            chatOptions = ToolCallbackUtils.buildChatOptionsWithTools(
                    chatOptions, agentContext.toolExecutor(), agentContext.getToolExecutorContext(), false
            );
        }
        return chatOptions;
    }

    @Override
    public LlmAgentOutput run(LlmAgentInput agentInput) {
        Preconditions.checkArgument(BooleanUtils.isNotTrue(agentInput.streaming()), "argument streaming cannot be true for run");
        return super.run(agentInput);
    }

    @Override
    protected LlmAgentOutput doStep(LlmAgentInput agentInput, LlmAgentStepContext stepContext) {
        LlmAgentContext agentContext = agentInput.context();
        ChatModelInstance modelInstance = agentContext.getChatModelInstance(agentInput.modelId());

        ChatOptions chatOptions = buildChatOptions(agentInput, null);
        Prompt prompt = new Prompt(stepContext.getAllMessages(), chatOptions);

        ChatResponse chatResponse = modelInstance.getRuntime().call(prompt);
        List<MessagePart> outputMessageParts = handleModelResponse(modelInstance, prompt, chatResponse, stepContext);
        stepContext.addOutputMessageParts(outputMessageParts);

        stepContext.addTokenUsage(chatResponse.getMetadata().getUsage());
        return LlmAgentOutput.builder()
                .message(RoleMessage.builder()
                        .info(stepContext.getOutputMessageInfo())
                        .parts(stepContext.getOutputMessageParts())
                        .build())
                .finishReason(stepContext.getFinishReason())
                .tokenUsage(stepContext.getTokenUsage())
                .build();
    }

    @Override
    public LlmAgentOutput runStream(LlmAgentInput agentInput) {
        Preconditions.checkArgument(BooleanUtils.isNotFalse(agentInput.streaming()), "argument streaming cannot be false for runStream");

        return super.runStream(agentInput);
    }

    @Override
    protected LlmAgentOutput doRunStream(LlmAgentInput agentInput, LlmAgentStepContext stepContext) {
        return super.doRunStream(agentInput, stepContext);
    }

    /**
     * Performs a single step of streaming execution.
     * This includes one LLM call and any tool execution (without recursive calls).
     */
    @Override
    protected LlmAgentOutput doStepStream(LlmAgentInput agentInput, LlmAgentStepContext stepContext) {
        LlmAgentContext agentContext = agentInput.context();
        ChatModelInstance modelInstance = agentContext.getChatModelInstance(agentInput.modelId());

        ChatOptions chatOptions = buildChatOptions(agentInput, null);
        Prompt prompt = new Prompt(stepContext.getAllMessages(), chatOptions);
        Flux<MessagePart> stream = executeStream(modelInstance, prompt, agentInput, stepContext);

        return LlmAgentOutput.builder()
                .message(RoleMessage.builder().info(stepContext.getOutputMessageInfo()).build())
                .stream(stream)
                .tokenUsage(TokenUsage.empty())
                .build();
    }

    private Flux<MessagePart> executeStream(
            ChatModelInstance modelInstance,
            Prompt prompt,
            LlmAgentInput agentInput,
            LlmAgentStepContext stepContext) {
        Flux<MessagePart> stream = modelInstance.getRuntime()
                .stream(prompt)
                .map(ChatResponseUtils::rebuildResponseIfRequired)
                .flatMap(chatResponse -> {
                    List<MessagePart> streamMessages = handleModelResponse(modelInstance, prompt, chatResponse, stepContext);
                    return Flux.fromIterable(streamMessages);
                })
                .startWith(Mono.defer(() -> {
                    if (stepContext.getLoopCounter().get() > 0) {
                        return Mono.empty();
                    }
                    return Mono.just(LlmStartMessagePart.builder()
                            .metadata(MetadataProvider.builder()
                                    .build())
                            .build());
                }))
                .concatWith(Mono.defer(() -> {
                    if (stepContext.isFinished()) {
                        LlmFinishMessagePart finishMessagePart = LlmFinishMessagePart.builder()
                                .finishReason(stepContext.getFinishReason())
                                .tokenUsage(stepContext.getTokenUsage())
                                .build();
                        return Mono.just(finishMessagePart);
                    } else {
                        return Mono.empty();
                    }
                }));

        Sinks.Many<MessagePart> sink = Sinks.many().unicast().onBackpressureBuffer();
        stream.subscribe(sink::tryEmitNext, sink::tryEmitError, sink::tryEmitComplete);
        return sink.asFlux();
    }

    protected List<MessagePart> handleModelResponse(
            ChatModelInstance modelInstance,
            Prompt prompt,
            ChatResponse chatResponse,
            LlmAgentStepContext stepContext) {
        List<MessagePart> outputMessages = Lists.newArrayList();
        if (chatResponse.hasToolCalls()) {
            Generation generation = Objects.requireNonNull(chatResponse.getResult(), "generation is required");
            List<MessagePart> messageParts = messageConverter.convert(generation.getOutput());
            outputMessages.addAll(messageParts);
            for (MessagePart messagePart : messageParts) {
                if (messagePart instanceof ToolCallMessagePart) {
                    stepContext.addToolCallMessage((ToolCallMessagePart) messagePart);
                }
            }

            ToolCallingManager toolCallingManager = getToolCallingManager(modelInstance);
            ToolExecutionResult executionResult = toolCallingManager.executeToolCalls(prompt, chatResponse);
            if (executionResult.returnDirect()) {
                AssistantMessage toolCallMessage = generation.getOutput();
                stepContext.addNewMessages(List.of(toolCallMessage));

                List<Generation> toolGenerations = ToolExecutionResult.buildGenerations(executionResult);
                toolGenerations.forEach(toolGeneration ->
                        stepContext.addNewMessages(List.of(toolGeneration.getOutput()))
                );
            } else {
                List<org.springframework.ai.chat.messages.Message> conversationHistory = executionResult.conversationHistory();
                var newMessages = conversationHistory.subList(prompt.getInstructions().size(), conversationHistory.size());
                stepContext.addNewMessages(newMessages);
            }

            List<ToolCallMessagePart> toolResultParts = buildToolCallResultMessageParts(stepContext, executionResult);
            stepContext.setFinishReason(generation.getMetadata().getFinishReason());
            outputMessages.addAll(toolResultParts);
        } else {
            if (chatResponse.getResult() != null) {
                Generation generation = chatResponse.getResult();
                stepContext.addNewMessages(Collections.singletonList(generation.getOutput()));
                if (!stepContext.isFinished()) {
                    stepContext.setFinishReason(generation.getMetadata().getFinishReason());
                }

                outputMessages.addAll(messageConverter.convert(generation.getOutput()));
            } else {
                stepContext.setFinishReason(null);
            }
        }

        stepContext.addTokenUsage(chatResponse.getMetadata().getUsage());
        return outputMessages;
    }

    protected ToolCallingManager getToolCallingManager(ChatModelInstance modelInstance) {
        ChatModel chatModel = modelInstance.getRuntime();
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

    private List<ToolCallMessagePart> buildToolCallResultMessageParts(
            LlmAgentStepContext stepContext, ToolExecutionResult toolExecutionResult) {
        var conversationHistory = toolExecutionResult.conversationHistory();
        List<ToolCallMessagePart> messageParts = new ArrayList<>();
        if (conversationHistory.get(conversationHistory.size() - 1) instanceof ToolResponseMessage toolResponseMessage) {
            for (ToolResponseMessage.ToolResponse toolResponse : toolResponseMessage.getResponses()) {
                ToolCallMessagePart toolCallMessage = stepContext.getToolCallMessage(toolResponse.id());

                ToolCallMessagePart messagePart = ToolCallMessagePart.builder()
                        .callId(toolResponse.id())
                        .toolName(toolResponse.name())
                        .status(ToolCallMessagePart.ToolCallStatus.END)
                        .arguments(toolCallMessage != null ? toolCallMessage.arguments() : "")
                        .response(toolResponse.responseData())
                        .createdAt(toolCallMessage != null ? toolCallMessage.createdAt() : Instant.now())
                        .build();
                messageParts.add(messagePart);
                stepContext.addToolCallMessage(messagePart);
            }
        }
        return messageParts;
    }

    @Override
    protected LlmAgentOutput rebuildOutput(LlmAgentInput agentInput, LlmAgentOutput agentOutput, Flux<MessagePart> stream) {
        return agentOutput.toBuilder()
                .stream(stream)
                .build();
    }

    @Override
    public void close() {
    }

}
