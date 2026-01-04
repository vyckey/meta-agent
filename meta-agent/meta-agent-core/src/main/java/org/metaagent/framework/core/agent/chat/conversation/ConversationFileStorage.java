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

package org.metaagent.framework.core.agent.chat.conversation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.MessageSerializer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * File-based implementation of ConversationStorage.
 *
 * @author vyckey
 */
public class ConversationFileStorage implements ConversationStorage {
    private final String filePathPattern;

    public ConversationFileStorage(String filePathPattern) {
        this.filePathPattern = Objects.requireNonNull(filePathPattern).trim();
    }

    public String getFilePath(String historyId) {
        return String.format(filePathPattern, historyId);
    }

    @Override
    public void store(Conversation conversation) {
        String filePath = getFilePath(conversation.id());
        File file = new File(filePath);
        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                throw new IllegalStateException("Unable to create directory: " + file.getParentFile().getAbsolutePath());
            }
        }
        try {
            String className = conversation.getClass().getTypeName();
            ConversationDO convDo;
            if (conversation instanceof TurnBasedConversation turnBasedConversation) {
                List<MessageTurnDO> messageTurns = Lists.newArrayList();
                for (MessageTurn messageTurn : turnBasedConversation.turns(false)) {
                    messageTurns.add(new MessageTurnDO(messageTurn));
                }
                convDo = new ConversationDO(conversation.id(), null, messageTurns, className);
            } else {
                convDo = new ConversationDO(conversation.id(), Lists.newArrayList(conversation), null, className);
            }
            MessageSerializer.getObjectMapper().writeValue(file, convDo);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store conversation " + conversation.id() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void load(Conversation conversation) {
        String filePath = getFilePath(conversation.id());
        File file = new File(filePath);
        if (file.exists()) {
            try {
                ConversationDO convDo = MessageSerializer.getObjectMapper().readValue(file, ConversationDO.class);
                if (conversation instanceof TurnBasedConversation turnBasedConversation && convDo.messageTurns != null) {
                    convDo.messageTurns.stream()
                            .map(turnDO -> new DefaultMessageTurn(turnDO.messages, turnDO.finished))
                            .forEach(turnBasedConversation::appendTurn);
                } else {
                    conversation.clear();
                    List<Message> messages = convDo.messages;
                    messages.forEach(conversation::appendMessage);
                }
            } catch (IOException e) {
                throw new IllegalStateException("Failed to load conversation file " + file.getAbsolutePath() + ": " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void clear(String conversationId) {
        String filePath = getFilePath(conversationId);
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }

    private record MessageTurnDO(List<Message> messages, boolean finished) {
        private MessageTurnDO(MessageTurn messageTurn) {
            this(messageTurn.messages(), messageTurn.isFinished());
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record ConversationDO(String convId, List<Message> messages, List<MessageTurnDO> messageTurns,
                                  @JsonProperty("@class") String className) {
    }
}
