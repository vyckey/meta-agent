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

package org.metaagent.framework.core.tool.tools.mcp;

import com.google.common.collect.Maps;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.core.mcp.client.UnifiedMcpClient;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.manager.ToolChangeListener;
import org.metaagent.framework.core.tool.toolkit.DefaultToolkit;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Model Context Protocol (MCP) Toolkit
 *
 * @author vyckey
 * @see McpSyncClient
 * @see McpAsyncClient
 */
public abstract class McpToolkit extends DefaultToolkit {
    protected McpToolkit(String name, String description, Map<String, McpTool> mcpTools) {
        super(name, description, asToolMap(mcpTools));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Tool<?, ?>> asToolMap(Map<String, McpTool> mcpTools) {
        Map<String, ?> toolMap = mcpTools;
        return (Map<String, Tool<?, ?>>) toolMap;
    }

    public static McpToolkit sync(String name, String description, McpSyncClient mcpSyncClient) {
        return new McpSyncClientToolkit(name, description, mcpSyncClient);
    }

    public static McpToolkit async(String name, String description, McpAsyncClient mcpAsyncClient) {
        return new McpAsyncClientToolkit(name, description, mcpAsyncClient);
    }

    public static McpToolkit create(String name, String description, UnifiedMcpClient mcpClient) {
        if (mcpClient.isSync()) {
            return new McpSyncClientToolkit(name, description, mcpClient.sync());
        } else {
            return new McpAsyncClientToolkit(name, description, mcpClient.async());
        }
    }

    @Override
    public void addTool(Tool<?, ?> tool) {
        throw new UnsupportedOperationException("McpToolkit does not support adding tools");
    }

    @Override
    public void addTools(Tool<?, ?>... tools) {
        throw new UnsupportedOperationException("McpToolkit does not support adding tools");
    }

    @Override
    public void removeTool(String name) {
        throw new UnsupportedOperationException("McpToolkit does not support removing tools");
    }

    @Override
    public void removeTool(Tool<?, ?> tool) {
        throw new UnsupportedOperationException("McpToolkit does not support removing tools");
    }

    protected void onToolsChange(List<McpSchema.Tool> tools, Function<McpSchema.Tool, McpTool> toolCreator) {
        for (McpSchema.Tool tool : tools) {
            McpTool mcpTool = toolCreator.apply(tool);
            McpTool oldTool = (McpTool) this.tools.put(mcpTool.getName(), mcpTool);
            if (oldTool != null) {
                notifyChangeListeners(mcpTool, ToolChangeListener.EventType.UPDATED);
            } else {
                notifyChangeListeners(mcpTool, ToolChangeListener.EventType.ADDED);
            }
        }
    }

    @Override
    public String toString() {
        return "McpToolkit{name=\"" + name + "\"}";
    }

    static class McpSyncClientToolkit extends McpToolkit {
        protected final McpSyncClient mcpSyncClient;

        McpSyncClientToolkit(String name, String description,
                             McpSyncClient mcpSyncClient, Map<String, McpTool> mcpToolCache) {
            super(name, description, mcpToolCache);
            this.mcpSyncClient = Objects.requireNonNull(mcpSyncClient, "McpSyncClient is required");
        }

        McpSyncClientToolkit(String name, String description, McpSyncClient mcpSyncClient) {
            this(name, description, mcpSyncClient, Maps.newConcurrentMap());
        }

        @Override
        public void loadTools() {
            mcpSyncClient.ping();
            String nextCursor = null;
            do {
                McpSchema.ListToolsResult toolsResult = (nextCursor == null)
                        ? mcpSyncClient.listTools() : mcpSyncClient.listTools(nextCursor);
                List<McpSchema.Tool> tools = toolsResult.tools();
                for (McpSchema.Tool tool : tools) {
                    McpTool mcpTool = new McpTool(mcpSyncClient, tool);
                    this.tools.put(mcpTool.getDefinition().name(), mcpTool);
                }
                nextCursor = toolsResult.nextCursor();
            } while (StringUtils.isNotEmpty(nextCursor));
        }

        public void onToolsChange(List<McpSchema.Tool> tools) {
            onToolsChange(tools, toolSchema -> new McpTool(mcpSyncClient, toolSchema));
        }
    }

    static class McpAsyncClientToolkit extends McpToolkit {
        protected final McpAsyncClient mcpAsyncClient;

        McpAsyncClientToolkit(String name, String description,
                              McpAsyncClient mcpAsyncClient, Map<String, McpTool> mcpToolCache) {
            super(name, description, mcpToolCache);
            this.mcpAsyncClient = Objects.requireNonNull(mcpAsyncClient, "McpSyncClient is required");
        }

        McpAsyncClientToolkit(String name, String description, McpAsyncClient mcpAsyncClient) {
            this(name, description, mcpAsyncClient, Maps.newConcurrentMap());
        }

        @Override
        public void loadTools() {
            mcpAsyncClient.ping().block();

            String nextCursor = null;
            do {
                Mono<McpSchema.ListToolsResult> toolsResultMono = (nextCursor == null)
                        ? mcpAsyncClient.listTools() : mcpAsyncClient.listTools(nextCursor);
                McpSchema.ListToolsResult toolsResult = toolsResultMono.block();
                List<McpSchema.Tool> tools = toolsResult.tools();
                for (McpSchema.Tool tool : tools) {
                    McpTool mcpTool = new McpTool(mcpAsyncClient, tool);
                    this.tools.put(mcpTool.getDefinition().name(), mcpTool);
                }
                nextCursor = toolsResult.nextCursor();
            } while (StringUtils.isNotEmpty(nextCursor));
        }

        public void onToolsChange(List<McpSchema.Tool> tools) {
            onToolsChange(tools, toolSchema -> new McpTool(mcpAsyncClient, toolSchema));
        }
    }
}
