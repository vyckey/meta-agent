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

import org.apache.commons.collections.MapUtils;
import org.metaagent.framework.common.util.PageResult;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.MessageId;
import org.metaagent.framework.core.agent.chat.message.MessageInfo;
import org.metaagent.framework.core.agent.chat.session.SessionId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of {@link ConversationStore}.
 * <p>
 * This implementation stores all data in concurrent maps and is suitable for
 * testing, demonstration, or lightweight scenarios where persistence is not required.
 * It is thread-safe and supports all tree-aware operations.
 * </p>
 *
 * @author vyckey
 */
public class InMemoryConversationStore implements ConversationStore {
    private static final InMemoryConversationStore GLOBAL = new InMemoryConversationStore();

    private final Map<SessionId, ConversationData> conversations = new ConcurrentHashMap<>();

    /**
     * Gets the global instance of the in-memory conversation store.
     */
    public static InMemoryConversationStore global() {
        return GLOBAL;
    }

    /**
     * Internal data structure for a conversation.
     */
    private static class ConversationData {
        private final SessionId sessionId;
        private final List<Message> messages = new ArrayList<>();  // Maintains insertion order
        private final Map<MessageId, Message> messageMap = new HashMap<>();
        private final Map<MessageId, Set<MessageId>> parentToChildren = new HashMap<>();
        private final Object lock = new Object();  // Per-session lock for consistency

        ConversationData(SessionId sessionId) {
            this.sessionId = sessionId;
        }

        /**
         * Gets a message by ID.
         */
        Optional<Message> getMessage(MessageId messageId) {
            return Optional.ofNullable(messageMap.get(messageId));
        }

        int findMessageIndex(MessageId messageId) {
            for (int i = 0; i < messages.size(); i++) {
                if (messages.get(i).info().id().equals(messageId)) {
                    return i;
                }
            }
            return -1;
        }

        /**
         * Adds a message to the session.
         */
        void addMessage(Message message) {
            messageMap.put(message.info().id(), message);
            messages.add(message);

            MessageId parentId = message.info().parentId();
            parentToChildren.computeIfAbsent(parentId, k -> ConcurrentHashMap.newKeySet())
                    .add(message.info().id());
        }

        /**
         * Updates an existing message.
         */
        void updateMessage(Message message) {
            messageMap.put(message.info().id(), message);
            // Note: parentId change is not supported as per interface contract
        }

        /**
         * Checks if a message exists.
         */
        boolean containsMessage(MessageId messageId) {
            return messageMap.containsKey(messageId);
        }

        /**
         * Gets all messages in insertion order.
         */
        List<Message> getAllMessages() {
            return Collections.unmodifiableList(messages);
        }

        /**
         * Deletes a subtree rooted at the given message.
         */
        Set<MessageId> deleteSubtree(MessageId rootMessageId, boolean inclusive) {
            Set<MessageId> toDelete = new HashSet<>();
            collectSubtreeIds(rootMessageId, inclusive, toDelete);

            if (!toDelete.isEmpty()) {
                // Remove from parent-to-children mapping
                for (MessageId id : toDelete) {
                    Message msg = messageMap.get(id);
                    if (msg != null && msg.info().parentId() != null) {
                        Set<MessageId> siblings = parentToChildren.get(msg.info().parentId());
                        if (siblings != null) {
                            siblings.remove(id);
                        }
                    }
                    parentToChildren.remove(id);
                }

                // Remove from messageMap and messages list
                messageMap.keySet().removeAll(toDelete);
                messages.removeIf(msg -> toDelete.contains(msg.info().id()));
            }

            return toDelete;
        }

        private void collectSubtreeIds(MessageId messageId, boolean includeCurrent, Set<MessageId> result) {
            if (includeCurrent) {
                result.add(messageId);
            }

            Set<MessageId> children = parentToChildren.getOrDefault(messageId, Collections.emptySet());
            for (MessageId childId : children) {
                collectSubtreeIds(childId, true, result);
            }
        }

        /**
         * Clears all messages.
         */
        void clear() {
            messages.clear();
            messageMap.clear();
            parentToChildren.clear();
        }
    }

    // ==================== Helper Methods ====================

    private ConversationData getOrCreateConversation(SessionId sessionId) {
        return conversations.computeIfAbsent(sessionId, ConversationData::new);
    }

    private ConversationData getConversationIfExists(SessionId sessionId) {
        return conversations.get(sessionId);
    }

    // ==================== Interface Implementations ====================

    @Override
    public void appendMessage(SessionId sessionId, Message message) {
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        Objects.requireNonNull(message, "message must not be null");

        ConversationData conversation = getOrCreateConversation(sessionId);
        synchronized (conversation.lock) {
            // Validate parent exists if specified
            MessageId parentId = message.info().parentId();
            if (parentId != null && !conversation.containsMessage(parentId)) {
                throw new IllegalArgumentException("Parent message not found: " + parentId);
            }

            // Check for duplicate ID
            if (conversation.containsMessage(message.info().id())) {
                throw new IllegalArgumentException("Message already exists: " + message.info().id());
            }

            conversation.addMessage(message);
        }
    }

    @Override
    public void appendMessages(SessionId sessionId, List<Message> messages) {
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        Objects.requireNonNull(messages, "messages must not be null");

        if (messages.isEmpty()) {
            return;
        }

        ConversationData session = getOrCreateConversation(sessionId);
        synchronized (session.lock) {
            // Validate all messages before adding
            Set<MessageId> existingIds = new HashSet<>(session.messageMap.keySet());

            for (Message msg : messages) {
                MessageId parentId = msg.info().parentId();
                if (parentId != null && !existingIds.contains(parentId)) {
                    throw new IllegalArgumentException("Parent message not found: " + parentId);
                }
                if (existingIds.contains(msg.info().id())) {
                    throw new IllegalArgumentException("Duplicate message ID: " + msg.info().id());
                }
                existingIds.add(msg.info().id());
            }

            // Add all messages
            for (Message msg : messages) {
                session.addMessage(msg);
            }
        }
    }

    @Override
    public void updateMessage(SessionId sessionId, Message message) {
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        Objects.requireNonNull(message, "message must not be null");

        ConversationData conversation = getConversationIfExists(sessionId);
        if (conversation == null) {
            throw new IllegalArgumentException("Conversation not found: " + sessionId);
        }

        synchronized (conversation.lock) {
            if (!conversation.containsMessage(message.info().id())) {
                throw new IllegalArgumentException("Message not found: " + message.info().id());
            }
            conversation.updateMessage(message);
        }
    }

    @Override
    public Optional<Message> getMessage(SessionId sessionId, MessageId messageId) {
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        Objects.requireNonNull(messageId, "messageId must not be null");

        ConversationData conversation = getConversationIfExists(sessionId);
        if (conversation == null) {
            return Optional.empty();
        }

        synchronized (conversation.lock) {
            return conversation.getMessage(messageId);
        }
    }

    @Override
    public Optional<Message> getLastMessage(SessionId sessionId) {
        Objects.requireNonNull(sessionId, "sessionId must not be null");

        ConversationData conversation = getConversationIfExists(sessionId);
        if (conversation == null) {
            return Optional.empty();
        }

        synchronized (conversation.lock) {
            List<Message> messages = conversation.getAllMessages();
            if (messages.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(messages.get(messages.size() - 1));
        }
    }

    @Override
    public List<MessageId> getLeafMessageIds(SessionId sessionId) {
        Objects.requireNonNull(sessionId, "sessionId must not be null");

        ConversationData conversation = getConversationIfExists(sessionId);
        if (conversation == null || MapUtils.isEmpty(conversation.messageMap)) {
            return Collections.emptyList();
        }

        synchronized (conversation.lock) {
            Set<MessageId> allIds = conversation.messageMap.keySet();
            Set<MessageId> parentIds = conversation.messageMap.values().stream()
                    .map(Message::info).map(MessageInfo::parentId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            return allIds.stream()
                    .filter(id -> !parentIds.contains(id))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public PageResult<Message> loadMessagesBefore(SessionId sessionId, MessageId messageId, int limit, String cursor) {
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be positive");
        }

        ConversationData conversation = getConversationIfExists(sessionId);
        if (conversation == null) {
            return PageResult.empty();
        }

        synchronized (conversation.lock) {
            List<Message> messages = conversation.getAllMessages();
            if (messages.isEmpty()) {
                return PageResult.empty();
            }

            int endIdx; // exclusive end index

            if (cursor != null) {
                endIdx = Integer.parseInt(cursor);
            } else if (messageId == null) {
                endIdx = messages.size();
            } else {
                endIdx = conversation.findMessageIndex(messageId); // messages BEFORE ref, exclusive
            }

            int startIdx = Math.max(0, endIdx - limit);
            List<Message> items = new ArrayList<>(messages.subList(startIdx, endIdx));
            String nextCursor = startIdx > 0 ? String.valueOf(startIdx) : null;
            boolean hasMore = startIdx > 0;
            return new PageResult<>(items, nextCursor, hasMore);
        }
    }

    @Override
    public PageResult<Message> loadMessagesAfter(SessionId sessionId, MessageId messageId, int limit, String cursor) {
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be positive");
        }

        ConversationData conversation = getConversationIfExists(sessionId);
        if (conversation == null) {
            return PageResult.empty();
        }

        synchronized (conversation.lock) {
            List<Message> messages = conversation.getAllMessages();
            if (messages.isEmpty()) {
                return PageResult.empty();
            }

            int startIdx; // inclusive start index

            if (cursor != null) {
                startIdx = Integer.parseInt(cursor);
            } else if (messageId == null) {
                startIdx = 0;
            } else {
                startIdx = conversation.findMessageIndex(messageId) + 1; // messages AFTER ref, exclusive
            }

            int endIdx = Math.min(startIdx + limit, messages.size());
            List<Message> items = new ArrayList<>(messages.subList(startIdx, endIdx));

            String nextCursor = endIdx < messages.size() ? String.valueOf(endIdx) : null;
            boolean hasMore = endIdx < messages.size();
            return new PageResult<>(items, nextCursor, hasMore);
        }
    }

    @Override
    public Set<MessageId> deleteMessages(SessionId sessionId, MessageId rootMessageId, boolean inclusive) {
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        Objects.requireNonNull(rootMessageId, "rootMessageId must not be null");

        ConversationData conversation = getConversationIfExists(sessionId);
        if (conversation == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }

        synchronized (conversation.lock) {
            if (!conversation.containsMessage(rootMessageId)) {
                throw new IllegalArgumentException("Root message not found: " + rootMessageId);
            }

            return conversation.deleteSubtree(rootMessageId, inclusive);
        }
    }

    @Override
    public void deleteMessages(SessionId sessionId) {
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        conversations.remove(sessionId);
    }

    // ==================== Additional Utility Methods ====================

    /**
     * Checks if a session exists.
     *
     * @param sessionId the session identifier
     * @return true if the session exists
     */
    public boolean hasSession(SessionId sessionId) {
        return conversations.containsKey(sessionId);
    }

    /**
     * Gets the number of messages in a session.
     *
     * @param sessionId the session identifier
     * @return the number of messages, or 0 if session doesn't exist
     */
    public int getMessageCount(SessionId sessionId) {
        ConversationData conversation = conversations.get(sessionId);
        if (conversation == null) {
            return 0;
        }
        synchronized (conversation.lock) {
            return conversation.messageMap.size();
        }
    }

    /**
     * Clears all data from the store.
     */
    public void clear() {
        conversations.clear();
    }
}