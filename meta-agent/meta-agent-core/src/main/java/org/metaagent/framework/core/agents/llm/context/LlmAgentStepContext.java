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

package org.metaagent.framework.core.agents.llm.context;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.MessageInfo;
import org.metaagent.framework.core.agent.chat.message.RoleMessage;
import org.metaagent.framework.core.agent.chat.message.part.MessagePart;
import org.metaagent.framework.core.agent.context.AgentStepContext;
import org.metaagent.framework.core.agents.llm.LlmStreamingAgent;
import org.metaagent.framework.core.agents.llm.message.SystemMessagePart;
import org.metaagent.framework.core.agents.llm.message.ToolCallMessagePart;
import org.metaagent.framework.core.model.chat.metadata.TokenUsage;
import org.springframework.ai.chat.metadata.Usage;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Step context for {@link LlmStreamingAgent}, which contains the context for a single step of the agent.
 *
 * @author vyckey
 */
public class LlmAgentStepContext implements AgentStepContext {
    private static final Set<String> FINISH_REASONS = Set.of("STOP", "LENGTH", "CONTENT_FILTER", "RETURN_DIRECT");
    private final AtomicInteger loopCounter;
    private final List<SystemMessagePart> systemMessages;
    private final List<Message> historyMessages;
    private final Map<String, ToolCallMessagePart> toolCallMessages;
    private MessageInfo outputMessageInfo;
    private final List<MessagePart> outputMessageParts;
    private final Map<Message, List<org.springframework.ai.chat.messages.Message>> mappingMessageCache;
    private String finishReason;
    private TokenUsage tokenUsage;

    public LlmAgentStepContext(List<SystemMessagePart> systemMessages, List<Message> historyMessages) {
        this.loopCounter = new AtomicInteger(0);
        this.systemMessages = Objects.requireNonNull(systemMessages, "systemMessages cannot be null");
        this.historyMessages = Objects.requireNonNull(historyMessages, "historyMessages cannot be null");
        this.toolCallMessages = Maps.newHashMap();
        this.outputMessageParts = Lists.newArrayList();
        this.mappingMessageCache = Maps.newHashMap();
        this.tokenUsage = TokenUsage.empty();
    }

    @Override
    public AtomicInteger getLoopCounter() {
        return loopCounter;
    }

    public MessageInfo getOutputMessageInfo() {
        return outputMessageInfo;
    }

    public void setOutputMessageInfo(MessageInfo outputMessageInfo) {
        this.outputMessageInfo = outputMessageInfo;
    }

    public Message getOutputMessage() {
        MessageInfo messageInfo = outputMessageInfo;
        if (!outputMessageParts.isEmpty()) {
            messageInfo = messageInfo.toBuilder()
                    .updatedAt(outputMessageParts.get(outputMessageParts.size() - 1).updatedAt())
                    .build();
        }
        return RoleMessage.builder().info(messageInfo).parts(outputMessageParts).build();
    }

    public List<SystemMessagePart> getSystemMessages() {
        return systemMessages;
    }

    public List<Message> getHistoryMessages() {
        return historyMessages;
    }

    public Message getLastUserMessage() {
        for (int i = historyMessages.size() - 1; i >= 0; i--) {
            Message message = historyMessages.get(i);
            if (MessageInfo.ROLE_USER.equals(message.info().role())) {
                return message;
            }
        }
        throw new IllegalStateException("No user message found. This should never happen");
    }

    public List<Message> getAllMessages() {
        List<Message> allMessages = Lists.newArrayList(historyMessages);
        allMessages.add(getOutputMessage());
        return allMessages;
    }

    public void addOutputMessagePart(MessagePart messagePart) {
        if (messagePart instanceof ToolCallMessagePart toolCallMessagePart) {
            // insert or update tool call message
            ToolCallMessagePart foundToolCallMessagePart = toolCallMessages.get(toolCallMessagePart.callId());
            int index;
            if (foundToolCallMessagePart != null && (index = this.outputMessageParts.indexOf(foundToolCallMessagePart)) >= 0) {
                this.outputMessageParts.set(index, messagePart);
            } else {
                this.outputMessageParts.add(messagePart);
            }

            this.toolCallMessages.put(toolCallMessagePart.callId(), toolCallMessagePart);
        } else {
            this.outputMessageParts.add(messagePart);
        }
    }

    public void addOutputMessageParts(List<? extends MessagePart> messageParts) {
        for (MessagePart messagePart : messageParts) {
            addOutputMessagePart(messagePart);
        }
    }

    public ToolCallMessagePart getToolCallMessage(String toolCallId) {
        return toolCallMessages.get(toolCallId);
    }

    public List<org.springframework.ai.chat.messages.Message> getMappingMessages(
            Message message,
            Function<Message, List<org.springframework.ai.chat.messages.Message>> converter) {
        return mappingMessageCache.computeIfAbsent(message, converter);
    }

    public String getFinishReason() {
        return finishReason;
    }

    public void setFinishReason(String finishReason) {
        this.finishReason = finishReason != null ? finishReason.toUpperCase() : "";
    }

    public boolean isFinished() {
        return StringUtils.isNotEmpty(finishReason) && FINISH_REASONS.contains(finishReason);
    }

    public TokenUsage getTokenUsage() {
        return tokenUsage;
    }

    public TokenUsage addTokenUsage(Usage tokenUsage) {
        this.tokenUsage = this.tokenUsage.accumulate(tokenUsage);
        return this.tokenUsage;
    }

    @Override
    public void reset() {
        loopCounter.set(0);
        outputMessageInfo = null;
        outputMessageParts.clear();
        toolCallMessages.clear();
        mappingMessageCache.clear();
        tokenUsage = TokenUsage.empty();
    }
}
