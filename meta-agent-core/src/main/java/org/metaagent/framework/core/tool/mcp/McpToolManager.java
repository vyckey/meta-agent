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

import com.google.common.collect.Maps;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.ClientMcpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.manager.AbstractToolManager;
import org.metaagent.framework.core.tool.manager.ToolChangeListener;
import org.metaagent.framework.core.tool.manager.ToolManager;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * MCP Tool Manager
 *
 * @author vyckey
 */
public class McpToolManager extends AbstractToolManager implements ToolManager {
    protected final McpSyncClient mcpSyncClient;
    protected final Map<String, McpTool> mcpToolCache;

    public McpToolManager(McpSyncClient mcpSyncClient, Map<String, McpTool> mcpToolCache) {
        this.mcpSyncClient = Objects.requireNonNull(mcpSyncClient, "McpSyncClient is required");
        this.mcpToolCache = Objects.requireNonNull(mcpToolCache, "McpToolCache is required");
    }

    public McpToolManager(McpSyncClient mcpSyncClient) {
        this(mcpSyncClient, Maps.newConcurrentMap());
    }

    public static McpToolManager create(McpClient.SyncSpec syncSpec) {
        McpToolManager[] toolManagerHolder = new McpToolManager[1];
        McpSyncClient syncClient = syncSpec
                .toolsChangeConsumer(tools -> toolManagerHolder[0].onToolsChange(tools))
                .build();
        McpToolManager mcpToolManager = new McpToolManager(syncClient);
        toolManagerHolder[0] = mcpToolManager;
        return mcpToolManager;
    }

    public static McpToolManager create(ClientMcpTransport transport) {
        McpClient.SyncSpec syncSpec = McpClient.sync(transport)
                .requestTimeout(Duration.ofSeconds(30));
        return create(syncSpec);
    }

    public static McpToolManager httpSse(String baseUri) {
        HttpClientSseClientTransport transport = new HttpClientSseClientTransport(baseUri);
        return create(transport);
    }

    public static McpToolManager stdio(ServerParameters parameters) {
        StdioClientTransport transport = new StdioClientTransport(parameters);
        return create(transport);
    }

    @Override
    public Set<String> getToolNames() {
        return mcpToolCache.keySet();
    }

    public void loadTools() {
        String nextCursor = null;
        do {
            McpSchema.ListToolsResult toolsResult = (nextCursor == null)
                    ? mcpSyncClient.listTools() : mcpSyncClient.listTools(nextCursor);
            List<McpSchema.Tool> tools = toolsResult.tools();
            for (McpSchema.Tool tool : tools) {
                McpTool mcpTool = new McpTool(mcpSyncClient, tool);
                mcpToolCache.put(mcpTool.getDefinition().name(), mcpTool);
            }
            nextCursor = toolsResult.nextCursor();
        } while (StringUtils.isEmpty(nextCursor));
    }

    public void onToolsChange(List<McpSchema.Tool> tools) {
        for (McpSchema.Tool tool : tools) {
            McpTool mcpTool = new McpTool(mcpSyncClient, tool);
            McpTool oldTool = mcpToolCache.put(mcpTool.getName(), mcpTool);
            if (oldTool != null) {
                notifyChangeListeners(mcpTool, ToolChangeListener.EventType.UPDATED);
            } else {
                notifyChangeListeners(mcpTool, ToolChangeListener.EventType.ADDED);
            }
        }
    }

    @Override
    public boolean hasTool(String name) {
        return mcpToolCache.containsKey(name);
    }

    @Override
    public Tool<?, ?> getTool(String name) {
        return mcpToolCache.get(name);
    }

    @Override
    public void addTool(Tool<?, ?> tool) {
        throw new UnsupportedOperationException("Unsupported operation for MCP tool manager");
    }

    @Override
    public void removeTool(String name) {
        throw new UnsupportedOperationException("Unsupported operation for MCP tool manager");
    }
}
