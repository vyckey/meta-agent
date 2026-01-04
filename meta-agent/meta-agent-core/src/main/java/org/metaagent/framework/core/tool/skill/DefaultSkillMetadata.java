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

package org.metaagent.framework.core.tool.skill;

import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.common.metadata.ClassMetadataProvider;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link SkillMetadata}
 *
 * @author vyckey
 */
public class DefaultSkillMetadata extends ClassMetadataProvider implements SkillMetadata {
    private final String name;
    private final String description;
    private String version;
    private Path filePath;

    public DefaultSkillMetadata(String name, String description) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Skill name cannot be empty");
        }
        this.name = name;
        this.description = description != null ? description : "";
    }

    private DefaultSkillMetadata(Builder builder) {
        this(builder.name, builder.description);
        this.version = builder.version;
        this.filePath = builder.filePath;
        this.extendProperties.putAll(builder.extendProperties);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public String version() {
        return version;
    }

    @Override
    public Path filePath() {
        return filePath;
    }

    @Override
    public String toString() {
        return "SKILL - " + name + "\n" + description;
    }

    public static class Builder {
        private String name;
        private String description;
        private Path filePath;
        private String version;
        private final Map<String, Object> extendProperties = new HashMap<>(0);

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder filePath(Path filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder addExtendProperty(String key, Object value) {
            this.extendProperties.put(key, value);
            return this;
        }

        public Builder setExtendProperties(Map<String, Object> extendProperties) {
            this.extendProperties.putAll(extendProperties);
            return this;
        }

        public DefaultSkillMetadata build() {
            return new DefaultSkillMetadata(this);
        }
    }
}
