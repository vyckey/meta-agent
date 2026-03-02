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
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.metaagent.framework.common.metadata.MetadataProvider;
import org.metaagent.framework.core.agent.AbstractAgentBuilder;
import org.metaagent.framework.core.agent.AbstractStreamAgent;
import org.metaagent.framework.core.agent.AgentExecutionException;
import org.metaagent.framework.core.agent.AgentInterruptedException;
import org.metaagent.framework.core.agent.chat.conversation.ConversationInMemoryStorage;
import org.metaagent.framework.core.agent.chat.conversation.ConversationStorage;
import org.metaagent.framework.core.agent.chat.conversation.DefaultMessageTurn;
import org.metaagent.framework.core.agent.chat.conversation.DefaultTurnBasedConversation;
import org.metaagent.framework.core.agent.chat.conversation.MessageTurn;
import org.metaagent.framework.core.agent.chat.conversation.TurnBasedConversation;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.MessageId;
import org.metaagent.framework.core.agent.chat.message.MessageInfo;
import org.metaagent.framework.core.agent.chat.message.RoleMessage;
import org.metaagent.framework.core.agent.chat.message.RoleMessageInfo;
import org.metaagent.framework.core.agent.chat.message.part.MessagePart;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.metaagent.framework.core.agent.output.StreamOutput;
import org.metaagent.framework.core.agent.profile.AgentProfile;
import org.metaagent.framework.core.agents.chat.ChatAgent;
import org.metaagent.framework.core.agents.chat.ChatAgentTask;
import org.metaagent.framework.core.agents.chat.DefaultChatAgentTask;
import org.metaagent.framework.core.agents.chat.input.ChatInput;
import org.metaagent.framework.core.agents.chat.input.DefaultChatInput;
import org.metaagent.framework.core.agents.chat.model.ChatModelClient;
import org.metaagent.framework.core.agents.chat.model.ChatModelResponse;
import org.metaagent.framework.core.agents.chat.model.ChatStreamResponse;
import org.metaagent.framework.core.agents.chat.output.ChatOutput;
import org.metaagent.framework.core.agents.chat.output.DefaultChatOutput;
import org.metaagent.framework.core.agents.chat.output.DefaultChatStreamOutput;
import org.metaagent.framework.core.model.ModelId;
import org.metaagent.framework.core.model.chat.ChatModelInfo;
import org.metaagent.framework.core.model.chat.ChatModelInstance;
import org.metaagent.framework.core.model.chat.compression.ChatCompressionModel;
import org.metaagent.framework.core.model.chat.compression.CompressOptions;
import org.metaagent.framework.core.model.chat.compression.CompressionModel;
import org.metaagent.framework.core.model.chat.compression.CompressionRequest;
import org.metaagent.framework.core.model.chat.compression.CompressionResponse;
import org.metaagent.framework.core.model.chat.compression.CompressionResult;
import org.metaagent.framework.core.model.chat.compression.DefaultCompressOptions;
import org.metaagent.framework.core.model.chat.message.MessageConverter;
import org.metaagent.framework.core.model.prompt.Prompt;
import org.metaagent.framework.core.model.prompt.PromptTemplate;
import org.metaagent.framework.core.model.prompt.PromptValue;
import org.metaagent.framework.core.model.prompt.StringPromptTemplate;
import org.metaagent.framework.core.model.prompt.StringPromptValue;
import org.metaagent.framework.core.model.prompt.registry.PromptRegistry;
import org.metaagent.framework.core.model.provider.ModelProviderRegistry;
import org.metaagent.framework.core.model.provider.ModelProviderUtils;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.model.ChatModel;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.metaagent.framework.core.agent.chat.message.MessageMetadataKeys.KEY_MESSAGE_ID;

/**
 * {@link ChatAgent} implementation with Large Language Model (LLM).
 *
 * @author vyckey
 */
public class LlmChatAgent extends AbstractStreamAgent<ChatInput, ChatOutput, MessagePart>
        implements ChatAgent<ChatInput, ChatOutput> {
    public static final String DEFAULT_SYSTEM_PROMPT_ID = "framework:chat_agent_system_prompt";
    public static final String DEFAULT_CONTEXT_COMPRESSION_SYSTEM_PROMPT_ID = "framework:chat_agent_context_compression_system_prompt";
    public static final float DEFAULT_CONTEXT_COMPRESSION_RATIO = 0.8f;
    public static final int DEFAULT_COMPRESSION_RESERVED_MESSAGES_COUNT = 5;
    protected static final ChatAgentTask EXIT_TASK = new DefaultChatAgentTask(
            AgentInput.create(ChatInput.builder().messages().build())
    );

    /**
     * The flag {@code exit} indicates whether the agent should exit.
     */
    protected volatile boolean exit = false;

    /**
     * The flag {@code streaming} indicates whether the agent is currently streaming.
     * If the agent is currently streaming, we shouldn't process the next input.
     */
    protected final Object streamingLock = new Object();
    protected boolean streaming = false;

    /**
     * The queue of pending inputs which are waiting to be processed.
     * The {@code processExecutor} must have only one thread to process
     * the inputs ensuring the agent only processes one input at a time.
     */
    protected BlockingQueue<ChatAgentTask> pendingInputs = new LinkedBlockingQueue<>();
    protected ExecutorService taskExecutor = Executors.newSingleThreadExecutor();

    protected ModelProviderRegistry modelProviderRegistry;
    protected ModelId defaultModelId;
    protected ModelId currentModelId;
    protected ChatModelClient chatModelClient;
    protected Prompt systemPrompt;
    protected MessageConverter messageConverter;
    protected CompressionModel compressionModel;

    protected TurnBasedConversation conversation;
    protected ConversationStorage conversationStorage;
    protected ConversationStorage compressedConversationStorage;

    public LlmChatAgent(String name, String description, ModelId defaultModelId) {
        this(name, description, defaultModelId, null);
    }

    public LlmChatAgent(String name, String description, ModelId defaultModelId, Prompt systemPrompt) {
        super(AgentProfile.create(name, description));
        this.modelProviderRegistry = ModelProviderRegistry.global();
        this.defaultModelId = Objects.requireNonNull(defaultModelId, "defaultModelId cannot be null");
        this.currentModelId = defaultModelId;
        ChatModelInstance chatModel = ModelProviderUtils.getChatModel(modelProviderRegistry, defaultModelId);
        this.chatModelClient = new ChatModelClient(chatModel);
        this.systemPrompt = systemPrompt != null ?
                systemPrompt : PromptRegistry.global().getPromptTemplate(DEFAULT_SYSTEM_PROMPT_ID);
        this.messageConverter = new MessageConverter(true);
        this.compressionModel = defaultCompressionModel(chatModel.getRuntime());
        this.conversation = new DefaultTurnBasedConversation();
        this.conversationStorage = ConversationInMemoryStorage.INSTANCE;
        this.compressedConversationStorage = ConversationInMemoryStorage.INSTANCE;
    }

    public LlmChatAgent(Builder builder) {
        super(builder);
        this.modelProviderRegistry = builder.modelProviderRegistry != null ?
                builder.modelProviderRegistry : ModelProviderRegistry.global();
        this.defaultModelId = Objects.requireNonNull(builder.defaultModelId, "defaultModelId cannot be null");
        this.currentModelId = defaultModelId;
        ChatModelInstance chatModel = ModelProviderUtils.getChatModel(modelProviderRegistry, defaultModelId);
        this.chatModelClient = new ChatModelClient(chatModel);
        this.systemPrompt = builder.systemPrompt != null ?
                builder.systemPrompt : PromptRegistry.global().getPromptTemplate(DEFAULT_SYSTEM_PROMPT_ID);
        this.messageConverter = new MessageConverter(true);
        this.compressionModel = builder.compressionModel != null ?
                builder.compressionModel : defaultCompressionModel(chatModel.getRuntime());
        this.conversation = builder.conversation != null ? builder.conversation : new DefaultTurnBasedConversation();
        this.conversationStorage = builder.conversationStorage != null ?
                builder.conversationStorage : ConversationInMemoryStorage.INSTANCE;
        this.compressedConversationStorage = builder.compressedConversationStorage != null ?
                builder.compressedConversationStorage : ConversationInMemoryStorage.INSTANCE;
    }

    static {
        PromptRegistry.global().registerPromptTemplate(DEFAULT_SYSTEM_PROMPT_ID,
                StringPromptTemplate.fromFile("agents/prompts/chat_agent_system_prompt.md"));
        PromptRegistry.global().registerPrompt(DEFAULT_CONTEXT_COMPRESSION_SYSTEM_PROMPT_ID,
                StringPromptValue.fromFile("agents/prompts/chat_agent_context_compression_system_prompt.md"));
    }

    public static Builder builder() {
        return new Builder();
    }

    private static CompressionModel defaultCompressionModel(ChatModel chatModel) {
        PromptValue prompt = PromptRegistry.global().getPrompt(DEFAULT_CONTEXT_COMPRESSION_SYSTEM_PROMPT_ID);
        return new ChatCompressionModel(chatModel, new SystemMessage(prompt.text()));
    }

    protected ChatModelInstance getChatModel() {
        return ModelProviderUtils.getModel(modelProviderRegistry, currentModelId, ChatModelInstance.class);
    }

    @Override
    public TurnBasedConversation getConversation() {
        return conversation;
    }

    @Override
    public void doInitialize() {
        super.doInitialize();
        conversationStorage.load(conversation);
        taskExecutor.submit(this::processPendingInputs);
    }

    private void ensureInitialized() {
        if (!initialized) {
            initialize();
        }
    }

    /**
     * Creates a {@link ChatAgentTask} offered to pending queue and waits for the result synchronously.
     *
     * @param agentInput the agent input
     * @return the agent output
     */
    @Override
    public AgentOutput<ChatOutput> run(AgentInput<ChatInput> agentInput) {
        ensureInitialized();
        checkInput(agentInput);

        ChatAgentTask agentTask = createChatTask(agentInput);
        try {
            return agentTask.outputFuture().get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AgentInterruptedException("agent interrupted", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new AgentExecutionException("agent execution failed", cause);
        }
    }

    /**
     * Creates a {@link ChatAgentTask} offered to pending queue and returns a {@link CompletableFuture}
     * to get the result asynchronously.
     *
     * @param agentInput the agent input
     * @return the agent output
     */
    @Override
    public CompletableFuture<AgentOutput<ChatOutput>> runAsync(AgentInput<ChatInput> agentInput) {
        ensureInitialized();
        checkInput(agentInput);

        ChatAgentTask agentTask = createChatTask(agentInput);
        return agentTask.outputFuture();
    }

    /**
     * Creates a {@link ChatAgentTask} offered to pending queue and waits for the streaming result synchronously.
     *
     * @param agentInput the agent input
     * @return the agent output
     */
    @Override
    public AgentOutput<ChatOutput> runStream(AgentInput<ChatInput> agentInput) {
        ensureInitialized();
        checkInput(agentInput);

        ChatInput chatInput = agentInput.input();
        if (BooleanUtils.isFalse(chatInput.isStreamingEnabled())) {
            throw new IllegalArgumentException("Streaming is not enabled.");
        }

        if (BooleanUtils.isTrue(chatInput.isStreamingEnabled())) {
            return run(agentInput);
        } else {
            // Enable streaming for the current input
            ChatInput newChatInput = DefaultChatInput.builder(chatInput).isStreamingEnabled(true).build();
            return run(newChatInput);
        }
    }

    protected ChatAgentTask createChatTask(AgentInput<ChatInput> agentInput) {
        ChatAgentTask agentTask = new DefaultChatAgentTask(agentInput);
        if (!pendingInputs.offer(agentTask)) {
            throw new AgentExecutionException("Too many pending input task to process.");
        }
        return agentTask;
    }

    protected void processPendingInputs() {
        while (!exit) {
            try {
                // If agent is streaming, we need to wait for the previous task to finish
                synchronized (streamingLock) {
                    while (streaming && !exit) {
                        try {
                            streamingLock.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }

                if (Thread.currentThread().isInterrupted()) {
                    break;
                }

                ChatAgentTask agentTask = pendingInputs.take();
                if (EXIT_TASK.equals(agentTask) || exit) {
                    break;
                }

                processChatTask(agentTask);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("Error processing agent task", e);
            }
        }
    }

    /**
     * Process a single input task.
     */
    protected void processChatTask(ChatAgentTask chatTask) {
        AgentInput<ChatInput> agentInput = chatTask.input();
        ChatInput chatInput = agentInput.input();
        boolean streamingEnabled = BooleanUtils.isTrue(chatInput.isStreamingEnabled());
        if (streamingEnabled) {
            try {
                synchronized (streamingLock) {
                    streaming = true;
                }
                AgentOutput<ChatOutput> agentStreamOutput = super.runStream(agentInput);
                chatTask.outputFuture().complete(agentStreamOutput);
            } catch (Exception e) {
                setStreamingFinished();
                chatTask.outputFuture().completeExceptionally(e);
            }
        } else {
            try {
                AgentOutput<ChatOutput> agentOutput = super.run(agentInput);
                chatTask.outputFuture().complete(agentOutput);
            } catch (Exception e) {
                chatTask.outputFuture().completeExceptionally(e);
            }
        }
    }

    protected void setStreamingFinished() {
        synchronized (streamingLock) {
            streaming = false;
            streamingLock.notify();
        }
    }

    protected void checkInput(AgentInput<ChatInput> agentInput) {
        ChatInput chatInput = agentInput.input();
        if (chatInput.modelId() != null) {
            try {
                ModelProviderUtils.getChatModel(modelProviderRegistry, chatInput.modelId());
            } catch (Exception e) {
                throw new AgentExecutionException(e.getMessage(), e);
            }
        }
    }

    @Override
    protected AgentInput<ChatInput> preprocess(AgentInput<ChatInput> agentInput) {
        agentInput = super.preprocess(agentInput);
        if (agentInput.input().modelId() != null) {
            currentModelId = agentInput.input().modelId();
        }

        PromptValue systemPromptValue = buildSystemPrompt(agentInput);
        chatModelClient.setSystemMessage(new SystemMessage(systemPromptValue.text()));
        chatModelClient.setToolContext(buildToolExecutorContext(agentInput), agentInput.context().getToolExecutor());

        conversation.newTurn();
        agentInput.input().messages().forEach(conversation::appendMessage);
        return agentInput;
    }

    protected PromptValue buildSystemPrompt(AgentInput<ChatInput> agentInput) {
        ChatModelInstance chatModel = getChatModel();
        ChatModelInfo modelInfo = chatModel.getInfo();
        String modelCutoffDate = "Unknown";
        if (modelInfo.getCutOffDate() != null) {
            modelCutoffDate = LocalDate.ofInstant(modelInfo.getCutOffDate(), ZoneId.systemDefault())
                    .format(DateTimeFormatter.ISO_LOCAL_DATE);
        }

        Prompt systemPrompt = Optional.ofNullable(agentInput.input().systemPrompt()).orElse(this.systemPrompt);
        if (systemPrompt instanceof PromptTemplate promptTemplate) {
            return promptTemplate.format(Map.of(
                    "name", getName(),
                    "current_date", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    "model_cutoff_date", modelCutoffDate
            ));
        } else {
            return (PromptValue) systemPrompt;
        }
    }

    protected void compressContextIfNeeded(AgentInput<ChatInput> input) {
        List<Message> messages = Lists.newArrayList(conversation);
        List<org.springframework.ai.chat.messages.Message> historyMessages = messages.stream()
                .map(message -> messageConverter.convert(message))
                .flatMap(List::stream).toList();

        MetadataProvider agentMetadata = getAgentProfile().getMetadata();
        float contextCompressionRatio = agentMetadata.getProperty(ChatAgent.METADATA_KEY_CONTEXT_COMPRESSION_RATIO,
                Float.class, DEFAULT_CONTEXT_COMPRESSION_RATIO);
        int maxMessagesCount = agentMetadata.getProperty(ChatAgent.METADATA_KEY_COMPRESSION_RESERVED_MESSAGES_COUNT,
                Integer.class, DEFAULT_COMPRESSION_RESERVED_MESSAGES_COUNT);

        ChatModelInstance chatModel = getChatModel();
        int maxTokens = (int) (chatModel.getInfo().getContextSize() * contextCompressionRatio);
        CompressOptions compressOptions = DefaultCompressOptions.builder()
                .maxTokens(maxTokens)
                .reservedMessagesCount(maxMessagesCount)
                .build();

        CompressionResponse compressionResponse = compressionModel.call(new CompressionRequest(historyMessages, compressOptions));
        CompressionResult compressionResult = Objects.requireNonNull(compressionResponse.getResult());
        if (compressionResult.isCompressed()) {
            applyCompressionResult(compressionResult);
        }
    }

    protected void applyCompressionResult(CompressionResult compressionResult) {
        String summaryText = compressionResult.getSummary().getText();
        String finalSummaryText = String.format("<context-summary time=\"%s\">\n%s\n</context-summary>", LocalDateTime.now(), summaryText);

        List<org.springframework.ai.chat.messages.Message> retainedMessages = compressionResult.getRetainedMessages();
        List<MessageTurn> turnsToReserve = Lists.newArrayList();
        if (!retainedMessages.isEmpty()) {
            // Remove turns before the first reserved message
            MessageId firstReservedMessageId = Optional
                    .ofNullable(MapUtils.getString(retainedMessages.get(0).getMetadata(), KEY_MESSAGE_ID))
                    .map(MessageId::of).orElse(null);
            List<MessagePart> firstReservedMessageParts = messageConverter.convert(retainedMessages.get(0));

            for (MessageTurn turn : conversation.turns(true)) {
                List<Message> turnMessages = turn.messages();
                int matchIndex = IntStream.range(0, turnMessages.size())
                        .filter(idx -> {
                            Message message = turnMessages.get(idx);
                            if (firstReservedMessageId != null && message.info().id().equals(firstReservedMessageId)) {
                                return true;
                            }
                            return !message.parts().isEmpty() && message.parts().get(0).equals(firstReservedMessageParts.get(0));
                        })
                        .findFirst()
                        .orElse(-1);
                if (matchIndex >= 0) {
                    MessageTurn turnToReserve = new DefaultMessageTurn(turnMessages.subList(matchIndex, turnMessages.size()), true);
                    turnsToReserve.add(turnToReserve);
                    break;
                } else {
                    turnsToReserve.add(turn);
                }
            }
            conversation.resetAfter(firstReservedMessageId, false);
        }
        Collections.reverse(turnsToReserve);

        // Store the compressed conversation
        compressedConversationStorage.store(conversation);

        // Clear and reset the conversation
        conversation.clear();
        turnsToReserve.forEach(conversation::appendTurn);

        // Reset the history messages of model client
        List<org.springframework.ai.chat.messages.Message> historyMessages = Lists.newArrayList();
        historyMessages.add(new AssistantMessage(finalSummaryText));
        historyMessages.addAll(retainedMessages);
        chatModelClient.setHistoryMessages(historyMessages);
    }

    @Override
    protected AgentOutput<ChatOutput> doStep(AgentInput<ChatInput> agentInput) {
        ChatInput input = agentInput.input();

        ChatModelResponse response = chatModelClient.sendMessages(input.messages());

        List<MessagePart> messageParts = response.messages();
        Message outputMessage = RoleMessage.builder()
                .info(MessageInfo.assistant()
                        .createdAt(messageParts.get(0).createdAt())
                        .updatedAt(messageParts.get(messageParts.size() - 1).updatedAt())
                        .build()
                )
                .parts(messageParts)
                .build();
        conversation.appendMessage(outputMessage);

        ChatOutput agentOutput = ChatOutput.builder().message(outputMessage).build();
        return AgentOutput.create(agentOutput);
    }

    @Override
    protected AgentOutput<ChatOutput> stepStream(AgentInput<ChatInput> input, Consumer<AgentOutput<ChatOutput>> onStreamComplete) {
        compressContextIfNeeded(input);

        return super.stepStream(input, agentOutput -> {
            ChatOutput chatOutput = agentOutput.result();
            conversation.appendMessage(chatOutput.message());
            onStreamComplete.accept(agentOutput);
        });
    }

    @Override
    protected Flux<MessagePart> doRunStreamComplete(Flux<MessagePart> stream) {
        // Set streaming finished to allow processing next input
        return super.doRunStreamComplete(stream)
                .doOnComplete(this::setStreamingFinished)
                .doOnError(e -> setStreamingFinished());
    }

    @Override
    protected AgentOutput<ChatOutput> doStepStream(AgentInput<ChatInput> agentInput) {
        ChatInput input = agentInput.input();
        RoleMessageInfo messageInfo = RoleMessageInfo.assistant()
                .build();

        Flux<ChatStreamResponse> messageFlux = chatModelClient.sendMessageStream(input.messages());
        ChatOutput chatOutput = ChatOutput.builder()
                .message(RoleMessage.builder().info(messageInfo).build())
                .stream(messageFlux.map(ChatStreamResponse::message))
                .build();
        return AgentOutput.create(chatOutput);
    }

    @Override
    public StreamOutput.Aggregator<MessagePart, ChatOutput> getStreamOutputAggregator() {
        RoleMessageInfo messageInfo = RoleMessageInfo.assistant()
                .build();
        return DefaultChatStreamOutput.aggregator(messageInfo);
    }

    @Override
    protected AgentOutput<ChatOutput> rebuildOutput(AgentInput<ChatInput> input, AgentOutput<ChatOutput> agentOutput, Flux<MessagePart> stream) {
        ChatOutput chatOutput = DefaultChatOutput.builder(agentOutput.result()).stream(stream).build();
        return AgentOutput.create(chatOutput, agentOutput.metadata());
    }

    @Override
    public void reset() {
        super.reset();
        chatModelClient.reset();
        getConversation().clear();
    }

    @Override
    public void close() {
        exit = true;
        pendingInputs.clear();
        pendingInputs.offer(EXIT_TASK);

        this.conversationStorage.store(conversation);
        try {
            this.conversationStorage.close();
            this.compressedConversationStorage.close();
        } catch (IOException e) {
            logger.error("Failed to close conversation storage", e);
        }

        taskExecutor.shutdown();
        try {
            if (!taskExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                taskExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error("Failed to shutdown process executor", e);
            Thread.currentThread().interrupt();
        }

        chatModelClient.close();
    }


    public static class Builder extends AbstractAgentBuilder<LlmChatAgent, Builder, ChatInput, ChatOutput> {
        private ModelProviderRegistry modelProviderRegistry;
        private ModelId defaultModelId;
        private Prompt systemPrompt;
        private CompressionModel compressionModel;
        private TurnBasedConversation conversation;
        protected ConversationStorage conversationStorage;
        protected ConversationStorage compressedConversationStorage;

        @Override
        protected Builder self() {
            return this;
        }

        public Builder modelProviderRegistry(ModelProviderRegistry modelProviderRegistry) {
            this.modelProviderRegistry = modelProviderRegistry;
            return self();
        }

        public Builder defaultModelId(ModelId defaultModelId) {
            this.defaultModelId = defaultModelId;
            return self();
        }

        public Builder systemPrompt(Prompt systemPrompt) {
            this.systemPrompt = systemPrompt;
            return self();
        }

        public Builder compressionModel(CompressionModel compressionModel) {
            this.compressionModel = compressionModel;
            return self();
        }

        public Builder conversation(TurnBasedConversation conversation) {
            this.conversation = conversation;
            return self();
        }

        public Builder conversationStorage(ConversationStorage conversationStorage) {
            this.conversationStorage = conversationStorage;
            return self();
        }

        public Builder compressedConversationStorage(ConversationStorage compressedConversationStorage) {
            this.compressedConversationStorage = compressedConversationStorage;
            return self();
        }

        @Override
        public LlmChatAgent build() {
            return new LlmChatAgent(this);
        }
    }
}
