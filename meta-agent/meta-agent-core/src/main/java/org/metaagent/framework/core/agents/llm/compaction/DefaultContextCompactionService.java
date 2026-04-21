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

package org.metaagent.framework.core.agents.llm.compaction;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.common.metadata.MetadataProvider;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.RoleMessage;
import org.metaagent.framework.core.agent.chat.message.RoleMessageInfo;
import org.metaagent.framework.core.agent.chat.message.part.MessagePart;
import org.metaagent.framework.core.agent.chat.message.part.MessagePartId;
import org.metaagent.framework.core.agent.chat.message.part.TextMessagePart;
import org.metaagent.framework.core.agents.llm.LlmAgent;
import org.metaagent.framework.core.agents.llm.context.LlmAgentContext;
import org.metaagent.framework.core.agents.llm.context.LlmContextCache;
import org.metaagent.framework.core.agents.llm.input.LlmAgentInput;
import org.metaagent.framework.core.agents.llm.message.part.ToolCallMessagePart;
import org.metaagent.framework.core.agents.llm.output.LlmAgentOutput;
import org.metaagent.framework.core.model.ModelId;
import org.metaagent.framework.core.model.prompt.PromptValue;
import org.metaagent.framework.core.model.prompt.StringPromptValue;
import org.metaagent.framework.core.model.prompt.registry.PromptRegistry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.metaagent.framework.core.agent.chat.message.MessageMetadataKeys.KEY_SYNTHETIC;

/**
 * DefaultContextCompactionService is the default implementation of {@link ContextCompactionService}.
 *
 * @author vyckey
 * @see ContextCompactionService
 */
public class DefaultContextCompactionService implements ContextCompactionService {
    public static final String SYSTEM_PROMPT_ID = "context_compaction_prompt";
    private static final int DEFAULT_CHAR_KEEP_WHEN_PRUNE = 50;
    private final LlmAgent llmAgent;
    private final PromptValue compactPrompt;
    private final PruningConfig pruningConfig;

    static {
        PromptRegistry.global().registerPrompt(
                SYSTEM_PROMPT_ID, StringPromptValue.fromFile("agents/prompts/context_compaction_prompt.md")
        );
    }

    public DefaultContextCompactionService(LlmAgent llmAgent, PromptValue compactPrompt, PruningConfig pruningConfig) {
        this.llmAgent = Objects.requireNonNull(llmAgent, "llmAgent cannot be null");
        this.compactPrompt = Objects.requireNonNull(compactPrompt, "compactPrompt cannot be null");
        this.pruningConfig = Objects.requireNonNull(pruningConfig, "pruningConfig cannot be null");
    }

    public DefaultContextCompactionService(LlmAgent llmAgent, PruningConfig pruningConfig) {
        this(llmAgent, PromptRegistry.global().getPrompt(SYSTEM_PROMPT_ID), pruningConfig);
    }

    public static DefaultContextCompactionService create(PruningConfig pruningConfig) {
        return new DefaultContextCompactionService(new LlmAgent("CompactionAgent"), pruningConfig);
    }

    @Override
    public PruningResult prune(PruningInput input) {
        List<Message> messages = input.messages();
        PruningOptions options = input.options();

        List<MessagePartId> deletedIds = new ArrayList<>();
        List<MessagePart> modifiedParts = new ArrayList<>();

        List<ToolCallMessagePart> toolCallMessages = filterToolCallMessages(messages);
        for (ToolCallMessagePart toolCallMessage : toolCallMessages) {
            String toolName = toolCallMessage.toolName();
            if (StringUtils.isEmpty(toolCallMessage.response())) {
                continue;
            }
            if (pruningConfig.toolsToKeepOutput().contains(toolName)) {
                continue;
            }

            boolean needsTruncate = false;
            String truncatedResponse = null;
            if (pruningConfig.toolsToDropOutput().contains(toolName)) {
                needsTruncate = true;
                truncatedResponse = "[truncated]";
            } else if (estimateCharsLength(toolCallMessage) > options.charThresholdToPrune()) {
                needsTruncate = true;
                final int length = toolCallMessage.response().length();
                final int keepLength = Math.min(length, Optional.ofNullable(options.charKeepWhenPrune()).orElse(DEFAULT_CHAR_KEEP_WHEN_PRUNE));

                truncatedResponse = toolCallMessage.response().substring(0, keepLength) +
                        "[truncated " + (length - keepLength) + " chars]";
            }

            if (needsTruncate) {
                Path cacheFilePath;
                if (options.prunedOutputPath() != null &&
                        (cacheFilePath = storeToFile(options.prunedOutputPath(), toolCallMessage)) != null) {
                    truncatedResponse += " [stored to " + cacheFilePath + "]";
                }
                modifiedParts.add(toolCallMessage.toBuilder()
                        .response(truncatedResponse)
                        .build()
                );
            }
        }

        boolean pruned = CollectionUtils.isNotEmpty(deletedIds) || CollectionUtils.isNotEmpty(modifiedParts);
        return PruningResult.builder()
                .pruned(pruned)
                .deletedMessagePartIds(deletedIds)
                .modifiedMessageParts(modifiedParts)
                .build();
    }

    private List<ToolCallMessagePart> filterToolCallMessages(List<Message> messages) {
        List<ToolCallMessagePart> toolCallMessages = new ArrayList<>();
        for (Message message : messages) {
            for (MessagePart part : message.parts()) {
                if (part instanceof ToolCallMessagePart toolCallMessage) {
                    toolCallMessages.add(toolCallMessage);
                }
            }
        }
        return toolCallMessages;
    }

    private int estimateCharsLength(ToolCallMessagePart message) {
        int charsLength = message.toolName().length() + message.callId().length();
        if (message.arguments() != null) {
            charsLength += message.arguments().length();
        }
        if (message.response() != null) {
            charsLength += message.response().length();
        }
        return charsLength;
    }

    private Path storeToFile(Path storagePath, ToolCallMessagePart messagePart) {
        try {
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
            }

            String fileName = "tool_output_" + messagePart.callId() + ".txt";
            Path filePath = storagePath.resolve(fileName);

            StringBuilder sb = new StringBuilder()
                    .append("- Tool: ").append(messagePart.toolName())
                    .append("\n- Call ID: ").append(messagePart.callId())
                    .append("\n- Created: ").append(messagePart.createdAt())
                    .append("\n- Status: ").append(messagePart.status())
                    .append("\n- Arguments: ").append(messagePart.arguments())
                    .append("\n=== OUTPUT ===\n\n")
                    .append(messagePart.response());

            Files.writeString(filePath, sb.toString());
            return filePath;
        } catch (IOException e) {
            // ignore exception
            return null;
        }
    }

    @Override
    public CompactionResult compact(CompactionInput input) {
        CompactionOptions options = input.options();
        int reservedMessagesToKeep = Math.min(options.reservedMessagesToKeep(), input.messages().size());
        int messagesToCompactCount = input.messages().size() - reservedMessagesToKeep;

        List<Message> messagesToCompact = Lists.newArrayList(input.messages().subList(0, messagesToCompactCount));
        List<Message> reservedMessages = input.messages().subList(messagesToCompactCount, input.messages().size());

        return doCompaction(input, messagesToCompact, reservedMessages);
    }

    private CompactionResult doCompaction(CompactionInput input, List<Message> messagesToCompact, List<Message> reservedMessages) {
        CompactionOptions options = input.options();
        LlmContextCache contextCache = input.contextCache();

        ModelId compactModelId = options.modelId();
        List<PromptValue> systemPrompts;
        if (contextCache != null) {
            compactModelId = compactModelId == null || compactModelId.equals(contextCache.modelId())
                    ? contextCache.modelId() : compactModelId;
            systemPrompts = contextCache.systemPrompts();
            if (CollectionUtils.isNotEmpty(contextCache.prependMessages())) {
                messagesToCompact.addAll(0, contextCache.prependMessages());
            }
        } else {
            systemPrompts = List.of();
        }

        messagesToCompact.add(buildCompactionMessage(options));

        LlmAgentInput agentInput = LlmAgentInput.builder()
                .modelId(compactModelId)
                .systemPrompts(systemPrompts)
                .messages(messagesToCompact)
                .maxSteps(1)
                .context(LlmAgentContext.builder()
                        .modelProviderRegistry(input.modelProviderRegistry())
                        .toolManager(contextCache != null ? contextCache.toolManager() : null)
                        .toolExecutorContext(contextCache != null ? contextCache.toolExecutorContext() : null)
                        .contextCompactionService(NoopContextCompactionService.INSTANCE)
                        .abortSignal(input.abortSignal())
                        .build())
                .build();

        LlmAgentOutput agentOutput = llmAgent.run(agentInput);
        List<MessagePart> summaryParts = agentOutput.message().parts();

        return CompactionResult.builder()
                .compacted(true)
                .compactModelId(compactModelId)
                .reservedMessages(reservedMessages)
                .summaryMessages(summaryParts)
                .compactionUsage(agentOutput.tokenUsage())
                .build();
    }

    private Message buildCompactionMessage(CompactionOptions options) {
        String compactionPrompt = compactPrompt.text();
        if (options.appendedPrompt() != null) {
            compactionPrompt += "\n\n" + options.appendedPrompt();
        }

        TextMessagePart compactionMessagePart = TextMessagePart.builder()
                .text(compactionPrompt)
                .metadata(MetadataProvider.builder()
                        .setProperty(KEY_SYNTHETIC, true)
                        .build())
                .build();
        return RoleMessage.builder()
                .info(RoleMessageInfo.user()
                        .metadata(MetadataProvider.builder()
                                .setProperty(KEY_SYNTHETIC, true)
                                .build())
                        .build())
                .addPart(compactionMessagePart)
                .build();
    }

    public record PruningConfig(
            Set<String> toolsToDropOutput,
            Set<String> toolsToKeepOutput
    ) {
        public PruningConfig {
            toolsToDropOutput = toolsToDropOutput != null ? toolsToDropOutput : Set.of();
            toolsToKeepOutput = toolsToKeepOutput != null ? toolsToKeepOutput : Set.of();
        }
    }
}