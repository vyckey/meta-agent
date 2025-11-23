/*
 * MIT License
 *
 * Copyright (c) 2025 MetaAgent
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

package org.metaagent.framework.core.agent.chat.message;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.metaagent.framework.common.metadata.MapMetadataProvider;
import org.metaagent.framework.common.metadata.MetadataProvider;

import java.time.Instant;

/**
 * The abstract class for the message interface.
 *
 * @author vyckey
 */
public abstract class AbstractMessage implements Message {
    @JsonDeserialize(as = MessageIdValue.class)
    protected MessageId id;
    @JsonDeserialize(as = MapMetadataProvider.class)
    protected MetadataProvider metadata;
    protected Instant createdAt = Instant.now();

    @Override
    public MessageId getId() {
        if (id == null) {
            id = MessageId.random();
        }
        return id;
    }

    @Override
    public MetadataProvider getMetadata() {
        if (metadata == null) {
            metadata = MetadataProvider.create();
        }
        return metadata;
    }

    @Override
    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return getRole() + ": " + getContent();
    }
}
