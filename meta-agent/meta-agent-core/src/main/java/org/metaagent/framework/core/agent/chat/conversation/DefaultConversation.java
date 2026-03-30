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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.metaagent.framework.common.util.PageResult;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.MessageId;
import org.metaagent.framework.core.agent.chat.message.MessageInfo;
import org.metaagent.framework.core.agent.chat.session.SessionId;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static org.metaagent.framework.core.agent.chat.message.Message.TIME_FORMATTER;

/**
 * Thread-safe default implementation of {@link Conversation} backed by a {@link ConversationStore}.
 * <p>
 * This implementation uses lazy loading with a message cache. Messages are loaded on demand
 * through iterators or specific queries. The cache holds all loaded messages regardless of branch.
 * </p>
 *
 * @author vyckey
 */
public class DefaultConversation implements Conversation {
    private final SessionId sessionId;
    private final ConversationStore store;
    private final ConversationConfig config;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // Message cache: ID -> Message (holds all loaded messages)
    private final Map<MessageId, Message> messageCache = new HashMap<>();

    // Message buffer: holds messages that are not yet persisted
    private final List<Message> messageBuffer = new ArrayList<>();
    private ScheduledExecutorService flushExecutor;
    private boolean flushing = false;

    // Current branch state - only stores the leaf message, path is loaded lazily
    private Message currentLeaf;
    private boolean currentLeafLoaded = false;

    /**
     * Creates a new DefaultConversation for the given session.
     *
     * @param sessionId the session identifier
     * @param store     the underlying conversation store
     * @param config    the conversation configuration
     */
    public DefaultConversation(SessionId sessionId, ConversationStore store, ConversationConfig config) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId must not be null");
        this.store = Objects.requireNonNull(store, "store must not be null");
        this.config = Objects.requireNonNull(config, "config must not be null");
        if (config.flushIntervalMillis > 0) {
            flushExecutor = Executors.newScheduledThreadPool(1);
            flushExecutor.scheduleWithFixedDelay(
                    this::flushNow,
                    config.flushIntervalMillis, config.flushIntervalMillis, TimeUnit.MILLISECONDS
            );
        }
    }

    public DefaultConversation(SessionId sessionId, ConversationStore store) {
        this(sessionId, store, new ConversationConfig());
    }

    public DefaultConversation(SessionId sessionId) {
        this(sessionId, InMemoryConversationStore.global());
    }

    // ========== Cache Management ==========

    private void setCurrentLeaf(Message currentLeaf) {
        lock.writeLock().lock();
        try {
            this.currentLeaf = currentLeaf;
            this.currentLeafLoaded = true;
            if (currentLeaf != null) {
                this.messageCache.put(currentLeaf.info().id(), currentLeaf);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Gets a message from cache or loads it from store.
     */
    public Optional<Message> getMessage(MessageId messageId) {
        Objects.requireNonNull(messageId, "messageId must not be null");

        lock.readLock().lock();
        try {
            Message cached = messageCache.get(messageId);
            if (cached != null) {
                return Optional.of(cached);
            }
        } finally {
            lock.readLock().unlock();
        }

        // Not in cache, load from store
        lock.writeLock().lock();
        try {
            Message message = messageCache.computeIfAbsent(messageId, id -> store.getMessage(sessionId, id).orElse(null));
            return Optional.ofNullable(message);
        } finally {
            lock.writeLock().unlock();
        }
    }

    // ========== Conversation Interface Implementation ==========

    @Override
    public SessionId sessionId() {
        return sessionId;
    }

    @Override
    public boolean isEmpty() {
        return currentLeafId().isEmpty();
    }

    @Override
    public void appendMessage(Message message) {
        Objects.requireNonNull(message, "message must not be null");

        lock.writeLock().lock();
        try {
            Optional<MessageId> currentLeafId = currentLeafId();
            MessageId parentId = message.info().parentId();
            // Auto-set parent if not specified
            if (currentLeafId.isPresent() && parentId == null) {
                message = message.withParentId(currentLeafId.get());
            }

            // Validate parent matches current leaf
            if (!Objects.equals(parentId, currentLeafId.orElse(null))) {
                throw new IllegalArgumentException("Parent ID must match current branch leaf: expected "
                        + currentLeafId.orElse(null) + ", but got " + parentId);
            }

            // Set message to current leaf
            setCurrentLeaf(message);

            // Add message to buffer and flush if required
            messageBuffer.add(message);
            flushIfRequired();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void updateMessage(Message newMessage) {
        Objects.requireNonNull(newMessage, "newMessage must not be null");

        lock.writeLock().lock();
        try {
            MessageId messageId = newMessage.info().id();
            // Verify message exists in cache or store
            Optional<Message> existing = getMessage(messageId);
            if (existing.isEmpty()) {
                throw new IllegalArgumentException("Message does not exist: " + messageId);
            }

            // Verify it's on current branch (simplified - would need full branch check)
            // For now, we'll skip this check for simplicity

            store.updateMessage(sessionId, newMessage);
            messageCache.put(messageId, newMessage);

            // If this was the current leaf, need to update leaf message
            if (messageId.equals(currentLeafId().orElse(null))) {
                setCurrentLeaf(newMessage);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void deleteMessagesAfter(MessageId messageId, boolean inclusive) {
        Objects.requireNonNull(messageId, "messageId must not be null");

        lock.writeLock().lock();
        try {
            // Check if message is in buffer
            if (deleteMessagesInBufferIfPresent(messageId, inclusive)) {
                return;
            }

            // Check if message in on current branch
            Optional<Message> foundMessage = findMessage(message -> message.info().id().equals(messageId), false);
            if (foundMessage.isEmpty()) {
                if (getMessage(messageId).isEmpty()) {
                    throw new IllegalArgumentException("Message does not exist: " + messageId);
                }
                throw new IllegalArgumentException("Message " + messageId + " is not on current branch");
            }

            // Delete messages from store
            Set<MessageId> deletedMessageIds = store.deleteMessages(sessionId, messageId, inclusive);
            deletedMessageIds.forEach(messageCache::remove);

            // Reset current leaf message
            resetLeafMessage(foundMessage.get(), inclusive);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private boolean deleteMessagesInBufferIfPresent(MessageId messageId, boolean inclusive) {
        int messageIndex = IntStream.range(0, messageBuffer.size())
                .filter(idx -> messageBuffer.get(idx).info().id().equals(messageId))
                .findFirst().orElse(-1);
        if (messageIndex < 0) {
            return false;
        }

        Message message = messageBuffer.remove(messageIndex);
        messageBuffer.subList(inclusive ? messageIndex : messageIndex + 1, messageBuffer.size()).clear();
        resetLeafMessage(message, inclusive);
        return true;
    }

    private void resetLeafMessage(Message message, boolean parentAsLeaf) {
        if (parentAsLeaf) {
            MessageId parentedId = message.info().parentId();
            if (parentedId != null) {
                Optional<Message> parentMessage = getMessage(parentedId);
                if (parentMessage.isEmpty()) {
                    throw new IllegalStateException("Parent message not found: " + parentedId);
                }
                setCurrentLeaf(parentMessage.get());
            } else {
                setCurrentLeaf(null);
            }
        } else {
            setCurrentLeaf(message);
        }
    }

    @Override
    public Optional<Message> lastMessage() {
        lock.readLock().lock();
        try {
            if (currentLeafLoaded) {
                return Optional.ofNullable(currentLeaf);
            }
        } finally {
            lock.readLock().unlock();
        }

        lock.writeLock().lock();
        try {
            if (currentLeafLoaded) {
                return Optional.ofNullable(currentLeaf);
            }

            Optional<Message> lastMessage = store.getLastMessage(sessionId);
            lastMessage.ifPresent(message -> {
                currentLeaf = message;
                messageCache.put(message.info().id(), message);
            });
            currentLeafLoaded = true;
            return lastMessage;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public List<Message> lastMessages(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count must be non-negative");
        }

        List<Message> result = new ArrayList<>();
        Iterator<Message> it = reverse();
        for (int i = 0; i < count && it.hasNext(); i++) {
            result.add(it.next());
        }
        Collections.reverse(result);
        return Collections.unmodifiableList(result);
    }

    @Override
    public List<Message> findMessages(Predicate<Message> predicate, boolean forward) {
        Objects.requireNonNull(predicate, "predicate must not be null");

        List<Message> result = new ArrayList<>();
        Iterator<Message> it = forward ? iterator() : reverse();
        while (it.hasNext()) {
            Message msg = it.next();
            if (predicate.test(msg)) {
                result.add(msg);
            }
        }

        return Collections.unmodifiableList(result);
    }

    @Override
    public Optional<Message> findMessage(Predicate<Message> predicate, boolean forward) {
        Objects.requireNonNull(predicate, "predicate must not be null");

        Iterator<Message> it = forward ? iterator() : reverse();
        while (it.hasNext()) {
            Message msg = it.next();
            if (predicate.test(msg)) {
                return Optional.of(msg);
            }
        }

        return Optional.empty();
    }

    @Override
    public Iterator<Message> iterator() {
        return new ForwardBranchIterator();
    }

    @Override
    public Iterator<Message> reverse() {
        return new BackwardBranchIterator();
    }

    @Override
    public Optional<MessageId> currentLeafId() {
        return lastMessage().map(Message::info).map(MessageInfo::id);
    }

    @Override
    public void switchToBranch(MessageId leafMessageId) {
        Objects.requireNonNull(leafMessageId, "leafMessageId must not be null");

        lock.writeLock().lock();
        try {
            if (currentLeaf != null && Objects.equals(currentLeaf.info().id(), leafMessageId)) {
                // No need to update
            } else {
                // Verify it's a leaf by checking store
                List<MessageId> leaves = store.getLeafMessageIds(sessionId);
                if (!leaves.contains(leafMessageId)) {
                    throw new IllegalArgumentException("Message " + leafMessageId + " is not a leaf");
                }

                // Force flush message buffer on the current branch
                flushNow();

                // Set the leaf message as current leaf
                setCurrentLeaf(getMessage(leafMessageId).orElse(null));
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public List<MessageId> listLeafMessageIds() {
        lock.readLock().lock();
        try {
            List<MessageId> leafMessageIds = store.getLeafMessageIds(sessionId);
            // Add current leaf because of the current leaf maybe in the buffer
            Optional<MessageId> currentLeafId = currentLeafId();
            if (currentLeafId.isPresent() && !leafMessageIds.contains(currentLeafId.get())) {
                List<MessageId> result = new ArrayList<>(leafMessageIds.size() + 1);
                result.addAll(leafMessageIds);
                result.add(currentLeafId.get());
                return Collections.unmodifiableList(result);
            }
            return leafMessageIds;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Message> getPathFromRoot(MessageId leaf) {
        Objects.requireNonNull(leaf, "leaf must not be null");

        ForwardBranchIterator iterator = new ForwardBranchIterator(leaf);
        return Lists.newArrayList(iterator);
    }

    @Override
    public void fork(MessageId parentMessageId, Message message) {
        Objects.requireNonNull(parentMessageId, "parentMessageId must not be null");
        Objects.requireNonNull(message, "message must not be null");

        lock.writeLock().lock();
        try {
            // Verify parent exists and is a leaf
            Optional<Message> parentOpt = getMessage(parentMessageId);
            if (parentOpt.isEmpty()) {
                throw new IllegalArgumentException("Parent message does not exist: " + parentMessageId);
            }

            List<MessageId> leaves = listLeafMessageIds();
            if (leaves.contains(parentMessageId)) {
                throw new IllegalArgumentException("Parent message " + parentMessageId + " has been a leaf");
            }

            // Set parent ID if not set (depends on Message implementation)
            if (message.info().parentId() == null) {
                message = message.withParentId(parentMessageId);
            }
            if (!Objects.equals(message.info().parentId(), parentMessageId)) {
                throw new IllegalArgumentException("Parent ID does not match parent message ID");
            }

            // Force flush message buffer on the current branch
            flushNow();

            // Add message to buffer
            messageBuffer.add(message);
            flushIfRequired();

            // Switch to new branch
            setCurrentLeaf(message);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            messageBuffer.clear();
            messageCache.clear();
            store.deleteMessages(sessionId);
            setCurrentLeaf(null);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Conversation copy(SessionId newSessionId, Supplier<MessageId> messageIdGenerator) {
        Objects.requireNonNull(newSessionId, "newSessionId must not be null");
        Objects.requireNonNull(messageIdGenerator, "messageIdGenerator must not be null");

        // Load all messages in current branch
        ForwardBranchIterator iterator = new ForwardBranchIterator();
        List<Message> messages = Lists.newArrayList(iterator);

        // Create new ID mapping
        Map<MessageId, MessageId> idMapping = Maps.newHashMap();
        for (Message message : messages) {
            if (!idMapping.containsKey(message.info().id())) {
                idMapping.putIfAbsent(message.info().id(), messageIdGenerator.get());
            }
        }

        DefaultConversation conversation = new DefaultConversation(newSessionId, store);
        for (Message message : messages) {
            MessageId newMessageId = idMapping.get(message.info().id());
            MessageId newParentId = idMapping.get(message.info().parentId());
            Message newMessage = message.toBuilder()
                    .info(message.info().toBuilder()
                            .id(newMessageId)
                            .parentId(newParentId)
                            .sessionId(newSessionId)
                            .build())
                    .build();
            conversation.appendMessage(newMessage);
        }
        return conversation;
    }

    private void flushIfRequired() {
        if (messageBuffer.size() >= config.maxMessageBufferSize
                || config.flushIntervalMillis <= 0) {
            flushNow();
        }
    }

    private void flushNow() {
        try {
            flush();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to flush message buffer", e);
        }
    }

    @Override
    public void flush() throws IOException {
        if (flushing) return;

        List<Message> toFlush;
        lock.writeLock().lock();
        try {
            if (messageBuffer.isEmpty() || flushing) {
                // Nothing to flush
                return;
            }

            flushing = true;
            toFlush = Lists.newArrayList(messageBuffer);
            messageBuffer.clear();
        } finally {
            lock.writeLock().unlock();
        }

        try {
            if (toFlush.size() == 1) {
                store.appendMessage(sessionId, toFlush.get(0));
            } else {
                store.appendMessages(sessionId, toFlush);
            }
        } catch (Exception e) {
            lock.writeLock().lock();
            try {
                // Refill messages to buffer if flush failed
                messageBuffer.addAll(0, toFlush);
            } finally {
                lock.writeLock().unlock();
            }
            throw new IOException("Failed to flush messages", e);
        } finally {
            lock.writeLock().lock();
            try {
                flushing = false;
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    @Override
    public void close() throws IOException {
        lock.writeLock().lock();
        try {
            flush();
        } finally {
            messageCache.clear();
            setCurrentLeaf(null);
            lock.writeLock().unlock();
        }

        if (flushExecutor != null) {
            try {
                if (!flushExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    flushExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                flushExecutor.shutdownNow();
            }
        }
    }

    private void appendMessages(StringBuilder sb, List<Message> messages, int maxMessageLength) {
        for (Message message : messages) {
            String content = message.content();
            if (content.length() > maxMessageLength) {
                content = content.substring(0, maxMessageLength) + "...(truncated)";
            }
            ZonedDateTime createdTime = message.info().createdAt().atZone(ZoneId.systemDefault());
            sb.append("[").append(createdTime.format(TIME_FORMATTER)).append("] ");
            sb.append(message.info().role()).append(": ").append(content);
            sb.append("\n");
        }
    }

    public String asText(int maxMessageSize, int maxMessageLength) {
        StringBuilder sb = new StringBuilder("Conversation history (ID=").append(sessionId.value()).append("):\n");
        boolean hasMoreMessages = false;
        List<Message> lastMessages = Lists.newArrayList();
        for (Iterator<Message> iterator = reverse(); iterator.hasNext(); ) {
            Message message = iterator.next();
            if (lastMessages.size() >= maxMessageSize) {
                hasMoreMessages = true;
                break;
            }
            lastMessages.add(message);
        }
        if (hasMoreMessages) {
            sb.append("... (hidden messages)\n");
        }

        if (lastMessages.isEmpty()) {
            sb.append("<empty messages>\n");
        } else {
            appendMessages(sb, Lists.reverse(lastMessages), maxMessageLength);
        }
        return sb.toString();
    }

    public String asText() {
        return asText(10, 1000);
    }

    // ========== Iterator Implementations ==========

    private class ForwardBranchIterator implements Iterator<Message> {
        private final MessageId leafMessageId;
        private final List<Message> branchPath = new ArrayList<>();
        private int nextIndex;
        private boolean fullyLoaded;

        ForwardBranchIterator(MessageId leafMessageId) {
            this.leafMessageId = leafMessageId;
        }

        ForwardBranchIterator() {
            this.leafMessageId = currentLeafId().orElse(null);
        }

        private void loadCurrentPath() {
            lock.writeLock().lock();
            try {
                if (fullyLoaded) return;

                MessageId lastMessageId = null;
                MessageId currentMessageId = leafMessageId;
                while (currentMessageId != null) {
                    if (!messageCache.containsKey(currentMessageId)) {
                        if (lastMessageId != null) {
                            // Try loading multiple messages that are close in time to the current message at once
                            PageResult<Message> pageResult = store.loadMessagesBefore(
                                    sessionId, lastMessageId, config.messageLoadPageSize, null);
                            for (Message message : pageResult.items()) {
                                messageCache.put(message.info().id(), message);
                            }
                        }

                        if (!messageCache.containsKey(currentMessageId)) {
                            // If we do not find the parent message, we need to retrieve it by specific ID
                            store.getMessage(sessionId, currentMessageId)
                                    .ifPresent(message -> messageCache.put(message.info().id(), message));
                        }
                    }

                    Message currentMessage = messageCache.get(currentMessageId);
                    if (currentMessage != null) {
                        branchPath.add(0, currentMessage);
                        lastMessageId = currentMessageId;
                        currentMessageId = currentMessage.info().parentId();
                    }
                }

                fullyLoaded = true;
            } finally {
                lock.writeLock().unlock();
            }
        }

        @Override
        public boolean hasNext() {
            if (!fullyLoaded) loadCurrentPath();

            return nextIndex < branchPath.size();
        }

        @Override
        public Message next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            return branchPath.get(nextIndex++);
        }
    }

    private class BackwardBranchIterator implements Iterator<Message> {
        private boolean initialized;
        private MessageId prevId;
        private MessageId nextId;
        private boolean fullyLoaded;

        private void initialize() {
            lock.writeLock().lock();
            try {
                Optional<Message> lastMessage = lastMessage();
                if (lastMessage.isPresent()) {
                    nextId = lastMessage.get().info().id();
                } else {
                    fullyLoaded = true;
                }

                initialized = true;
            } finally {
                lock.writeLock().unlock();
            }
        }

        private void loadMessagesIfNeeded() {
            if (fullyLoaded || messageCache.containsKey(nextId)) return;

            lock.writeLock().lock();
            try {
                String nextCursor = null;
                boolean foundNext = false;
                do {
                    PageResult<Message> pageResult = store.loadMessagesBefore(
                            sessionId, prevId, config.messageLoadPageSize, nextCursor);
                    for (Message message : pageResult.items()) {
                        messageCache.put(message.info().id(), message);
                        if (Objects.equals(message.info().id(), nextId)) {
                            foundNext = true;
                        }
                    }
                    nextCursor = pageResult.nextCursor();
                    if (!pageResult.hasMore()) {
                        fullyLoaded = true;
                    }
                } while (!(foundNext || fullyLoaded));
            } finally {
                lock.writeLock().unlock();
            }
        }

        @Override
        public boolean hasNext() {
            if (!initialized) initialize();
            loadMessagesIfNeeded();

            return nextId != null;
        }

        @Override
        public Message next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            lock.readLock().lock();
            try {
                Message nextMessage = messageCache.get(nextId);
                prevId = nextId;
                nextId = nextMessage.info().parentId();
                return nextMessage;
            } finally {
                lock.readLock().unlock();
            }
        }
    }

    /**
     * Configuration for {@link DefaultConversation}.
     */
    public static class ConversationConfig {
        /**
         * Page size for loading messages.
         */
        private int messageLoadPageSize = 30;
        /**
         * Maximum number of messages to keep in memory.
         * All messages must be persisted to the store if value is set to 0.
         */
        private int maxMessageBufferSize = 5;
        /**
         * Flush interval in millis.
         * Messages are flushed to the store every {@code flushIntervalMillis}.
         */
        private int flushIntervalMillis = 30;

        public ConversationConfig messageLoadPageSize(int messageLoadPageSize) {
            if (messageLoadPageSize <= 0) {
                throw new IllegalArgumentException("messageLoadPageSize must be positive");
            }
            this.messageLoadPageSize = messageLoadPageSize;
            return this;
        }

        public ConversationConfig maxMessageBufferSize(int maxMessageBufferSize) {
            if (maxMessageBufferSize < 0) {
                throw new IllegalArgumentException("maxMessageBufferSize must not be negative");
            }
            this.maxMessageBufferSize = maxMessageBufferSize;
            return this;
        }

        public ConversationConfig flushIntervalMillis(int flushIntervalMillis) {
            this.flushIntervalMillis = flushIntervalMillis;
            return this;
        }

    }
}