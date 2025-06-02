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

package org.metaagent.framework.core.mcp.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.client.transport.WebFluxSseClientTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Model Context Protocol (MCP) Transport Factory
 *
 * @see McpClientTransport
 * @see StdioClientTransport
 * @see HttpClientSseClientTransport
 */
public abstract class McpTransportFactory {
    public static StdioClientTransport stdioClient(ServerParameters params) {
        return new StdioClientTransport(params);
    }

    public static StdioClientTransport stdioClient(ServerParameters params, ObjectMapper objectMapper) {
        return new StdioClientTransport(params, objectMapper);
    }

    public static HttpClientSseClientTransport.Builder httpClientSseClientBuilder(String baseUri) {
        return HttpClientSseClientTransport.builder(baseUri);
    }

    public static HttpClientSseClientTransport httpClientSseClient(String baseUri) {
        return HttpClientSseClientTransport.builder(baseUri).build();
    }

    public static WebFluxSseClientTransport webFluxSseClient(WebClient.Builder webClientBuilder) {
        return new WebFluxSseClientTransport(webClientBuilder);
    }

    public static WebFluxSseClientTransport webFluxSseClient(String baseUrl) {
        return webFluxSseClient(WebClient.builder().baseUrl(baseUrl));
    }
}
