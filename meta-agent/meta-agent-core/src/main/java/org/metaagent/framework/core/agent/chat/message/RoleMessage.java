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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.metaagent.framework.common.content.MediaResource;
import org.metaagent.framework.common.metadata.MetadataProvider;

import java.util.List;
import java.util.Objects;

/**
 * RoleMessage is a message that contains role, content and media resources.
 *
 * @author vyckey
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class RoleMessage extends AbstractMessage implements Message {
    public static final String ROLE_USER = "user";
    public static final String ROLE_ASSISTANT = "assistant";

    private final String role;
    private final String content;
    private final List<MediaResource> media;

    @JsonCreator
    public RoleMessage(@JsonProperty("role") String role,
                       @JsonProperty("content") String content,
                       @JsonProperty("media") List<MediaResource> media,
                       @JsonProperty("metadata") MetadataProvider metadata) {
        this.role = Objects.requireNonNull(role, "role is required");
        this.content = Objects.requireNonNull(content, "content is required");
        this.media = Objects.requireNonNull(media, "media is required");
        this.metadata = metadata;
    }

    public RoleMessage(String role, String content, MetadataProvider metadata) {
        this(role, content, List.of(), metadata);
    }

    public static RoleMessage user(String content) {
        return new RoleMessage(ROLE_USER, content, MetadataProvider.empty());
    }

    public static RoleMessage user(String content, MetadataProvider metadata) {
        return new RoleMessage(ROLE_USER, content, metadata);
    }

    public static RoleMessage user(String content, List<MediaResource> media, MetadataProvider metadata) {
        return new RoleMessage(ROLE_USER, content, media, metadata);
    }

    public static RoleMessage assistant(String content) {
        return new RoleMessage(ROLE_ASSISTANT, content, MetadataProvider.empty());
    }

    public static RoleMessage assistant(String content, MetadataProvider metadata) {
        return new RoleMessage(ROLE_ASSISTANT, content, metadata);
    }

    public static RoleMessage assistant(String content, List<MediaResource> media, MetadataProvider metadata) {
        return new RoleMessage(ROLE_ASSISTANT, content, media, metadata);
    }

    @Override
    public String toString() {
        return getRole() + ": " + getContent();
    }
}
