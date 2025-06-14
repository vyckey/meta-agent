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

import com.google.common.collect.Maps;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.metaagent.framework.core.mcp.client.UnifiedMcpClient;
import org.metaagent.framework.core.mcp.client.configure.McpClientCommonProperties;
import org.metaagent.framework.core.mcp.client.configure.McpClientProperties;
import org.metaagent.framework.core.mcp.client.configure.McpSseClientProperties;
import org.metaagent.framework.core.mcp.client.configure.McpStdioClientProperties;

import java.util.Map;

/**
 * Model Context Protocol (MCP) Client Factory
 *
 * @see McpClient
 * @see McpSyncClient
 * @see McpAsyncClient
 */
public abstract class McpClientFactory {
    public static McpClient.SyncSpec syncClient(McpClientTransport transport) {
        return McpClient.sync(transport);
    }

    public static McpClient.AsyncSpec asyncClient(McpClientTransport transport) {
        return McpClient.async(transport);
    }


    public static Map<String, UnifiedMcpClient> createMcpClients(McpClientProperties clientProperties) {
        return createMcpClients(clientProperties, McpClientCustomizer.DEFAULT);
    }

    public static Map<String, UnifiedMcpClient> createMcpClients(McpClientProperties clientProperties, McpClientCustomizer customizer) {
        Map<String, UnifiedMcpClient> clients = Maps.newHashMap();
        if (!clientProperties.isEnabled()) {
            return clients;
        }

        McpSseClientProperties sseProperties = clientProperties.getSse();
        if (sseProperties.isEnabled()) {
            Map<String, McpSseClientProperties.SseParameters> sseConnections = sseProperties.getConnections();
            Map<String, McpClientTransport> sseTransports = Maps.newHashMap();
            for (Map.Entry<String, McpSseClientProperties.SseParameters> entry : sseConnections.entrySet()) {
                HttpClientSseClientTransport transport = sseClientTransport(entry.getValue());
                sseTransports.put(entry.getKey(), transport);
            }
            clients.putAll(createMcpClients(clientProperties, sseTransports, customizer));
        }

        McpStdioClientProperties stdioProperties = clientProperties.getStdio();
        if (stdioProperties.isEnabled()) {
            Map<String, McpStdioClientProperties.StdioParameters> stdioConnections = stdioProperties.getConnections();
            Map<String, McpClientTransport> stdioTransports = Maps.newHashMap();
            for (Map.Entry<String, McpStdioClientProperties.StdioParameters> entry : stdioConnections.entrySet()) {
                StdioClientTransport transport = stdioClientTransport(entry.getValue());
                stdioTransports.put(entry.getKey(), transport);
            }
            clients.putAll(createMcpClients(clientProperties, stdioTransports, customizer));
        }
        return clients;
    }

    static Map<String, UnifiedMcpClient> createMcpClients(McpClientCommonProperties clientProperties,
                                                          Map<String, McpClientTransport> transports,
                                                          McpClientCustomizer customizer) {
        Map<String, UnifiedMcpClient> clients = Maps.newHashMap();
        if (!clientProperties.isEnabled()) {
            return clients;
        }
        for (Map.Entry<String, McpClientTransport> entry : transports.entrySet()) {
            String transportName = entry.getKey();
            McpSchema.Implementation clientInfo = new McpSchema.Implementation(
                    clientProperties.getName(),
                    clientProperties.getVersion()
            );

            if (McpClientCommonProperties.ClientType.SYNC == clientProperties.getType()) {
                McpClient.SyncSpec syncSpec = McpClientFactory.syncClient(entry.getValue())
                        .clientInfo(clientInfo)
                        .requestTimeout(clientProperties.getRequestTimeout());
                customizer.customize(transportName, syncSpec);
                UnifiedMcpClient mcpClient = UnifiedMcpClient.from(transportName, syncSpec.build());

                clients.put(transportName, mcpClient);
            } else {
                McpClient.AsyncSpec asyncSpec = McpClientFactory.asyncClient(entry.getValue())
                        .clientInfo(clientInfo)
                        .requestTimeout(clientProperties.getRequestTimeout());
                customizer.customize(transportName, asyncSpec);
                UnifiedMcpClient mcpClient = UnifiedMcpClient.from(transportName, asyncSpec.build());

                clients.put(transportName, mcpClient);
            }
        }
        return clients;
    }

    static HttpClientSseClientTransport sseClientTransport(McpSseClientProperties.SseParameters sseParameters) {
        return McpTransportFactory.httpClientSseClient(sseParameters.url());
    }

    static StdioClientTransport stdioClientTransport(McpStdioClientProperties.StdioParameters stdioParameters) {
        ServerParameters.Builder parametersBuilder = ServerParameters.builder(stdioParameters.command());
        if (CollectionUtils.isNotEmpty(stdioParameters.args())) {
            parametersBuilder.args(stdioParameters.args());
        }
        if (MapUtils.isNotEmpty(stdioParameters.env())) {
            parametersBuilder.env(stdioParameters.env());
        }
        return McpTransportFactory.stdioClient(parametersBuilder.build());
    }
}
