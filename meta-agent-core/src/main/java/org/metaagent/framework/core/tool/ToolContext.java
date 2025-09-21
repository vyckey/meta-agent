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

import org.metaagent.framework.core.common.security.SecurityLevel;
import org.metaagent.framework.core.tool.config.ToolConfig;
import org.metaagent.framework.core.util.abort.AbortSignal;

/**
 * ToolContext provides the context for tool execution,
 *
 * @author vyckey
 */
public interface ToolContext {
    /**
     * Gets the tool executor for executing tools.
     *
     * @return the tool executor
     */
    static ToolContext create() {
        return DefaultToolContext.builder().build();
    }

    /**
     * Creates a new builder for constructing a ToolContext.
     *
     * @return a new ToolContext builder
     */
    static ToolContextBuilder builder() {
        return DefaultToolContext.builder();
    }

    /**
     * Gets the tool configuration.
     *
     * @return the tool configuration
     */
    ToolConfig getToolConfig();

    /**
     * Gets the security level for the tool execution.
     *
     * @return the security level
     */
    SecurityLevel getSecurityLevel();

    /**
     * Gets the abort signal for managing tool execution aborts.
     *
     * @return the abort signal
     */
    AbortSignal getAbortSignal();

}
