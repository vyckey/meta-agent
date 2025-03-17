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

package org.metaagent.framework.core.tool.mcp;

import com.fasterxml.jackson.core.type.TypeReference;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.ToolExecutionException;
import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.converter.ToolConverters;
import org.metaagent.framework.core.tool.definition.ToolDefinition;
import org.metaagent.framework.core.util.json.JsonObjectMapper;

import java.util.Map;

/**
 * description is here
 *
 * @author vyckey
 */
@Slf4j
public class McpTool implements Tool<Map<String, Object>, McpSchema.CallToolResult> {
    protected final McpSyncClient mcpSyncClient;
    protected final McpSchema.Tool toolSchema;
    protected final ToolDefinition toolDefinition;

    public McpTool(McpSyncClient mcpSyncClient, McpSchema.Tool toolSchema) {
        this.mcpSyncClient = mcpSyncClient;
        this.toolSchema = toolSchema;
        this.toolDefinition = buildToolDefinition(toolSchema);
    }

    protected ToolDefinition buildToolDefinition(McpSchema.Tool toolSchema) {
        String inputSchema = JsonObjectMapper.CAMEL_CASE.toJson(toolSchema.inputSchema());
        return ToolDefinition.builder(toolSchema.name())
                .description(toolSchema.description())
                .inputSchema(inputSchema)
                .outputSchema(McpSchema.CallToolResult.class)
                .build();
    }


    @Override
    public ToolDefinition getDefinition() {
        return toolDefinition;
    }

    @Override
    public ToolConverter<Map<String, Object>, McpSchema.CallToolResult> getConverter() {
        return ToolConverters.jsonConverter(new TypeReference<>() {
        });
    }

    @Override
    public McpSchema.CallToolResult run(ToolContext context, Map<String, Object> input) throws ToolExecutionException {
        String name = getDefinition().name();
        McpSchema.CallToolRequest toolRequest = new McpSchema.CallToolRequest(name, input);
        McpSchema.CallToolResult toolResult = mcpSyncClient.callTool(toolRequest);
        return toolResult;
    }

    @Override
    public String toString() {
        return "McpTool{name=\"" + getDefinition().name() + "\"}";
    }
}
