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

package org.metaagent.framework.agents.mcp.client;

import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.metaagent.framework.core.mcp.client.McpClientManager;
import org.metaagent.framework.core.mcp.client.UnifiedMcpClient;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.tools.mcp.McpToolkit;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class McpClientTest {

    private McpClientManager mcpClientManager;

    @BeforeEach
    void setUp() {
        mcpClientManager = McpClientManager.getInstance();
    }

    @Disabled
    @Test
    void testAmapMcpClient() {
        UnifiedMcpClient mcpClient = mcpClientManager.getClient("amap-amap-sse");
        McpSchema.InitializeResult initializeResult = mcpClient.initialize().block();
        McpToolkit toolkit = McpToolkit.create("Amap", "", mcpClient);
        List<Tool<?, ?>> tools = toolkit.listTools();
        assertFalse(tools.isEmpty(), "Amap MCP Client should have tools");
    }
}
