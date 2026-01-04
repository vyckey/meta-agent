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
import org.metaagent.framework.common.metadata.MetadataProvider;
import org.metaagent.framework.core.agent.AbstractAgentBuilder;
import org.metaagent.framework.core.agent.AbstractStreamAgent;
import org.metaagent.framework.core.agent.chat.conversation.ConversationInMemoryStorage;
import org.metaagent.framework.core.agent.chat.conversation.ConversationStorage;
import org.metaagent.framework.core.agent.chat.conversation.DefaultMessageTurn;
import org.metaagent.framework.core.agent.chat.conversation.DefaultTurnBasedConversation;
import org.metaagent.framework.core.agent.chat.conversation.MessageTurn;
import org.metaagent.framework.core.agent.chat.conversation.TurnBasedConversation;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.MessageId;
import org.metaagent.framework.core.agent.chat.message.RoleMessage;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.metaagent.framework.core.agent.output.AgentStreamOutput;
import org.metaagent.framework.core.agent.profile.AgentProfile;
import org.metaagent.framework.core.agents.chat.ChatAgent;
import org.metaagent.framework.core.agents.chat.input.ChatInput;
import org.metaagent.framework.core.agents.chat.input.DefaultChatInput;
import org.metaagent.framework.core.agents.chat.output.ChatOutput;
import org.metaagent.framework.core.agents.chat.output.ChatStreamOutput;
import org.metaagent.framework.core.agents.chat.output.DefaultChatOutput;
import org.metaagent.framework.core.agents.chat.output.DefaultChatStreamOutput;
import org.metaagent.framework.core.model.chat.ChatModelClient;
import org.metaagent.framework.core.model.chat.ChatModelProvider;
import org.metaagent.framework.core.model.chat.ChatModelResponse;
import org.metaagent.framework.core.model.chat.ChatModelUtils;
import org.metaagent.framework.core.model.chat.compression.ChatCompressionModel;
import org.metaagent.framework.core.model.chat.compression.CompressOptions;
import org.metaagent.framework.core.model.chat.compression.CompressionModel;
import org.metaagent.framework.core.model.chat.compression.CompressionRequest;
import org.metaagent.framework.core.model.chat.compression.CompressionResponse;
import org.metaagent.framework.core.model.chat.compression.CompressionResult;
import org.metaagent.framework.core.model.chat.compression.DefaultCompressOptions;
import org.metaagent.framework.core.model.chat.message.MessageConverter;
import org.metaagent.framework.core.model.chat.metadata.ChatModelMetadata;
import org.metaagent.framework.core.model.prompt.PromptTemplate;
import org.metaagent.framework.core.model.prompt.PromptValue;
import org.metaagent.framework.core.model.prompt.StringPromptTemplate;
import org.metaagent.framework.core.model.prompt.StringPromptValue;
import org.metaagent.framework.core.model.prompt.registry.PromptRegistry;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.model.ChatModel;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * {@link ChatAgent} implementation with Large Language Model (LLM).
 *
 * @author vyckey
 */
public class LlmChatAgent extends AbstractStreamAgent<ChatInput, ChatOutput, ChatStreamOutput>
        implements ChatAgent<ChatInput, ChatOutput, ChatStreamOutput> {
    public static final String DEFAULT_SYSTEM_PROMPT_ID = "framework:chat_agent_system_prompt";
    public static final String DEFAULT_CONTEXT_COMPRESSION_SYSTEM_PROMPT_ID = "framework:chat_agent_context_compression_system_prompt";
    public static final float DEFAULT_CONTEXT_COMPRESSION_RATIO = 0.8f;
    public static final int DEFAULT_COMPRESSION_RESERVED_MESSAGES_COUNT = 5;

    protected ChatModelProvider modelProvider;
    protected ChatModelClient chatModelClient;
    protected PromptTemplate systemPromptTemplate;
    protected MessageConverter messageConverter;
    protected CompressionModel compressionModel;

    protected TurnBasedConversation conversation;
    protected ConversationStorage conversationStorage;
    protected ConversationStorage compressedConversationStorage;

    public LlmChatAgent(String name, String description, ChatModelProvider modelProvider) {
        this(name, description, modelProvider, null);
    }

    public LlmChatAgent(String name, String description, ChatModelProvider modelProvider, PromptTemplate systemPromptTemplate) {
        super(AgentProfile.create(name, description));
        this.modelProvider = Objects.requireNonNull(modelProvider, "modelProvider is required");
        this.chatModelClient = new ChatModelClient(modelProvider.getModel());
        this.systemPromptTemplate = systemPromptTemplate != null ?
                systemPromptTemplate : PromptRegistry.global().getPromptTemplate(DEFAULT_SYSTEM_PROMPT_ID);
        this.messageConverter = new MessageConverter(true);
        this.compressionModel = defaultCompressionModel(modelProvider.getModel());
        this.conversation = new DefaultTurnBasedConversation();
        this.conversationStorage = ConversationInMemoryStorage.INSTANCE;
        this.compressedConversationStorage = ConversationInMemoryStorage.INSTANCE;
    }

    public LlmChatAgent(Builder builder) {
        super(builder);
        this.modelProvider = Objects.requireNonNull(builder.modelProvider, "modelProvider is required");
        this.chatModelClient = new ChatModelClient(modelProvider.getModel());
        this.systemPromptTemplate = builder.systemPromptTemplate != null ?
                builder.systemPromptTemplate : PromptRegistry.global().getPromptTemplate(DEFAULT_SYSTEM_PROMPT_ID);
        this.messageConverter = new MessageConverter(true);
        this.compressionModel = builder.compressionModel != null
                ? builder.compressionModel : defaultCompressionModel(modelProvider.getModel());
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
        return new ChatCompressionModel(chatModel, new SystemMessage(prompt.toString()));
    }

    @Override
    public TurnBasedConversation getConversation() {
        return conversation;
    }

    @Override
    public void initialize() {
        super.initialize();

        conversationStorage.load(conversation);
    }

    @Override
    public AgentOutput<ChatOutput> run(Message... messages) {
        return run(DefaultChatInput.builder().messages(messages).build());
    }

    @Override
    public AgentStreamOutput<ChatOutput, ChatStreamOutput> runStream(Message... messages) {
        return runStream(DefaultChatInput.builder().messages(messages).build());
    }

    @Override
    protected void beforeRun(AgentInput<ChatInput> agentInput) {
        // set system prompt
        ChatModelMetadata modelMetadata = modelProvider.getModelMetadata();
        String modelCutoffDate = "Unknown";
        if (modelMetadata.getCutOffDate() != null) {
            modelCutoffDate = LocalDate.ofInstant(modelMetadata.getCutOffDate(), ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE);
        }

        PromptValue systemPrompt = systemPromptTemplate.format(Map.of(
                "name", getName(),
                "current_date", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                "model_cutoff_date", modelCutoffDate
        ));
        chatModelClient.setSystemMessage(new SystemMessage(systemPrompt.toString()));
        chatModelClient.setToolContext(buildToolExecutorContext(agentInput), agentInput.context().getToolExecutor());

        conversation.newTurn();
        agentInput.input().messages().forEach(conversation::appendMessage);
    }

    protected void compressContextIfNeeded(AgentInput<ChatInput> input) {
        List<org.springframework.ai.chat.messages.Message> historyMessages =
                Lists.newArrayList(conversation).stream().map(messageConverter::convertTo).toList();

        MetadataProvider agentMetadata = getAgentProfile().getMetadata();
        float contextCompressionRatio = agentMetadata.getProperty(ChatAgent.METADATA_KEY_CONTEXT_COMPRESSION_RATIO,
                Float.class, DEFAULT_CONTEXT_COMPRESSION_RATIO);
        int maxMessagesCount = agentMetadata.getProperty(ChatAgent.METADATA_KEY_COMPRESSION_RESERVED_MESSAGES_COUNT,
                Integer.class, DEFAULT_COMPRESSION_RESERVED_MESSAGES_COUNT);

        int maxTokens = (int) (modelProvider.getModelMetadata().getMaxWindowSize() * contextCompressionRatio);
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
        RoleMessage summaryMessage = new RoleMessage("system", finalSummaryText, MetadataProvider.empty());

        List<Message> retainedMessages = compressionResult.getRetainedMessages().stream().map(messageConverter::convertTo).toList();
        List<MessageTurn> turnsToReserve = Lists.newArrayList();
        if (!retainedMessages.isEmpty()) {
            // Remove turns before the first reserved message
            MessageId firstReservedMessageId = retainedMessages.get(0).getId();
            for (MessageTurn turn : conversation.turns(true)) {
                List<Message> turnMessages = turn.messages();
                int matchIndex = IntStream.range(0, turnMessages.size())
                        .filter(idx -> turnMessages.get(idx).getId().equals(firstReservedMessageId))
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

        // Store the compressed conversation
        compressedConversationStorage.store(conversation);

        // Clear and reset the conversation
        conversation.clear();
        turnsToReserve.forEach(conversation::appendTurn);

        // Reset the history messages of model client
        List<org.springframework.ai.chat.messages.Message> historyMessages = Lists.newArrayList();
        historyMessages.add(messageConverter.convertTo(summaryMessage));
        historyMessages.addAll(compressionResult.getRetainedMessages());
        chatModelClient.setHistoryMessages(historyMessages);
    }

    @Override
    protected AgentOutput<ChatOutput> doStep(AgentInput<ChatInput> agentInput) {
        ChatInput input = agentInput.input();
        ChatModelResponse chatResponse = chatModelClient.sendMessages(input.messages().stream().map(messageConverter::convert).toList());

        List<Message> outputMessages = chatModelClient.lastTurnOutputMessages().stream().map(messageConverter::convertTo).toList();
        outputMessages.forEach(conversation::appendMessage);

        DefaultChatOutput agentOutput = DefaultChatOutput.builder().messages(outputMessages).build();
        return AgentOutput.create(agentOutput, ChatModelUtils.getMetadata(chatResponse));
    }

    @Override
    protected AgentStreamOutput<ChatOutput, ChatStreamOutput> stepStream(AgentInput<ChatInput> input, Consumer<AgentOutput<ChatOutput>> onStreamComplete) {
        compressContextIfNeeded(input);

        return super.stepStream(input, agentOutput -> {
            ChatOutput chatOutput = agentOutput.result();
            chatOutput.messages().forEach(conversation::appendMessage);
            onStreamComplete.accept(agentOutput);
        });
    }

    @Override
    protected AgentStreamOutput<ChatOutput, ChatStreamOutput> doStepStream(AgentInput<ChatInput> agentInput) {
        ChatInput input = agentInput.input();
        Flux<ChatModelResponse> messageFlux = chatModelClient.sendMessageStream(input.messages().stream().map(messageConverter::convert).toList());
        Flux<ChatStreamOutput> stream = messageFlux.map(chatModelResponse -> {
            Message message = messageConverter.convertTo(chatModelResponse.getOutput());
            return new DefaultChatStreamOutput(message, MetadataProvider.empty());
        });
        return AgentStreamOutput.create(stream);
    }

    @Override
    public AgentStreamOutput.Aggregator<ChatStreamOutput, ChatOutput> getStreamOutputAggregator() {
        return DefaultChatStreamOutput.aggregator();
    }

    @Override
    public void reset() {
        super.reset();
        chatModelClient.reset();
        getConversation().clear();
    }

    @Override
    public void close() {
        this.conversationStorage.store(conversation);
        try {
            this.conversationStorage.close();
            this.compressedConversationStorage.close();
        } catch (IOException e) {
            logger.error("Failed to close conversation storage", e);
        }
    }


    public static class Builder extends AbstractAgentBuilder<LlmChatAgent, Builder, ChatInput, ChatOutput> {
        private ChatModelProvider modelProvider;
        private PromptTemplate systemPromptTemplate;
        private CompressionModel compressionModel;
        private TurnBasedConversation conversation;
        protected ConversationStorage conversationStorage;
        protected ConversationStorage compressedConversationStorage;

        @Override
        protected Builder self() {
            return this;
        }

        public Builder modelProvider(ChatModelProvider modelProvider) {
            this.modelProvider = modelProvider;
            return self();
        }

        public Builder systemPromptTemplate(PromptTemplate systemPromptTemplate) {
            this.systemPromptTemplate = systemPromptTemplate;
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
