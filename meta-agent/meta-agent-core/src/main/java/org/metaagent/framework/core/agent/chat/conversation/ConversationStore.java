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

import org.metaagent.framework.common.util.PageResult;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.MessageId;
import org.metaagent.framework.core.agent.chat.session.SessionId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Provides persistent storage and retrieval operations for conversation messages.
 * <p>
 * A conversation is uniquely identified by a {@link SessionId} and consists of
 * a tree of messages, each with a unique {@link MessageId} and an optional parent ID.
 * This store handles all CRUD operations for messages, including tree-aware queries
 * like retrieving leaf nodes or loading messages relative to a specific point.
 * </p>
 *
 * @author vyckey
 */
public interface ConversationStore {

    /**
     * Appends a single message to the conversation.
     * <p>
     * The message's parent ID must be {@code null} for the first message,
     * or refer to an existing message ID in the same session. The implementation
     * should ensure the message is persisted atomically.
     * </p>
     *
     * @param sessionId the session identifier (must not be {@code null})
     * @param message   the message to append (must not be {@code null})
     * @throws IllegalArgumentException if the parent ID is invalid or sessionId is null
     */
    void appendMessage(SessionId sessionId, Message message);

    /**
     * Appends multiple messages to the conversation in a single batch operation.
     * <p>
     * This method is semantically equivalent to calling {@link #appendMessage}
     * repeatedly, but may offer better performance by reducing round trips.
     * All messages must belong to the same session, and their parent IDs must
     * form a consistent tree (i.e., no message refers to a later message as parent).
     * </p>
     *
     * @param sessionId     the session identifier (must not be {@code null})
     * @param messagesToAdd the list of messages to append (must not be {@code null})
     * @throws IllegalArgumentException if any message has an invalid parent ID
     */
    void appendMessages(SessionId sessionId, List<Message> messagesToAdd);

    /**
     * Updates an existing message in the conversation.
     * <p>
     * The message's ID must already exist in the session. The update typically
     * replaces the content or metadata, but should not change the message ID
     * or its parent relationship.
     * </p>
     *
     * @param sessionId the session identifier (must not be {@code null})
     * @param message   the updated message (must not be {@code null})
     * @throws IllegalArgumentException if the message ID does not exist
     */
    void updateMessage(SessionId sessionId, Message message);

    /**
     * Retrieves a specific message by its ID within a session.
     *
     * @param sessionId the session identifier (must not be {@code null})
     * @param messageId the message identifier (must not be {@code null})
     * @return an {@link Optional} containing the message if found, otherwise empty
     */
    Optional<Message> getMessage(SessionId sessionId, MessageId messageId);

    /**
     * Retrieves the ID of the most recent message in the conversation.
     * <p>
     * The "most recent" is defined as the message with the highest timestamp
     * (or insertion order) that has no children. This is typically the tip of
     * the current active branch.
     * </p>
     *
     * @param sessionId the session identifier (must not be {@code null})
     * @return an {@link Optional} containing the last message ID, or empty if the conversation is empty
     */
    Optional<Message> getLastMessage(SessionId sessionId);

    /**
     * Loads a page of messages that appear <strong>before</strong> a given message ID
     * (i.e., older messages, closer to the root).
     * <p>
     * The messages are returned in reverse chronological order (from newest to oldest)
     * starting from the message immediately preceding the specified {@code messageId}.
     * If {@code messageId} is {@code null}, the page starts from the most recent message.
     * </p>
     *
     * @param sessionId the session identifier (must not be {@code null})
     * @param messageId the reference message ID; messages older than this are returned;
     *                  if {@code null}, start from the newest message
     * @param limit     maximum number of messages to return
     * @param cursor    an opaque cursor from a previous page result, used to continue paging;
     *                  may be {@code null} for the first page
     * @return a {@link PageResult} containing the messages and a cursor for the next page
     */
    PageResult<Message> loadMessagesBefore(SessionId sessionId, MessageId messageId, int limit, String cursor);

    /**
     * Loads a page of messages that appear <strong>after</strong> a given message ID
     * (i.e., newer messages, further towards the leaves).
     * <p>
     * The messages are returned in chronological order (from oldest to newest)
     * starting from the message immediately following the specified {@code messageId}.
     * If {@code messageId} is {@code null}, the page starts from the oldest message.
     * </p>
     *
     * @param sessionId the session identifier (must not be {@code null})
     * @param messageId the reference message ID; messages newer than this are returned;
     *                  if {@code null}, start from the oldest message
     * @param limit     maximum number of messages to return
     * @param cursor    an opaque cursor from a previous page result, used to continue paging;
     *                  may be {@code null} for the first page
     * @return a {@link PageResult} containing the messages and a cursor for the next page
     */
    PageResult<Message> loadMessagesAfter(SessionId sessionId, MessageId messageId, int limit, String cursor);

    /**
     * Retrieves all leaf message IDs in the conversation tree.
     * <p>
     * Leaf messages are those that have no child messages. They represent the
     * endpoints of all branches in the conversation.
     * </p>
     *
     * @param sessionId the session identifier (must not be {@code null})
     * @return a list of leaf message IDs (maybe empty)
     */
    List<MessageId> getLeafMessageIds(SessionId sessionId);

    /**
     * Deletes a subtree of messages rooted at the specified message.
     * <p>
     * If {@code inclusive} is {@code true}, the root message itself is also deleted.
     * Otherwise, only its descendants are removed. The operation must be atomic
     * and respect referential integrity (i.e., no orphaned messages should remain).
     * </p>
     *
     * @param sessionId     the session identifier (must not be {@code null})
     * @param rootMessageId the ID of the root message of the subtree to delete (must not be {@code null})
     * @param inclusive     if {@code true}, delete the root message as well; otherwise keep it
     * @return a set of deleted message IDs
     * @throws IllegalArgumentException if the root message does not exist
     */
    Set<MessageId> deleteMessages(SessionId sessionId, MessageId rootMessageId, boolean inclusive);

    /**
     * Deletes all messages belonging to the specified session.
     * <p>
     * This operation clears the entire conversation history. Use with caution.
     * </p>
     *
     * @param sessionId the session identifier (must not be {@code null})
     */
    void deleteMessages(SessionId sessionId);
}