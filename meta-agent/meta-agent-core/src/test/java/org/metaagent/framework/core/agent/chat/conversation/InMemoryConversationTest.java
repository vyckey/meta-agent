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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.metaagent.framework.common.util.PageResult;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.MessageId;
import org.metaagent.framework.core.agent.chat.message.RoleMessage;
import org.metaagent.framework.core.agent.chat.message.RoleMessageInfo;
import org.metaagent.framework.core.agent.chat.message.part.TextMessagePart;
import org.metaagent.framework.core.agent.chat.session.SessionId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link DefaultConversation} and {@link InMemoryConversationStore}.
 *
 * @author vyckey
 */
class InMemoryConversationTest {
    private InMemoryConversationStore store;
    private SessionId sessionId;
    private DefaultConversation.ConversationConfig config;
    private DefaultConversation conversation;

    @BeforeEach
    void setUp() {
        store = new InMemoryConversationStore();
        sessionId = SessionId.of("test-session");
        config = new DefaultConversation.ConversationConfig()
                .messageLoadPageSize(10)
                .maxMessageBufferSize(5)
                .flushIntervalMillis(1000);
        conversation = new DefaultConversation(sessionId, store, config);
    }

    // ==================== Helper Methods ====================

    private Message createMessage(String id, String parentId, String content) {
        return RoleMessage.builder()
                .info(RoleMessageInfo.user()
                        .sessionId(sessionId).id(MessageId.of(id))
                        .parentId(parentId != null ? MessageId.of(parentId) : null)
                        .build()
                )
                .addPart(new TextMessagePart(content))
                .build();
    }

    private Message createMessage(String id, String parentId) {
        return createMessage(id, parentId, "Content " + id);
    }

    // ==================== Basic Operation Tests ====================

    @Nested
    @DisplayName("Basic Operations")
    class BasicOperations {

        @Test
        @DisplayName("Empty conversation should be empty")
        void emptyConversation() {
            assertTrue(conversation.isEmpty());
            assertTrue(conversation.lastMessage().isEmpty());
            assertTrue(conversation.currentLeafId().isEmpty());
            assertEquals(Collections.emptyList(), conversation.lastMessages(5));
        }

        @Test
        @DisplayName("Append single message")
        void appendMessage() {
            Message msg1 = createMessage("msg1", null);
            conversation.appendMessage(msg1);

            assertFalse(conversation.isEmpty());
            assertEquals(Optional.of(msg1), conversation.lastMessage());
            assertEquals(Optional.of(MessageId.of("msg1")), conversation.currentLeafId());

            List<Message> lastMessages = conversation.lastMessages(1);
            assertEquals(1, lastMessages.size());
            assertEquals(msg1, lastMessages.get(0));
        }

        @Test
        @DisplayName("Append multiple messages")
        void appendMultipleMessages() {
            Message msg1 = createMessage("msg1", null);
            Message msg2 = createMessage("msg2", "msg1");
            Message msg3 = createMessage("msg3", "msg2");

            conversation.appendMessage(msg1);
            conversation.appendMessage(msg2);
            conversation.appendMessage(msg3);

            assertEquals(Optional.of(msg3), conversation.lastMessage());

            List<Message> last2 = conversation.lastMessages(2);
            assertEquals(2, last2.size());
            assertEquals(msg2, last2.get(0));
            assertEquals(msg3, last2.get(1));

            List<Message> last5 = conversation.lastMessages(5);
            assertEquals(3, last5.size());
        }

        @Test
        @DisplayName("Append with invalid parent should throw")
        void appendWithInvalidParent() {
            Message msg1 = createMessage("msg1", null);
            conversation.appendMessage(msg1);

            Message msg2 = createMessage("msg2", "non-existent");
            assertThrows(IllegalArgumentException.class, () -> conversation.appendMessage(msg2));
        }
    }

    // ==================== Buffer and Flush Tests ====================

    @Nested
    @DisplayName("Buffer and Flush Operations")
    class BufferAndFlushTests {

        @Test
        @DisplayName("Messages should be buffered and flushed on threshold")
        void bufferThresholdFlush() throws IOException {
            // Configure with small buffer
            DefaultConversation.ConversationConfig smallBufferConfig =
                    new DefaultConversation.ConversationConfig()
                            .maxMessageBufferSize(3)
                            .flushIntervalMillis(3000);

            try (Conversation conv = new DefaultConversation(sessionId, store, smallBufferConfig)) {
                Message msg1 = createMessage("msg1", null);
                Message msg2 = createMessage("msg2", "msg1");
                Message msg3 = createMessage("msg3", "msg2");

                // First two messages should be buffered
                conv.appendMessage(msg1);
                conv.appendMessage(msg2);

                // Verify in memory but not in store
                assertEquals(Optional.of(msg2), conv.lastMessage());
                assertEquals(0, store.getMessageCount(sessionId)); // Not yet flushed

                // Third message should trigger flush (buffer size = 3)
                conv.appendMessage(msg3);

                // Now store should have all messages
                assertEquals(3, store.getMessageCount(sessionId));

                // Verify messages in store
                Optional<Message> storedMsg1 = store.getMessage(sessionId, MessageId.of("msg1"));
                Optional<Message> storedMsg2 = store.getMessage(sessionId, MessageId.of("msg2"));
                Optional<Message> storedMsg3 = store.getMessage(sessionId, MessageId.of("msg3"));

                assertTrue(storedMsg1.isPresent());
                assertTrue(storedMsg2.isPresent());
                assertTrue(storedMsg3.isPresent());
                assertEquals("msg1", storedMsg1.get().info().id().value());

                conv.clear();
            }
        }

        @Test
        @DisplayName("Manual flush should persist buffered messages")
        void manualFlush() throws IOException {
            Message msg1 = createMessage("msg1", null);
            Message msg2 = createMessage("msg2", "msg1");

            conversation.appendMessage(msg1);
            conversation.appendMessage(msg2);

            assertEquals(0, store.getMessageCount(sessionId));

            conversation.flush();

            assertEquals(2, store.getMessageCount(sessionId));
        }

        @Test
        @DisplayName("Flush failure should keep messages in buffer")
        void flushFailure() throws IOException {
            // Create a failing store
            ConversationStore failingStore = new FailingConversationStore();
            DefaultConversation conv = new DefaultConversation(sessionId, failingStore, config);

            Message msg1 = createMessage("msg1", null);
            conv.appendMessage(msg1);

            // Flush should throw
            assertThrows(IOException.class, conv::flush);

            // Message should still be in buffer and accessible
            assertEquals(Optional.of(msg1), conv.lastMessage());
        }

        @Test
        @DisplayName("Auto-flush with interval should work")
        void autoFlushWithInterval() throws InterruptedException, IOException {
            DefaultConversation.ConversationConfig autoFlushConfig =
                    new DefaultConversation.ConversationConfig()
                            .maxMessageBufferSize(10)
                            .flushIntervalMillis(100); // Flush every second

            SessionId newSessionId = SessionId.next();
            try (DefaultConversation conv = new DefaultConversation(newSessionId, store, autoFlushConfig)) {
                Message msg1 = createMessage("msg1", null);
                Message msg2 = createMessage("msg2", "msg1");

                conv.appendMessage(msg1);
                conv.appendMessage(msg2);

                // Wait for auto-flush
                Thread.sleep(1000);

                // Messages should be persisted
                assertEquals(2, store.getMessageCount(newSessionId));

                conv.clear();
            }
        }
    }

    // ==================== Update and Reset Tests ====================

    @Nested
    @DisplayName("Update and Reset Operations")
    class UpdateAndResetTests {

        @Test
        @DisplayName("Update existing message")
        void updateMessage() throws IOException {
            Message msg1 = createMessage("msg1", null, "Original");
            conversation.appendMessage(msg1);
            conversation.flush();

            Message updatedMsg1 = createMessage("msg1", null, "Updated");
            conversation.updateMessage(updatedMsg1);

            assertEquals(Optional.of(updatedMsg1), conversation.lastMessage());

            // Verify in store after flush
            conversation.flush();
            Optional<Message> stored = store.getMessage(sessionId, MessageId.of("msg1"));
            assertTrue(stored.isPresent());
            assertEquals("Updated", stored.get().content());
        }

        @Test
        @DisplayName("Update non-existent message should throw")
        void updateNonExistentMessage() {
            Message msg1 = createMessage("msg1", null);
            assertThrows(IllegalArgumentException.class,
                    () -> conversation.updateMessage(msg1));
        }

        @Test
        @DisplayName("Reset after message (inclusive=false)")
        void resetAfterInclusiveFalse() {
            // Build branch: root -> a -> b -> c
            Message root = createMessage("root", null);
            Message a = createMessage("a", "root");
            Message b = createMessage("b", "a");
            Message c = createMessage("c", "b");

            conversation.appendMessage(root);
            conversation.appendMessage(a);
            conversation.appendMessage(b);
            conversation.appendMessage(c);

            assertEquals(Optional.of(c), conversation.lastMessage());

            // Delete after b (keep b)
            conversation.deleteMessagesAfter(MessageId.of("b"), false);

            // Current leaf should be b
            assertEquals(Optional.of(MessageId.of("b")), conversation.currentLeafId());
            assertEquals(Optional.of(b), conversation.lastMessage());

            // Verify path
            List<Message> path = conversation.getPathFromRoot(MessageId.of("b"));
            assertEquals(3, path.size());
            assertEquals("root", path.get(0).info().id().value());
            assertEquals("a", path.get(1).info().id().value());
            assertEquals("b", path.get(2).info().id().value());
        }

        @Test
        @DisplayName("Reset after message (inclusive=true)")
        void resetAfterInclusiveTrue() throws IOException {
            // Build branch: root -> a -> b -> c
            Message root = createMessage("root", null);
            Message a = createMessage("a", "root");
            Message b = createMessage("b", "a");
            Message c = createMessage("c", "b");

            conversation.appendMessage(root);
            conversation.appendMessage(a);
            conversation.appendMessage(b);
            conversation.appendMessage(c);

            conversation.flush();

            // Delete after b (delete b and its descendants)
            conversation.deleteMessagesAfter(MessageId.of("b"), true);

            // Current leaf should be a
            assertEquals(Optional.of(MessageId.of("a")), conversation.currentLeafId());
            assertEquals(Optional.of(a), conversation.lastMessage());

            // Verify b and c are gone
            assertTrue(store.getMessage(sessionId, MessageId.of("b")).isEmpty());
            assertTrue(store.getMessage(sessionId, MessageId.of("c")).isEmpty());
        }
    }

    // ==================== Branch Management Tests ====================

    @Nested
    @DisplayName("Branch Management")
    class BranchManagementTests {

        @Test
        @DisplayName("Fork new branch from leaf")
        void forkNewBranch() throws IOException {
            // Build main branch: root -> a -> b
            Message root = createMessage("root", null);
            Message a = createMessage("a", "root");
            Message b = createMessage("b", "a");

            conversation.appendMessage(root);
            conversation.appendMessage(a);
            conversation.appendMessage(b);
            conversation.flush();

            // Fork from 'a' to create alternative branch: a -> c
            Message c = createMessage("c", "a", "Alternative");
            conversation.fork(MessageId.of("a"), c);

            // Current branch should be the new one
            assertEquals(Optional.of(MessageId.of("c")), conversation.currentLeafId());
            assertEquals(Optional.of(c), conversation.lastMessage());

            // Verify both branches exist
            List<MessageId> leaves = conversation.listLeafMessageIds();
            assertEquals(2, leaves.size());
            assertTrue(leaves.contains(MessageId.of("b")));
            assertTrue(leaves.contains(MessageId.of("c")));
        }

        @Test
        @DisplayName("Switch between branches")
        void switchBranches() {
            // Build branch1: root -> a -> b
            Message root = createMessage("root", null);
            Message a = createMessage("a", "root");
            Message b = createMessage("b", "a");

            conversation.appendMessage(root);
            conversation.appendMessage(a);
            conversation.appendMessage(b);

            // Fork branch2: a -> c
            Message c = createMessage("c", "a");
            conversation.fork(MessageId.of("a"), c);

            // Switch back to branch1
            conversation.switchToBranch(MessageId.of("b"));
            assertEquals(Optional.of(MessageId.of("b")), conversation.currentLeafId());
            assertEquals(Optional.of(b), conversation.lastMessage());

            // Switch to branch2
            conversation.switchToBranch(MessageId.of("c"));
            assertEquals(Optional.of(MessageId.of("c")), conversation.currentLeafId());
            assertEquals(Optional.of(c), conversation.lastMessage());
        }

        @Test
        @DisplayName("Switch to non-leaf should throw")
        void switchToNonLeaf() {
            Message root = createMessage("root", null);
            Message a = createMessage("a", "root");
            Message b = createMessage("b", "a");

            conversation.appendMessage(root);
            conversation.appendMessage(a);
            conversation.appendMessage(b);

            // 'a' is not a leaf (has child in buffer)
            assertThrows(IllegalArgumentException.class,
                    () -> conversation.switchToBranch(MessageId.of("a")));
        }
    }

    // ==================== Query Tests ====================

    @Nested
    @DisplayName("Query Operations")
    class QueryTests {

        @Test
        @DisplayName("Find messages with predicate")
        void findMessages() {
            Message root = createMessage("root", null, "Start");
            Message a = createMessage("a", "root", "Hello");
            Message b = createMessage("b", "a", "World");
            Message c = createMessage("c", "b", "End");

            conversation.appendMessage(root);
            conversation.appendMessage(a);
            conversation.appendMessage(b);
            conversation.appendMessage(c);

            // Find messages containing 'd'
            Predicate<Message> containsD = msg -> msg.content().contains("d");

            List<Message> found = conversation.findMessages(containsD, true);
            assertEquals(2, found.size());
            assertEquals("b", found.get(0).info().id().value()); // "Start" contains 'd'
            assertEquals("c", found.get(1).info().id().value());    // "End" contains 'd'

            // Find first in reverse
            Optional<Message> firstReverse = conversation.findMessage(containsD, false);
            assertTrue(firstReverse.isPresent());
            assertEquals("c", firstReverse.get().info().id().value());
        }

        @Test
        @DisplayName("Get path from root to leaf")
        void getPathFromRoot() {
            Message root = createMessage("root", null);
            Message a = createMessage("a", "root");
            Message b = createMessage("b", "a");
            Message c = createMessage("c", "b");

            conversation.appendMessage(root);
            conversation.appendMessage(a);
            conversation.appendMessage(b);
            conversation.appendMessage(c);

            List<Message> path = conversation.getPathFromRoot(MessageId.of("c"));
            assertEquals(4, path.size());
            assertEquals("root", path.get(0).info().id().value());
            assertEquals("a", path.get(1).info().id().value());
            assertEquals("b", path.get(2).info().id().value());
            assertEquals("c", path.get(3).info().id().value());
        }

        @Test
        @DisplayName("Last messages with count")
        void lastMessages() {
            for (int i = 1; i <= 10; i++) {
                String parentId = i == 1 ? null : "msg" + (i - 1);
                Message msg = createMessage("msg" + i, parentId);
                conversation.appendMessage(msg);
            }

            List<Message> last3 = conversation.lastMessages(3);
            assertEquals(3, last3.size());
            assertEquals("msg8", last3.get(0).info().id().value());
            assertEquals("msg9", last3.get(1).info().id().value());
            assertEquals("msg10", last3.get(2).info().id().value());

            List<Message> last20 = conversation.lastMessages(20);
            assertEquals(10, last20.size());
        }
    }

    // ==================== Iterator Tests ====================

    @Nested
    @DisplayName("Iterator Operations")
    class IteratorTests {

        @Test
        @DisplayName("Forward iterator should traverse root to leaf")
        void forwardIterator() {
            Message root = createMessage("root", null);
            Message a = createMessage("a", "root");
            Message b = createMessage("b", "a");

            conversation.appendMessage(root);
            conversation.appendMessage(a);
            conversation.appendMessage(b);

            List<Message> iterated = new ArrayList<>();
            conversation.iterator().forEachRemaining(iterated::add);

            assertEquals(3, iterated.size());
            assertEquals("root", iterated.get(0).info().id().value());
            assertEquals("a", iterated.get(1).info().id().value());
            assertEquals("b", iterated.get(2).info().id().value());
        }

        @Test
        @DisplayName("Reverse iterator should traverse leaf to root")
        void reverseIterator() {
            Message root = createMessage("root", null);
            Message a = createMessage("a", "root");
            Message b = createMessage("b", "a");

            conversation.appendMessage(root);
            conversation.appendMessage(a);
            conversation.appendMessage(b);

            List<Message> iterated = new ArrayList<>();
            conversation.reverse().forEachRemaining(iterated::add);

            assertEquals(3, iterated.size());
            assertEquals("b", iterated.get(0).info().id().value());
            assertEquals("a", iterated.get(1).info().id().value());
            assertEquals("root", iterated.get(2).info().id().value());
        }

        @Test
        @DisplayName("Iterator should reflect current branch after switch")
        void iteratorAfterBranchSwitch() {
            Message root = createMessage("root", null);
            Message a = createMessage("a", "root");
            Message b = createMessage("b", "a");
            Message c = createMessage("c", "a"); // alternative

            conversation.appendMessage(root);
            conversation.appendMessage(a);
            conversation.appendMessage(b);
            conversation.fork(MessageId.of("a"), c);

            // Forward iterator on current branch (c)
            List<Message> iterated = new ArrayList<>();
            conversation.iterator().forEachRemaining(iterated::add);

            assertEquals(3, iterated.size());
            assertEquals("root", iterated.get(0).info().id().value());
            assertEquals("a", iterated.get(1).info().id().value());
            assertEquals("c", iterated.get(2).info().id().value());
        }
    }

    // ==================== Edge Case Tests ====================

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Clear conversation")
        void clearConversation() throws IOException {
            Message root = createMessage("root", null);
            Message a = createMessage("a", "root");

            conversation.appendMessage(root);
            conversation.appendMessage(a);
            conversation.flush();

            assertFalse(conversation.isEmpty());
            assertEquals(2, store.getMessageCount(sessionId));

            conversation.clear();

            assertTrue(conversation.isEmpty());
            assertEquals(0, store.getMessageCount(sessionId));
            assertFalse(store.hasSession(sessionId));
        }

        @Test
        @DisplayName("Copy conversation to new session")
        void copyConversation() throws IOException {
            Message root = createMessage("root", null);
            Message a = createMessage("a", "root");
            Message b = createMessage("b", "a");

            conversation.appendMessage(root);
            conversation.appendMessage(a);
            conversation.appendMessage(b);
            conversation.flush();

            SessionId newSessionId = SessionId.of("new-session");
            Supplier<MessageId> idGenerator = () -> MessageId.of(UUID.randomUUID().toString());

            try (Conversation copy = conversation.copy(newSessionId, idGenerator)) {
                // Verify copy has same structure
                assertEquals(Optional.of(newSessionId), Optional.of(copy.sessionId()));

                List<Message> originalPath = conversation.getPathFromRoot(MessageId.of("b"));
                List<Message> copyPath = copy.getPathFromRoot(copy.currentLeafId().get());

                assertEquals(originalPath.size(), copyPath.size());
                for (int i = 0; i < originalPath.size(); i++) {
                    assertEquals(originalPath.get(i).content(), copyPath.get(i).content());
                    assertNotEquals(originalPath.get(i).info().id(), copyPath.get(i).info().id());
                }
                copy.clear();
            }
        }

        @Test
        @DisplayName("Large message chain with pagination")
        void largeMessageChain() throws IOException {
            int messageCount = 100;

            // Build chain
            Message prev = null;
            for (int i = 1; i <= messageCount; i++) {
                String parentId = prev != null ? prev.info().id().value() : null;
                Message msg = createMessage("msg" + i, parentId);
                conversation.appendMessage(msg);
                prev = msg;
            }
            conversation.flush();

            // Test pagination
            PageResult<Message> page1 = store.loadMessagesAfter(sessionId, null, 30, null);
            assertEquals(30, page1.items().size());
            assertTrue(page1.hasMore());
            assertNotNull(page1.nextCursor());

            PageResult<Message> page2 = store.loadMessagesAfter(sessionId, null, 30, page1.nextCursor());
            assertEquals(30, page2.items().size());

            PageResult<Message> page3 = store.loadMessagesAfter(sessionId, null, 30, page2.nextCursor());
            assertEquals(30, page3.items().size());

            PageResult<Message> page4 = store.loadMessagesAfter(sessionId, null, 30, page3.nextCursor());
            assertEquals(10, page4.items().size());
            assertFalse(page4.hasMore());
        }
    }

    // ==================== Helper Classes ====================

    /**
     * A failing store implementation for testing error handling.
     */
    private static class FailingConversationStore implements ConversationStore {
        @Override
        public void appendMessage(SessionId sessionId, Message message) {
            throw new RuntimeException("Simulated failure");
        }

        @Override
        public void appendMessages(SessionId sessionId, List<Message> messages) {
            throw new RuntimeException("Simulated failure");
        }

        @Override
        public void updateMessage(SessionId sessionId, Message message) {
            throw new RuntimeException("Simulated failure");
        }

        @Override
        public Optional<Message> getMessage(SessionId sessionId, MessageId messageId) {
            return Optional.empty();
        }

        @Override
        public Optional<Message> getLastMessage(SessionId sessionId) {
            return Optional.empty();
        }

        @Override
        public List<MessageId> getLeafMessageIds(SessionId sessionId) {
            return Collections.emptyList();
        }

        @Override
        public PageResult<Message> loadMessagesBefore(SessionId sessionId, MessageId messageId, int limit, String cursor) {
            return new PageResult<>(Collections.emptyList(), null, false);
        }

        @Override
        public PageResult<Message> loadMessagesAfter(SessionId sessionId, MessageId messageId, int limit, String cursor) {
            return new PageResult<>(Collections.emptyList(), null, false);
        }

        @Override
        public Set<MessageId> deleteMessages(SessionId sessionId, MessageId rootMessageId, boolean inclusive) {
            throw new RuntimeException("Simulated failure");
        }

        @Override
        public void deleteMessages(SessionId sessionId) {
            throw new RuntimeException("Simulated failure");
        }
    }

}

