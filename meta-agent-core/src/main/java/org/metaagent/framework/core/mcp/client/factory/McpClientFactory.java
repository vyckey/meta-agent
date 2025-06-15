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
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.core.mcp.client.UnifiedMcpClient;
import org.metaagent.framework.core.mcp.client.configure.McpClientCommonProperties;
import org.metaagent.framework.core.mcp.client.configure.McpClientProperties;
import org.metaagent.framework.core.mcp.client.configure.McpSseClientProperties;
import org.metaagent.framework.core.mcp.client.configure.McpStdioClientProperties;

import java.net.URI;
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
            clients.putAll(createMcpClients(sseProperties, sseTransports, customizer));
        }

        McpStdioClientProperties stdioProperties = clientProperties.getStdio();
        if (stdioProperties.isEnabled()) {
            Map<String, McpStdioClientProperties.StdioParameters> stdioConnections = stdioProperties.getConnections();
            Map<String, McpClientTransport> stdioTransports = Maps.newHashMap();
            for (Map.Entry<String, McpStdioClientProperties.StdioParameters> entry : stdioConnections.entrySet()) {
                StdioClientTransport transport = stdioClientTransport(entry.getValue());
                stdioTransports.put(entry.getKey(), transport);
            }
            clients.putAll(createMcpClients(stdioProperties, stdioTransports, customizer));
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
                McpSyncClient syncClient = syncSpec.build();
                if (clientProperties.isInitialized()) {
                    syncClient.initialize();
                }

                UnifiedMcpClient mcpClient = UnifiedMcpClient.from(transportName, syncClient);
                clients.put(transportName, mcpClient);
            } else {
                McpClient.AsyncSpec asyncSpec = McpClientFactory.asyncClient(entry.getValue())
                        .clientInfo(clientInfo)
                        .requestTimeout(clientProperties.getRequestTimeout());
                customizer.customize(transportName, asyncSpec);
                McpAsyncClient asyncClient = asyncSpec.build();
                if (clientProperties.isInitialized()) {
                    asyncClient.initialize().block();
                }

                UnifiedMcpClient mcpClient = UnifiedMcpClient.from(transportName, asyncClient);
                clients.put(transportName, mcpClient);
            }
        }
        return clients;
    }

    static HttpClientSseClientTransport sseClientTransport(McpSseClientProperties.SseParameters sseParameters) {
        if (StringUtils.isNotEmpty(sseParameters.endpoint())) {
            return McpTransportFactory.httpClientSseClientBuilder(sseParameters.url())
                    .sseEndpoint(sseParameters.endpoint())
                    .build();
        }

        URI uri = URI.create(sseParameters.url());
        if (StringUtils.isEmpty(uri.getPath())) {
            return McpTransportFactory.httpClientSseClient(uri.toString());
        } else {
            return McpTransportFactory.httpClientSseClientBuilder(uri.resolve("").toString())
                    .sseEndpoint(uri.getPath())
                    .build();
        }
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
