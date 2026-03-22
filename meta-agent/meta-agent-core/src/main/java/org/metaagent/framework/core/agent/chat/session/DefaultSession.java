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

import lombok.EqualsAndHashCode;
import org.metaagent.framework.common.metadata.MetadataProvider;
import org.metaagent.framework.core.agent.chat.conversation.Conversation;
import org.metaagent.framework.core.agent.chat.conversation.DefaultConversation;

import java.time.Instant;
import java.util.Objects;

/**
 * DefaultSession is a default implementation of {@link Session}.
 *
 * @author vyckey
 */
@EqualsAndHashCode
public class DefaultSession implements Session {
    private final SessionId id;
    /**
     * The parent session ID, may be null.
     */
    private final SessionId parentId;
    private String name;
    private SessionStatus status = SessionStatus.IDLE;
    private MetadataProvider metadata;
    private final Instant createdAt;
    private Instant updatedAt;
    private Conversation conversation;

    protected DefaultSession(SessionId id, SessionId parentId, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.parentId = parentId;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");
    }

    protected DefaultSession(SessionId id, SessionId parentId,
                             String name, SessionStatus status, MetadataProvider metadata,
                             Instant createdAt, Instant updatedAt, Conversation conversation) {
        this(id, parentId, createdAt, updatedAt);
        this.name = Objects.requireNonNull(name, "name is required");
        this.status = Objects.requireNonNull(status, "status is required");
        this.metadata = Objects.requireNonNull(metadata, "metadata is required");
        this.conversation = conversation;
    }

    public static DefaultSession from(SessionId id, SessionId parentId,
                                      String name, SessionStatus status, MetadataProvider metadata,
                                      Instant createdAt, Instant updatedAt, Conversation conversation) {
        return new DefaultSession(id, parentId, name, status, metadata, createdAt, updatedAt, conversation);
    }

    public static DefaultSession create(SessionId id, SessionId parentId, String name) {
        DefaultSession session = new DefaultSession(id, parentId, Instant.now(), Instant.now());
        session.name = Objects.requireNonNull(name, "name is required");
        session.metadata = MetadataProvider.create();
        session.conversation = new DefaultConversation(id);
        return session;
    }


    @Override
    public SessionId id() {
        return id;
    }

    @Override
    public SessionId parentId() {
        return parentId;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name is required");
        this.updatedAt = Instant.now();
    }

    @Override
    public SessionStatus status() {
        return status;
    }

    @Override
    public void setStatus(SessionStatus status) {
        this.status = Objects.requireNonNull(status, "status is required");
        this.updatedAt = Instant.now();
    }

    @Override
    public Instant createdAt() {
        return createdAt;
    }

    @Override
    public Instant updatedAt() {
        return updatedAt;
    }

    @Override
    public MetadataProvider metadata() {
        return metadata;
    }

    @Override
    public Conversation conversation() {
        return conversation;
    }

    @Override
    public String toString() {
        return "DefaultSession{id=" + id + ", name='" + name + "', status=" + status + "}";
    }
}
