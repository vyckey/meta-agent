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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.manager.ToolChangeListener;
import org.metaagent.framework.core.tool.toolkit.Toolkit;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Model Context Protocol (MCP) Toolkit
 *
 * @author vyckey
 */
@Slf4j
public class McpToolkit implements Toolkit {
    protected final String name;
    protected String description;
    protected final McpSyncClient mcpSyncClient;
    protected final Map<String, McpTool> mcpToolCache;
    protected final List<ToolChangeListener> listeners = Lists.newArrayList();

    public McpToolkit(String name, String description,
                      McpSyncClient mcpSyncClient, Map<String, McpTool> mcpToolCache) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Toolkit name is required");
        }
        this.name = name;
        this.description = description;
        this.mcpSyncClient = Objects.requireNonNull(mcpSyncClient, "McpSyncClient is required");
        this.mcpToolCache = Objects.requireNonNull(mcpToolCache, "McpToolCache is required");
    }

    public McpToolkit(String name, String description, McpSyncClient mcpSyncClient) {
        this(name, description, mcpSyncClient, Maps.newConcurrentMap());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public List<Tool<?, ?>> listTools() {
        return Lists.newArrayList(mcpToolCache.values());
    }

    @Override
    public Tool<?, ?> getTool(String name) {
        return mcpToolCache.get(name);
    }

    public void loadTools() {
        mcpSyncClient.ping();
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
        } while (StringUtils.isNotEmpty(nextCursor));
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

    protected void notifyChangeListeners(Tool<?, ?> tool, ToolChangeListener.EventType eventType) {
        for (ToolChangeListener listener : listeners) {
            try {
                listener.onToolChange(tool, eventType);
            } catch (Exception e) {
                log.error("Failed to notify tool change listener", e);
            }
        }
    }

    @Override
    public String toString() {
        return "McpToolkit{name=\"" + name + "\"}";
    }
}
