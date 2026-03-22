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

package org.metaagent.framework.core.agent.chat.session;

import org.metaagent.framework.common.metadata.MetadataProvider;
import org.metaagent.framework.core.agent.chat.conversation.Conversation;

import java.time.Instant;

/**
 * Represents a session in the chat.
 * <p>
 * A session is a container for a conversation and its associated metadata.
 * It maintains the relationship between a conversation and its attributes
 * such as name, status, and timestamps. Sessions can be organized hierarchically
 * through parent-child relationships, allowing for conversation branching and forking.
 * </p>
 *
 * <p>
 * Implementations of this interface are expected to be lightweight proxies
 * that delegate to a persistent storage, ensuring thread-safety and consistency.
 * The mutable fields ({@link #setName(String)}, {@link #setStatus(SessionStatus)})
 * should automatically update the {@link #updatedAt()} timestamp.
 * </p>
 *
 * @author vyckey
 * @see Conversation
 * @see SessionStatus
 * @see MetadataProvider
 */
public interface Session {

    /**
     * Returns the unique identifier of this session.
     *
     * @return the session ID, never {@code null}
     */
    SessionId id();

    /**
     * Returns the identifier of the parent session, if this session is a child.
     * <p>
     * A {@code null} return value indicates that this session is a root session
     * with no parent. Parent-child relationships enable conversation forking and
     * branching scenarios.
     * </p>
     *
     * @return the parent session ID, or {@code null} if this session has no parent
     */
    SessionId parentId();

    /**
     * Returns the human-readable name of this session.
     * <p>
     * The name is used for display and identification purposes.
     * It can be modified using {@link #setName(String)}.
     * </p>
     *
     * @return the session name, never {@code null}
     */
    String name();

    /**
     * Updates the name of this session.
     * <p>
     * This operation automatically updates the {@link #updatedAt()} timestamp
     * to reflect the modification time.
     * </p>
     *
     * @param name the new session name, must not be {@code null} or empty
     * @throws IllegalArgumentException if the provided name is {@code null} or empty
     */
    void setName(String name);

    /**
     * Returns the current lifecycle status of this session.
     * <p>
     * The status determines whether the session is active, archived, or deleted,
     * affecting its visibility and availability for operations.
     * </p>
     *
     * @return the session status, never {@code null}
     * @see SessionStatus
     */
    SessionStatus status();

    /**
     * Updates the status of this session.
     * <p>
     * This operation automatically updates the {@link #updatedAt()} timestamp.
     * Implementations may enforce state transition rules (e.g., cannot transition
     * directly from ACTIVE to DELETED without going through ARCHIVED).
     * </p>
     *
     * @param status the new session status, must not be {@code null}
     * @throws IllegalArgumentException if the provided status is {@code null}
     * @throws IllegalStateException    if the requested status transition is not allowed
     */
    void setStatus(SessionStatus status);

    /**
     * Returns the timestamp when this session was created.
     * <p>
     * This value is immutable and set at session creation time.
     * </p>
     *
     * @return the creation timestamp, never {@code null}
     */
    Instant createdAt();

    /**
     * Returns the timestamp of the last modification to this session.
     * <p>
     * This timestamp is automatically updated whenever any mutable field
     * ({@link #setName(String)}, {@link #setStatus(SessionStatus)}) is modified.
     * It may also be updated indirectly through conversation changes.
     * </p>
     *
     * @return the last modification timestamp, never {@code null}
     */
    Instant updatedAt();

    /**
     * Returns a provider for accessing custom metadata associated with this session.
     * <p>
     * The metadata provider allows storing and retrieving arbitrary key-value pairs
     * for extensibility without modifying the core session schema. This can be used
     * for features like tags, custom properties, UI state, or integration data.
     * </p>
     *
     * @return the metadata provider for this session, never {@code null}
     * @see MetadataProvider
     */
    MetadataProvider metadata();

    /**
     * Returns the conversation associated with this session.
     * <p>
     * The conversation contains the actual message history, supporting branching,
     * forking, and linear view operations. The conversation is uniquely tied to
     * this session and shares its lifecycle.
     * </p>
     *
     * <p>
     * Changes to the conversation (e.g., appending messages, switching branches)
     * are managed through the {@link Conversation} interface and are automatically
     * persisted according to the implementation's strategy.
     * </p>
     *
     * @return the conversation object, never {@code null}
     * @see Conversation
     */
    Conversation conversation();
}