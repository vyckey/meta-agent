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
import org.metaagent.framework.core.tool.manager.DefaultToolManager;
import org.metaagent.framework.core.tool.manager.ToolManager;
import org.metaagent.framework.core.tool.tracker.ToolCallTracker;

/**
 * description is here
 *
 * @author vyckey
 */
@Getter
public class DefaultToolContext implements ToolContext {
    protected ToolManager toolManager;
    protected ToolCallTracker toolCallTracker;

    protected DefaultToolContext(Builder builder) {
        this.toolManager = builder.toolManager;
        this.toolCallTracker = builder.toolCallTracker;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ToolManager toolManager;
        private ToolCallTracker toolCallTracker;

        public Builder toolManager(ToolManager toolManager) {
            this.toolManager = toolManager;
            return this;
        }

        public Builder toolCallTracker(ToolCallTracker toolCallTracker) {
            this.toolCallTracker = toolCallTracker;
            return this;
        }

        public DefaultToolContext build() {
            if (toolManager == null) {
                this.toolManager = DefaultToolManager.getInstance();
            }
            return new DefaultToolContext(this);
        }
    }
}
