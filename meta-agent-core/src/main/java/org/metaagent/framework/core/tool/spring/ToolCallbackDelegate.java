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

package org.metaagent.framework.core.tool.spring;

import org.metaagent.framework.core.tool.DefaultToolContext;
import org.metaagent.framework.core.tool.Tool;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;

import java.util.Objects;

/**
 * Spring {@link ToolCallback} delegate.
 *
 * @author vyckey
 */
public class ToolCallbackDelegate implements ToolCallback {
    private final Tool<?, ?> tool;
    private final ToolDefinition toolDefinition;

    public ToolCallbackDelegate(Tool<?, ?> tool) {
        this.tool = Objects.requireNonNull(tool, "tool is required");
        this.toolDefinition = ToolDefinition.builder()
                .name(tool.getName())
                .description(tool.getDefinition().description())
                .inputSchema(tool.getDefinition().inputSchema())
                .build();
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return toolDefinition;
    }

    @Override
    public ToolMetadata getToolMetadata() {
        return ToolCallback.super.getToolMetadata();
    }

    @Override
    public String call(String toolInput) {
        return call(toolInput, null);
    }

    @Override
    public String call(String toolInput, ToolContext tooContext) {
        DefaultToolContext context = DefaultToolContext.builder().build();
        return tool.call(context, toolInput);
    }

    @Override
    public String toString() {
        return "ToolCallback{name=" + tool.getName() + "}";
    }
}
