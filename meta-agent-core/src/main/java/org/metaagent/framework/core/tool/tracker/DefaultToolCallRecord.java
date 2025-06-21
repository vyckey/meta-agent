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

package org.metaagent.framework.core.tool.tracker;

import lombok.Getter;
import org.metaagent.framework.core.tool.ToolExecutionException;

import java.time.Instant;
import java.util.UUID;

/**
 * description is here
 *
 * @author vyckey
 */
@Getter
public class DefaultToolCallRecord implements ToolCallRecord {
    private final String id;
    private final String toolName;
    private final String toolInput;
    private final String toolOutput;
    private final ToolExecutionException exception;
    private final Instant startTime;
    private final Instant endTime;

    private DefaultToolCallRecord(Builder builder) {
        this.id = builder.id;
        this.toolName = builder.toolName;
        this.toolInput = builder.toolInput;
        this.toolOutput = builder.toolOutput;
        this.exception = builder.exception;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
    }

    @Override
    public String toString() {
        return "DefaultToolCallRecord{id:\"" + id + "\", toolName:\"" + toolName + "\", \"}";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String toolName;
        private String toolInput;
        private String toolOutput;
        private final Instant startTime;
        private Instant endTime;
        private ToolExecutionException exception;

        private Builder() {
            this.startTime = Instant.now();
        }

        public Builder id() {
            this.id = UUID.randomUUID().toString();
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder toolName(String toolName) {
            this.toolName = toolName;
            return this;
        }

        public Builder toolInput(String toolInput) {
            this.toolInput = toolInput;
            return this;
        }

        public Builder toolOutput(String toolOutput) {
            this.toolOutput = toolOutput;
            this.endTime = Instant.now();
            return this;
        }

        public Builder exception(ToolExecutionException exception) {
            this.exception = exception;
            this.endTime = Instant.now();
            return this;
        }

        public DefaultToolCallRecord build() {
            return new DefaultToolCallRecord(this);
        }
    }
}
