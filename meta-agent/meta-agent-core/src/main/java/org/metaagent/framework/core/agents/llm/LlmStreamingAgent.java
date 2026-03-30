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

import com.google.common.collect.Lists;
import io.micrometer.observation.ObservationRegistry;
import org.apache.commons.collections.CollectionUtils;
import org.metaagent.framework.common.metadata.MetadataProvider;
import org.metaagent.framework.common.util.IdGenerator;
import org.metaagent.framework.core.agent.AbstractStreamAgent;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.MessageId;
import org.metaagent.framework.core.agent.chat.message.RoleMessageInfo;
import org.metaagent.framework.core.agent.chat.message.part.MessagePart;
import org.metaagent.framework.core.agent.chat.message.part.MessagePartId;
import org.metaagent.framework.core.agent.event.AgentEventBus;
import org.metaagent.framework.core.agent.event.AgentMessageEvent;
import org.metaagent.framework.core.agent.output.aggregator.StreamMessageAggregator;
import org.metaagent.framework.core.agents.llm.context.LlmAgentContext;
import org.metaagent.framework.core.agents.llm.context.LlmAgentStepContext;
import org.metaagent.framework.core.agents.llm.input.LlmAgentInput;
import org.metaagent.framework.core.agents.llm.message.LlmFinishMessagePart;
import org.metaagent.framework.core.agents.llm.message.LlmStartMessagePart;
import org.metaagent.framework.core.agents.llm.message.LlmStreamMessageAggregator;
import org.metaagent.framework.core.agents.llm.message.MessageConverter;
import org.metaagent.framework.core.agents.llm.message.ToolCallMessagePart;
import org.metaagent.framework.core.agents.llm.output.LlmAgentStreamOutput;
import org.metaagent.framework.core.model.chat.ChatModelInstance;
import org.metaagent.framework.core.model.chat.ChatResponseUtils;
import org.metaagent.framework.core.model.provider.ModelProviderUtils;
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

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * LlmStreamingAgent is a streaming agent that uses a Large Language Model (LLM) to generate streaming responses to user input.
 *
 * @author vyckey
 * @see LlmAgentInput
 * @see LlmAgentStreamOutput
 */
public class LlmStreamingAgent extends AbstractStreamAgent<LlmAgentInput, LlmAgentStreamOutput, LlmAgentStepContext, MessagePart> {
    private final MessageConverter messageConverter = new MessageConverter();

    public LlmStreamingAgent(String name) {
        super(name);
    }

    @Override
    public LlmAgentStepContext createStepContext(LlmAgentInput agentInput) {
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

        MessageId messageId = agentInput.messageIdGenerator().nextId();
        LlmAgentStepContext stepContext = new LlmAgentStepContext(systemMessages, historyMessages);
        stepContext.setOutputMessageInfo(RoleMessageInfo.assistant().id(messageId).build());

        // Add tool calls to step context
        agentInput.messages().stream().map(Message::parts)
                .flatMap(List::stream)
                .filter(messagePart -> messagePart instanceof ToolCallMessagePart)
                .forEach(messagePart -> stepContext.addToolCallMessage((ToolCallMessagePart) messagePart));

        return stepContext;
    }

    protected ChatOptions buildChatOptions(LlmAgentInput agentInput, ChatOptions defaultOptions) {
        LlmAgentContext agentContext = agentInput.context();
        ChatOptions chatOptions = ToolCallingChatOptions.builder().build();
        if (agentContext.toolExecutorContext() != null) {
            chatOptions = ToolCallbackUtils.buildChatOptionsWithTools(
                    chatOptions, agentContext.toolExecutor(), agentContext.toolExecutorContext(), false
            );
        }
        return chatOptions;
    }

    @Override
    protected LlmAgentStreamOutput buildAgentOutput(LlmAgentInput agentInput, LlmAgentStepContext stepContext, Flux<MessagePart> stream) {
        return LlmAgentStreamOutput.builder()
                .messageInfo(stepContext.getOutputMessageInfo())
                .stream(stream)
                .build();
    }

    @Override
    protected boolean shouldContinueLoop(LlmAgentInput agentInput, LlmAgentStepContext stepContext) {
        int currentIteration = stepContext.getLoopCounter().get();
        if (agentInput.maxSteps() != null && agentInput.maxSteps() > 0 && currentIteration >= agentInput.maxSteps()) {
            return false;
        }

        return !stepContext.isFinished();
    }

    @Override
    public Flux<MessagePart> runStream(LlmAgentInput agentInput, LlmAgentStepContext stepContext) {
        AgentEventBus agentEventBus = agentInput.context().agentEventBus();
        Consumer<MessagePart> messageHandler = messagePart -> {
            stepContext.addOutputMessageParts(List.of(messagePart));
            agentEventBus.publish(AgentMessageEvent.builder()
                    .agent(this)
                    .messageInfo(stepContext.getOutputMessageInfo())
                    .messagePart(messagePart)
                    .build()
            );
        };

        AggregatedMessagePublisher messagePublisher = new AggregatedMessagePublisher(
                messageHandler, LlmStreamMessageAggregator.INSTANCE);

        return super.runStream(agentInput, stepContext)
                .doOnNext(messagePublisher::add)
                .doFinally(signalType -> messagePublisher.flush());
    }

    /**
     * Performs a single step of streaming execution.
     * This includes one LLM call and any tool execution (without recursive calls).
     */
    @Override
    protected Flux<MessagePart> doStepStream(LlmAgentInput agentInput, LlmAgentStepContext stepContext) {
        LlmAgentContext agentContext = agentInput.context();
        ChatModelInstance modelInstance = ModelProviderUtils.getChatModel(agentContext.modelProviderRegistry(), agentInput.modelId());

        ChatOptions chatOptions = buildChatOptions(agentInput, null);
        Prompt prompt = new Prompt(stepContext.getAllMessages(), chatOptions);

        return modelInstance.getRuntime()
                .stream(prompt)
                .map(ChatResponseUtils::rebuildResponseIfRequired)
                .concatMap(chatResponse -> {
                    List<MessagePart> messageParts = parseOutputMessageParts(
                            chatResponse, stepContext, agentInput.messagePartIdGenerator());
                    Flux<MessagePart> messageFlux = Flux.fromIterable(messageParts);
                    if (!chatResponse.hasToolCalls()) {
                        return messageFlux;
                    }

                    return messageFlux.concatWith(Flux.defer(() ->
                            Flux.fromIterable(executeToolCalls(modelInstance, prompt, chatResponse, stepContext))
                    ));
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
    }

    protected List<MessagePart> parseOutputMessageParts(ChatResponse chatResponse, LlmAgentStepContext stepContext,
                                                        IdGenerator<MessagePartId> partIdGenerator) {
        List<MessagePart> outputMessages = Lists.newArrayList();

        if (chatResponse.getResult() != null) {
            Generation generation = chatResponse.getResult();
            stepContext.addNewMessages(Collections.singletonList(generation.getOutput()));

            if (!stepContext.isFinished()) {
                stepContext.setFinishReason(generation.getMetadata().getFinishReason());
            }

            List<MessagePart> messageParts = messageConverter.convert(
                    partIdGenerator, generation.getOutput(), Collections.emptyMap());
            outputMessages.addAll(messageParts);

            for (MessagePart messagePart : messageParts) {
                if (messagePart instanceof ToolCallMessagePart) {
                    stepContext.addToolCallMessage((ToolCallMessagePart) messagePart);
                }
            }
        } else {
            stepContext.setFinishReason(null);
        }

        stepContext.addTokenUsage(chatResponse.getMetadata().getUsage());
        return outputMessages;
    }

    protected List<ToolCallMessagePart> executeToolCalls(
            ChatModelInstance modelInstance,
            Prompt prompt,
            ChatResponse chatResponse,
            LlmAgentStepContext stepContext) {
        Generation generation = Objects.requireNonNull(chatResponse.getResult(), "generation is required");

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
            var newMessages = conversationHistory.subList(prompt.getInstructions().size() + 1, conversationHistory.size());
            stepContext.addNewMessages(newMessages);
        }

        List<ToolCallMessagePart> toolResultParts = buildToolCallResultMessageParts(stepContext, executionResult);
        stepContext.setFinishReason(generation.getMetadata().getFinishReason());
        return toolResultParts;
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
    public void close() {
    }


    static class AggregatedMessagePublisher {
        final List<MessagePart> pendingMessages = new ArrayList<>();
        final Consumer<MessagePart> publisher;
        final StreamMessageAggregator aggregator;

        AggregatedMessagePublisher(Consumer<MessagePart> publisher, StreamMessageAggregator aggregator) {
            this.publisher = Objects.requireNonNull(publisher, "message publisher is required");
            this.aggregator = Objects.requireNonNull(aggregator, "stream message aggregator is required");
        }

        public void add(MessagePart message) {
            if (aggregator.canAggregateWith(message, pendingMessages)) {
                pendingMessages.add(message);
            } else {
                List<MessagePart> aggregatedMessages = aggregator.aggregate(pendingMessages);
                aggregatedMessages.forEach(publisher);

                pendingMessages.clear();
                pendingMessages.add(message);
            }
        }

        public void flush() {
            List<MessagePart> aggregatedMessages = aggregator.aggregate(pendingMessages);
            aggregatedMessages.forEach(publisher);
            pendingMessages.clear();
        }
    }
}
