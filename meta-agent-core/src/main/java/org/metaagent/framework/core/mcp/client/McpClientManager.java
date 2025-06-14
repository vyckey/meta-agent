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

package org.metaagent.framework.core.mcp.client;

import com.google.common.collect.Maps;
import org.metaagent.framework.core.mcp.client.configure.McpClientProperties;
import org.metaagent.framework.core.mcp.client.configure.McpClientPropertiesParser;
import org.metaagent.framework.core.mcp.client.factory.McpClientFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Manager for Model Context Protocol (MCP) clients.
 *
 * @author vyckey
 */
public class McpClientManager {
    private static volatile McpClientManager instance;

    private final Map<String, UnifiedMcpClient> mcpClients;

    public McpClientManager(Map<String, UnifiedMcpClient> mcpClients) {
        this.mcpClients = Objects.requireNonNull(mcpClients);
    }

    public McpClientManager() {
        this(Maps.newHashMap());
    }

    public static McpClientManager getInstance() {
        if (instance == null) {
            synchronized (McpClientManager.class) {
                if (instance == null) {
                    McpClientManager manager = new McpClientManager();
                    manager.loadClients(McpClientProperties.DEFAULT_PROPERTIES_FILE);
                    instance = manager;
                }
            }
        }
        return instance;
    }

    public synchronized void loadClients(String configFile) {
        try {
            McpClientProperties properties = McpClientPropertiesParser.fromFile(configFile);
            Map<String, UnifiedMcpClient> newMcpClients = McpClientFactory.createMcpClients(properties);
            this.mcpClients.putAll(newMcpClients);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load MCP clients from properties file", e);
        }
    }

    public Set<String> getClientNames() {
        return mcpClients.keySet();
    }

    public UnifiedMcpClient getClient(String name) {
        return mcpClients.get(name);
    }

    public void addClient(UnifiedMcpClient mcpClient) {
        mcpClients.put(mcpClient.getName(), mcpClient);
    }

    public void removeClient(String name) {
        mcpClients.remove(name);
    }
}
