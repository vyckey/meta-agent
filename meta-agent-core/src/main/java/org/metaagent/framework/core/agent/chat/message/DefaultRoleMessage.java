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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.metaagent.framework.core.common.media.MediaResource;
import org.metaagent.framework.core.common.metadata.MetadataProvider;

import java.util.List;
import java.util.Objects;

/**
 * RoleMessage is a message that contains role, content and media resources.
 *
 * @author vyckey
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class DefaultRoleMessage extends AbstractMessage implements RoleMessage {
    private final String role;
    private final String content;
    private final List<MediaResource> media;

    public DefaultRoleMessage(String role, String content, List<MediaResource> media, MetadataProvider metadata) {
        this.role = Objects.requireNonNull(role, "role is required");
        this.content = Objects.requireNonNull(content, "content is required");
        this.media = Objects.requireNonNull(media, "media is required");
        this.metadata = metadata;
    }

    public DefaultRoleMessage(String role, String content, List<MediaResource> media) {
        this(role, content, media, MetadataProvider.create());
    }

    @Override
    public String toString() {
        return getRole() + ": " + getContent();
    }
}
