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

package org.metaagent.framework.core.tool.executor;

import lombok.Getter;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.listener.ToolExecutionListenerRegistry;
import org.metaagent.framework.core.tool.manager.ToolManager;
import org.metaagent.framework.core.tool.tracker.ToolCallTracker;

import java.util.concurrent.Executor;

@Getter
public class DefaultToolExecutorContext implements ToolExecutorContext {
    private final ToolManager toolManager;
    private final ToolExecutionListenerRegistry toolListenerRegistry;
    private final ToolCallTracker toolCallTracker;
    private final ToolContext toolContext;
    private final Executor executor;

    private DefaultToolExecutorContext(DefaultToolExecutorContext.Builder builder) {
        this.toolManager = builder.toolManager;
        this.toolListenerRegistry = builder.toolListenerRegistry;
        this.toolCallTracker = builder.toolCallTracker;
        this.toolContext = builder.toolContext;
        this.executor = builder.executor;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder implements ToolExecutorContextBuilder {
        private ToolManager toolManager;
        private ToolExecutionListenerRegistry toolListenerRegistry;
        private ToolCallTracker toolCallTracker;
        private ToolContext toolContext;
        private Executor executor;

        @Override
        public Builder toolManager(ToolManager toolManager) {
            this.toolManager = toolManager;
            return this;
        }

        @Override
        public Builder toolCallTracker(ToolCallTracker toolCallTracker) {
            this.toolCallTracker = toolCallTracker;
            return this;
        }

        @Override
        public Builder toolListenerRegistry(ToolExecutionListenerRegistry toolListenerRegistry) {
            this.toolListenerRegistry = toolListenerRegistry;
            return this;
        }

        @Override
        public Builder toolContext(ToolContext toolContext) {
            this.toolContext = toolContext;
            return this;
        }

        @Override
        public Builder executor(Executor executor) {
            this.executor = executor;
            return this;
        }

        @Override
        public DefaultToolExecutorContext build() {
            if (toolManager == null) {
                this.toolManager = ToolManager.create();
            }
            if (toolCallTracker == null) {
                this.toolCallTracker = ToolCallTracker.empty();
            }
            if (toolListenerRegistry == null) {
                this.toolListenerRegistry = ToolExecutionListenerRegistry.DEFAULT;
            }
            if (toolContext == null) {
                this.toolContext = ToolContext.create();
            }
            return new DefaultToolExecutorContext(this);
        }
    }

}
