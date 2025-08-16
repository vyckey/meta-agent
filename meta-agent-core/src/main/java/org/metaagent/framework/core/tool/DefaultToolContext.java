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

package org.metaagent.framework.core.tool;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.core.util.abort.AbortController;
import org.metaagent.framework.core.util.abort.AbortSignal;

import java.nio.file.Path;

/**
 * Default implementation of {@link ToolContext} interface.
 *
 * @author vyckey
 */
@Getter
public class DefaultToolContext implements ToolContext {
    private final Path workingDirectory;
    private final AbortSignal abortSignal;

    protected DefaultToolContext(Builder builder) {
        this.workingDirectory = builder.workingDirectory;
        this.abortSignal = builder.abortSignal;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder implements ToolContextBuilder {
        private Path workingDirectory;
        private AbortSignal abortSignal;

        @Override
        public ToolContextBuilder workingDirectory(Path workingDirectory) {
            this.workingDirectory = workingDirectory;
            return this;
        }

        @Override
        public ToolContextBuilder abortSignal(AbortSignal abortSignal) {
            this.abortSignal = abortSignal;
            return this;
        }

        @Override
        public DefaultToolContext build() {
            if (workingDirectory == null) {
                if (StringUtils.isNotEmpty(System.getenv("CWD"))) {
                    workingDirectory = Path.of(System.getenv("CWD"));
                } else {
                    workingDirectory = Path.of(".");
                }
            }
            if (abortSignal == null) {
                this.abortSignal = AbortController.global().signal();
            }
            return new DefaultToolContext(this);
        }
    }
}
