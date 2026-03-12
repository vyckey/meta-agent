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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.metaagent.framework.common.util.PageResult;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.MessageId;
import org.metaagent.framework.core.agent.chat.message.MessageInfo;
import org.metaagent.framework.core.agent.chat.message.MessageSerializer;
import org.metaagent.framework.core.agent.chat.session.SessionId;
import org.metaagent.framework.core.agent.chat.session.SessionIdValue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * JSON file-based implementation of {@link ConversationStore}.
 * <p>
 * Each session is stored in a separate JSON file under the configured root directory.
 * All operations are performed by reading the entire file into memory, modifying the
 * in-memory list, and then writing back. This implementation is not suitable for
 * high-concurrency or large-scale scenarios, but serves as a simple demonstration.
 * </p>
 *
 * @author vyckey
 */
public class JsonFileConversationStore implements ConversationStore {
    private final Path storageRoot;
    private final Function<SessionId, String> fileNameResolver;
    private final ObjectMapper objectMapper;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Constructs a new JsonFileConversationStore with the given storage root directory.
     *
     * @param storageRoot      the directory where session files will be stored
     * @param fileNameResolver the function to resolve file names from session IDs
     * @throws IOException if the directory cannot be created
     */
    public JsonFileConversationStore(Path storageRoot, Function<SessionId, String> fileNameResolver) throws IOException {
        this.storageRoot = storageRoot.toAbsolutePath().normalize();
        this.fileNameResolver = Objects.requireNonNull(fileNameResolver, "fileNameResolver cannot be null");
        this.objectMapper = MessageSerializer.getObjectMapper();
        Files.createDirectories(this.storageRoot);
    }

    public JsonFileConversationStore(Path storageRoot) throws IOException {
        this(storageRoot, sessionId -> "session-" + sessionId.value() + ".json");
    }

    // ==================== Core File Operations ====================

    private Path getSessionFile(SessionId sessionId) {
        return storageRoot.resolve(fileNameResolver.apply(sessionId) + ".json");
    }

    private ConversationDO readConversation(SessionId sessionId) {
        Path file = getSessionFile(sessionId);
        if (!Files.exists(file)) {
            return new ConversationDO(sessionId, new ArrayList<>());
        }
        try {
            return objectMapper.readValue(file.toFile(), ConversationDO.class);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read session file: " + file, e);
        }
    }

    private void writeConversation(SessionId sessionId, ConversationDO conversation) {
        Path file = getSessionFile(sessionId);
        try {
            objectMapper.writeValue(file.toFile(), conversation);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write session file: " + file, e);
        }
    }

    private <T> T withReadLock(SessionId sessionId, Function<ConversationDO, T> action) {
        lock.readLock().lock();
        try {
            ConversationDO conversation = readConversation(sessionId);
            return action.apply(conversation);
        } finally {
            lock.readLock().unlock();
        }
    }

    private <T> T withWriteLock(SessionId sessionId, Function<ConversationDO, T> action) {
        lock.writeLock().lock();
        try {
            ConversationDO conversation = readConversation(sessionId);
            T result = action.apply(conversation);
            writeConversation(sessionId, conversation);
            return result;
        } finally {
            lock.writeLock().unlock();
        }
    }

    // ==================== Interface Implementations ====================

    @Override
    public void appendMessage(SessionId sessionId, Message message) {
        withWriteLock(sessionId, conversation -> {
            // Validate parent exists if parentId is not null
            MessageId parentId = message.info().parentId();
            if (parentId != null) {
                boolean parentExists = conversation.messages().stream()
                        .map(Message::info).map(MessageInfo::id)
                        .anyMatch(id -> id.equals(parentId));
                if (!parentExists) {
                    throw new IllegalArgumentException("Parent message not found: " + parentId);
                }
            }
            conversation.messages().add(message);
            return null;
        });
    }

    @Override
    public void appendMessages(SessionId sessionId, List<Message> messagesToAdd) {
        withWriteLock(sessionId, conversation -> {
            // Build set of existing IDs for validation
            Set<MessageId> existingIds = conversation.messages().stream()
                    .map(Message::info).map(MessageInfo::id)
                    .collect(Collectors.toCollection(HashSet::new));

            // Validate all parent IDs exist
            for (Message msg : messagesToAdd) {
                MessageId parentId = msg.info().parentId();
                if (parentId != null && !existingIds.contains(parentId)) {
                    throw new IllegalArgumentException("Parent message not found: " + parentId);
                }
                existingIds.add(msg.info().id());
            }

            // Add all messages
            conversation.messages().addAll(messagesToAdd);
            return null;
        });
    }

    @Override
    public void updateMessage(SessionId sessionId, Message message) {
        withWriteLock(sessionId, conversation -> {
            List<Message> messages = conversation.messages();
            for (int i = 0; i < messages.size(); i++) {
                if (messages.get(i).info().id().equals(message.info().id())) {
                    messages.set(i, message);
                    return null;
                }
            }
            throw new IllegalArgumentException("Message not found: " + message.info().id());
        });
    }

    @Override
    public Optional<Message> getMessage(SessionId sessionId, MessageId messageId) {
        return withReadLock(sessionId, conversation ->
                conversation.messages().stream()
                        .filter(m -> m.info().id().equals(messageId))
                        .findFirst()
        );
    }

    @Override
    public Optional<Message> getLastMessage(SessionId sessionId) {
        return withReadLock(sessionId, conversation -> {
            List<Message> messages = conversation.messages();
            if (messages.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(messages.get(messages.size() - 1));
        });
    }

    @Override
    public List<MessageId> getLeafMessageIds(SessionId sessionId) {
        return withReadLock(sessionId, conversation -> {
            List<Message> messages = conversation.messages();
            if (messages.isEmpty()) {
                return Collections.emptyList();
            }

            // Find all messages that are not used as parent
            Set<MessageId> allIds = messages.stream()
                    .map(Message::info).map(MessageInfo::id)
                    .collect(Collectors.toSet());
            Set<MessageId> parentIds = messages.stream()
                    .map(Message::info).map(MessageInfo::parentId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            // Leaf nodes are those that are not parents of any message
            return allIds.stream()
                    .filter(id -> !parentIds.contains(id))
                    .collect(Collectors.toList());
        });
    }

    @Override
    public PageResult<Message> loadMessagesBefore(SessionId sessionId, MessageId messageId, int limit, String cursor) {
        return withReadLock(sessionId, conversation -> {
            List<Message> messages = conversation.messages();
            if (messages.isEmpty()) {
                return PageResult.empty();
            }

            final int messageIndex = messageId != null ? conversation.findMessageIndex(messageId) : messages.size();
            if (messageIndex <= 0) {
                return PageResult.empty();
            }

            final int toIdx = messageIndex - (cursor != null ? Math.max(0, Integer.parseInt(cursor)) : 0);
            final int fromIdx = Math.max(toIdx - limit, 0);
            if (fromIdx >= toIdx) {
                return PageResult.empty();
            }

            List<Message> items = Lists.newArrayList(messages.subList(fromIdx, toIdx));
            Collections.reverse(items);
            if (fromIdx > 0) {
                return PageResult.of(items, String.valueOf(messageIndex - fromIdx), true);
            }
            return PageResult.of(items, null, false);
        });
    }

    @Override
    public PageResult<Message> loadMessagesAfter(SessionId sessionId, MessageId messageId, int limit, String cursor) {
        return withReadLock(sessionId, conversation -> {
            List<Message> messages = conversation.messages();
            if (messages.isEmpty()) {
                return PageResult.empty();
            }

            final int messageIndex = messageId != null ? conversation.findMessageIndex(messageId) : -1;
            if (messageId != null && messageIndex < 0) {
                return PageResult.empty();
            }

            final int fromIdx = messageIndex + 1 + (cursor != null ? Integer.parseInt(cursor) : 0);
            final int toIdx = Math.min(fromIdx + limit, messages.size());
            if (fromIdx >= toIdx) {
                return PageResult.empty();
            }

            List<Message> items = Lists.newArrayList(messages.subList(fromIdx, toIdx));
            if (toIdx < messages.size()) {
                return PageResult.of(items, String.valueOf(toIdx - messageIndex - 1), true);
            }
            return PageResult.of(items, null, false);
        });
    }


    @Override
    public Set<MessageId> deleteMessages(SessionId sessionId, MessageId rootMessageId, boolean inclusive) {
        return withWriteLock(sessionId, conversation -> {
            List<Message> messages = conversation.messages();

            MessageTree messageTree = new MessageTree(messages);
            if (!messageTree.messageMap.containsKey(rootMessageId)) {
                throw new IllegalArgumentException("Root message not found: " + rootMessageId);
            }

            List<MessageId> toDelete = new ArrayList<>();
            messageTree.findDescendantIds(rootMessageId, toDelete);
            if (inclusive) {
                toDelete.add(messageTree.messageMap.get(rootMessageId).info().id());
            }

            // Remove messages
            messages.removeIf(msg -> toDelete.contains(msg.info().id()));
            return Sets.newHashSet(toDelete);
        });
    }

    @Override
    public void deleteMessages(SessionId sessionId) {
        Path file = getSessionFile(sessionId);
        lock.writeLock().lock();
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete session file: " + file, e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Data object for JSON serialization.
     */
    private record ConversationDO(
            @JsonProperty("sessionId") @JsonDeserialize(as = SessionIdValue.class)
            SessionId sessionId,

            @JsonProperty("messages")
            List<Message> messages
    ) {
        private ConversationDO {
            // Ensure messages list is mutable
            messages = messages != null ? new ArrayList<>(messages) : new ArrayList<>();
        }

        private int findMessageIndex(MessageId messageId) {
            for (int i = 0; i < messages.size(); i++) {
                if (messages.get(i).info().id().equals(messageId)) {
                    return i;
                }
            }
            return -1;
        }
    }

    private static class MessageTree {
        Map<MessageId, List<Message>> childrenMap = new HashMap<>();
        Map<MessageId, Message> messageMap = new HashMap<>();

        MessageTree(List<Message> messages) {
            for (Message msg : messages) {
                messageMap.put(msg.info().id(), msg);
                MessageId parentId = msg.info().parentId();
                if (parentId != null) {
                    childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(msg);
                }
            }
        }

        void findDescendantIds(MessageId parentId, List<MessageId> foundDescendantIds) {
            if (parentId == null) {
                return;
            }

            List<Message> children = childrenMap.getOrDefault(parentId, Collections.emptyList());
            for (Message child : children) {
                foundDescendantIds.add(child.info().id());
                findDescendantIds(child.info().id(), foundDescendantIds);
            }
        }
    }
}