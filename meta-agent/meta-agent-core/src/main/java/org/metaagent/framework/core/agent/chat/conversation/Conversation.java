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

import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.MessageId;
import org.metaagent.framework.core.agent.chat.session.SessionId;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Represents the entire conversation history of a session, organized as a tree of messages.
 * Each message has a unique ID and an optional parent ID (null for root messages).
 * The conversation maintains a "current leaf" – the leaf message of the currently active branch.
 * Most operations (like appending, querying recent messages, or resetting) are performed
 * on the current branch, providing a linear view for convenience.
 *
 * <p>Implementations are expected to be thread-safe if accessed concurrently.
 * They may cache parts of the tree for performance and should collaborate with a
 * {@link ConversationStore} for persistence.
 *
 * @author vyckey
 */
public interface Conversation extends Iterable<Message>, Flushable, Closeable {

    /**
     * Returns the session ID associated with this conversation.
     *
     * @return the session ID
     */
    SessionId sessionId();

    /**
     * Checks whether the conversation contains any messages.
     *
     * @return {@code true} if there is at least one message, {@code false} otherwise
     */
    boolean isEmpty();

    // ========== Linear view operations (based on current branch) ==========

    /**
     * Appends a message to the end of the current branch.
     * If the message's parent ID is not set, it will be automatically set to the current branch leaf.
     * After appending, the new message becomes the current branch leaf.
     *
     * @param message the message to append (must not be null)
     * @throws IllegalArgumentException if the message's parent ID is explicitly set but does not
     *                                  match the current branch leaf or if the message already exists
     */
    void appendMessage(Message message);

    /**
     * Updates an existing message on the current branch.
     * The message's ID must exist and belong to the current branch.
     *
     * @param newMessage the new message content (its ID must match {@code messageId})
     * @throws IllegalArgumentException if the message does not exist, is not on the current branch,
     *                                  or the ID mismatch
     */
    void updateMessage(Message newMessage);

    /**
     * Deletes all messages on the current branch after the given message ID.
     * The message with the given ID must exist and belong to the current branch.
     *
     * @param messageId the ID of the message after which to delete messages
     * @param inclusive whether to include the message with the given ID in the deletion
     * @throws IllegalArgumentException if the message does not exist, is not on the current branch,
     *                                  or the ID mismatch
     */
    void deleteMessagesAfter(MessageId messageId, boolean inclusive);

    /**
     * Returns the last message (the leaf) of the current branch.
     *
     * @return an {@link Optional} containing the last message, or empty if the conversation is empty
     */
    Optional<Message> lastMessage();

    /**
     * Returns the last {@code count} messages from the current branch, in chronological order
     * (oldest to newest). If the branch has fewer than {@code count} messages, all messages are returned.
     *
     * @param count the number of messages to retrieve (must be non-negative)
     * @return a list of the last {@code count} messages, never null
     * @throws IllegalArgumentException if {@code count} is negative
     */
    List<Message> lastMessages(int count);

    /**
     * Returns the message with the given ID.
     *
     * @param messageId the ID of the message to retrieve
     * @return the message with the given ID, or empty if no such message exists
     */
    Optional<Message> getMessage(MessageId messageId);

    /**
     * Finds all messages on the current branch that match the given predicate.
     * The search can be performed in forward (root to leaf) or reverse (leaf to root) order.
     *
     * @param predicate the condition to match
     * @param forward   if {@code true}, search from root to leaf; otherwise from leaf to root
     * @return a list of matching messages (possibly empty)
     */
    List<Message> findMessages(Predicate<Message> predicate, boolean forward);

    /**
     * Finds the first message on the current branch that matches the given predicate.
     * The search can be performed in forward or reverse order.
     *
     * @param predicate the condition to match
     * @param forward   if {@code true}, search from root to leaf; otherwise from leaf to root
     * @return an {@link Optional} containing the first matching message, or empty if none found
     */
    Optional<Message> findMessage(Predicate<Message> predicate, boolean forward);

    /**
     * Returns an iterator over the messages in the current branch,
     * in order from the root message to the current leaf.
     * The iterator supports {@link Iterator#remove()} only if permitted by the implementation.
     *
     * @return a forward iterator for the current branch
     */
    @Override
    Iterator<Message> iterator();

    /**
     * Returns an iterator over the messages in the current branch,
     * in reverse order from the current leaf back to the root.
     * The iterator does not support {@link Iterator#remove()}.
     *
     * @return a reverse iterator for the current branch
     */
    Iterator<Message> reverse();

    // ========== Branch management ==========

    /**
     * Returns the leaf message ID of the current active branch.
     *
     * @return an {@link Optional} containing the current leaf ID, or empty if the conversation is empty
     */
    Optional<MessageId> currentLeafId();

    /**
     * Switches the current branch to another leaf message.
     * The specified message must be a leaf node (i.e., have no children) belonging to this conversation.
     * After switching, all linear view operations will operate on the newly selected branch.
     *
     * @param leafMessageId the ID of the leaf message to become the new current leaf
     * @throws IllegalArgumentException if the message does not exist, is not a leaf,
     *                                  or does not belong to this conversation
     */
    void switchToBranch(MessageId leafMessageId);

    /**
     * Returns the IDs of all leaf messages in the conversation.
     * Leaf messages are those with no children.
     *
     * @return a list of message IDs, never null (maybe empty)
     */
    List<MessageId> listLeafMessageIds();

    /**
     * Returns the full path from the root to the specified leaf, inclusive.
     * The path is ordered from root (first) to leaf (last).
     *
     * @param leaf the ID of the leaf message (must belong to this conversation)
     * @return a list of messages forming the path
     * @throws IllegalArgumentException if the leaf does not exist
     */
    List<Message> getPathFromRoot(MessageId leaf);

    /**
     * Creates a new branch starting from the specified parent message.
     * The new branch will be the current branch, and the specified message will become the current leaf.
     *
     * @param parentMessageId the ID of the parent message for the new branch
     * @param message         the first message of the new branch
     * @throws IllegalArgumentException if the parent message does not exist or is not a leaf
     */
    void fork(MessageId parentMessageId, Message message);

    /**
     * Creates a copy of this conversation under a new session.
     * All messages in current branch are duplicated with new IDs generated by the provided supplier.
     * The new conversation will only have a path but independent persistence.
     *
     * @param newSessionId       the ID of the new session
     * @param messageIdGenerator a supplier that generates new unique message IDs
     * @return a new {@code Conversation} instance representing the forked conversation
     */
    Conversation copy(SessionId newSessionId, Supplier<MessageId> messageIdGenerator);

    /**
     * Removes all messages from this conversation.
     * This operation is irreversible and will also clear all branches.
     */
    void clear();

    /**
     * Flushes any pending changes to the underlying storage.
     * This method is called automatically by the framework at appropriate times,
     * but can be invoked manually to ensure durability.
     *
     * @throws java.io.IOException if an I/O error occurs
     */
    @Override
    void flush() throws IOException;

    /**
     * Releases any resources held by this conversation (e.g., caches, open connections).
     * After closing, the conversation should not be used further.
     *
     * @throws IOException if an error occurs during closing
     */
    @Override
    void close() throws IOException;
}