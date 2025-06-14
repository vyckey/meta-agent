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

package org.metaagent.framework.core.mcp.client.factory;

import io.modelcontextprotocol.client.McpClient;

/**
 * Customizer interface for modifying the behavior of MCP clients.
 * Implementations can customize the sync and async specifications of the clients.
 *
 * @author vyckey
 */
public interface McpClientCustomizer {
    McpClientCustomizer DEFAULT = new McpClientCustomizer() {
    };

    /**
     * Customize the sync specification of the MCP client.
     *
     * @param name      the name of the client
     * @param syncSpec  the sync specification to customize
     */
    default void customize(String name, McpClient.SyncSpec syncSpec) {
    }

    /**
     * Customize the async specification of the MCP client.
     *
     * @param name      the name of the client
     * @param asyncSpec the async specification to customize
     */
    default void customize(String name, McpClient.AsyncSpec asyncSpec) {
    }
}
