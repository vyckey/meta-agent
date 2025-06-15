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

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpSyncClient;

import java.util.Objects;

/**
 * A unified client for the Model Context Protocol (MCP) that can operate in both
 *
 * @see McpSyncClient
 * @see McpAsyncClient
 */
public final class UnifiedMcpClient {
    private final String name;
    private final McpSyncClient mcpSyncClient;
    private final McpAsyncClient mcpAsyncClient;

    private UnifiedMcpClient(String name, McpSyncClient mcpSyncClient, McpAsyncClient mcpAsyncClient) {
        this.name = name;
        this.mcpSyncClient = mcpSyncClient;
        this.mcpAsyncClient = mcpAsyncClient;
    }

    public static UnifiedMcpClient from(String name, McpSyncClient mcpSyncClient) {
        return new UnifiedMcpClient(name, Objects.requireNonNull(mcpSyncClient), null);
    }

    public static UnifiedMcpClient from(String name, McpAsyncClient mcpAsyncClient) {
        return new UnifiedMcpClient(name, null, Objects.requireNonNull(mcpAsyncClient));
    }

    public String getName() {
        return name;
    }

    public boolean isSync() {
        return mcpSyncClient != null;
    }

    public McpSyncClient sync() {
        return mcpSyncClient;
    }

    public McpAsyncClient async() {
        return mcpAsyncClient;
    }

    @Override
    public String toString() {
        if (isSync()) {
            return "McpSyncClient{name='" + name + "'}";
        }
        return "McpAsyncClient{name='" + name + "'}";
    }
}
