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

package org.metaagent.framework.core.tool.config;

import com.google.common.collect.Sets;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record DefaultToolExecutionConfig(
        Path workingDirectory,
        Set<ToolPattern> allowedCommands,
        Set<ToolPattern> disallowedCommands
) implements ToolExecutionConfig {
    public DefaultToolExecutionConfig {
        Objects.requireNonNull(workingDirectory, "workingDirectory is required");
        if (allowedCommands == null) {
            allowedCommands = Sets.newHashSet();
        }
        if (disallowedCommands == null) {
            disallowedCommands = Sets.newHashSet();
        }
    }

    private DefaultToolExecutionConfig(Builder builder) {
        this(builder.workingDirectory, builder.allowedCommands, builder.disallowedCommands);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Set<ToolPattern> allowedTools() {
        return allowedCommands;
    }

    @Override
    public List<ToolPattern> allowedTools(String toolName) {
        return allowedCommands.stream().filter(toolPattern -> toolPattern.toolName().equals(toolName)).toList();
    }

    @Override
    public Set<ToolPattern> disallowedTools() {
        return disallowedCommands;
    }

    @Override
    public List<ToolPattern> disallowedTools(String toolName) {
        return disallowedCommands.stream().filter(toolPattern -> toolPattern.toolName().equals(toolName)).toList();
    }

    public static class Builder {
        private Path workingDirectory;
        private Set<ToolPattern> allowedCommands;
        private Set<ToolPattern> disallowedCommands;

        private Builder() {
        }

        public Builder workingDirectory(Path workingDirectory) {
            this.workingDirectory = workingDirectory;
            return this;
        }

        public Builder allowedCommands(Set<ToolPattern> allowedCommands) {
            this.allowedCommands = allowedCommands;
            return this;
        }

        public Builder disallowedCommands(Set<ToolPattern> disallowedCommands) {
            this.disallowedCommands = disallowedCommands;
            return this;
        }

        public DefaultToolExecutionConfig build() {
            return new DefaultToolExecutionConfig(this);
        }
    }
}
