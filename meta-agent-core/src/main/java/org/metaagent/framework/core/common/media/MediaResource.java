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

package org.metaagent.framework.core.common.media;

import org.springframework.util.MimeType;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

/**
 * Media resource.
 *
 * @author vyckey
 */
public record MediaResource(MimeType mimeType, URI uri, String name, Object data) {
    public MediaResource(MimeType mimeType, URI uri, String name, Object data) {
        this.mimeType = Objects.requireNonNull(mimeType, "Mime type cannot be null");
        this.uri = Objects.requireNonNull(uri, "URI cannot be null");
        this.name = name != null ? name : uri.getPath().substring(uri.getPath().lastIndexOf('/') + 1);
        this.data = data;
    }

    public MediaResource(MimeType mimeType, URI uri) {
        this(mimeType, uri, null, null);
    }

    public MediaResource(MimeType mimeType, File file) {
        this(mimeType, file.toURI(), file.getName(), file);
    }

    private MediaResource(Builder builder) {
        this(builder.mimeType, builder.uri, builder.name, builder.data);
    }

    public String getStringData() {
        if (data instanceof String) {
            return data.toString();
        }
        return null;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return mimeType + ": [" + name + "](" + uri + ")";
    }

    public static class Builder {
        private MimeType mimeType;
        private URI uri;
        private String name;
        private Object data;

        public Builder mimeType(MimeType mimeType) {
            this.mimeType = Objects.requireNonNull(mimeType, "mimeType is required");
            return this;
        }

        public Builder uri(URI uri) {
            this.uri = Objects.requireNonNull(uri, "uri is required");
            return this;
        }

        public Builder url(URL url) {
            try {
                this.uri = Objects.requireNonNull(url, "URL is required.").toURI();
                this.data = url;
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid URL: " + url);
            }
            return this;
        }

        public Builder name(String name) {
            this.name = Objects.requireNonNull(name, "name is required");
            return this;
        }

        public Builder data(Object data) {
            this.data = Objects.requireNonNull(data, "data is required");
            return this;
        }

        public MediaResource build() {
            return new MediaResource(this);
        }
    }
}
