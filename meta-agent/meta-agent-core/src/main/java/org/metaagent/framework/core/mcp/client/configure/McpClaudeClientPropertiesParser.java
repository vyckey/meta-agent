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

package org.metaagent.framework.core.mcp.client.configure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class McpClaudeClientPropertiesParser {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

    private McpClaudeClientPropertiesParser() {
    }

    public static McpClientProperties parseFile(String fileName) throws IOException {
        Objects.requireNonNull(fileName, "Claude MCP client configuration file name is required.");
        ClassLoader classLoader = McpClaudeClientPropertiesParser.class.getClassLoader();
        try (InputStream stream = classLoader.getResourceAsStream(fileName)) {
            if (stream == null) {
                throw new IOException("Resource not found: " + fileName);
            }
            String json = new String(stream.readAllBytes());
            return parseJson(json);
        }
    }

    public static McpClientProperties parseJson(String json) throws IOException {
        Objects.requireNonNull(json, "Claude MCP client configuration JSON is required.");
        ClaudeClientProperties claudeProperties = OBJECT_MAPPER.readValue(json, new TypeReference<>() {
        });

        McpClientProperties properties = new McpClientProperties();
        properties.setEnabled(true);
        properties.getStdio().setEnabled(true);
        properties.getSse().setEnabled(true);
        for (Map.Entry<String, Map<String, Object>> entry : claudeProperties.mcpServers.entrySet()) {
            String name = entry.getKey();
            Map<String, Object> config = entry.getValue();

            if (config.containsKey("command")) {
                McpStdioClientProperties.StdioParameters stdioParameters =
                        OBJECT_MAPPER.convertValue(config, McpStdioClientProperties.StdioParameters.class);
                properties.getStdio().getConnections().put(name, stdioParameters);
            } else if (config.containsKey("url")) {
                McpSseClientProperties.SseParameters sseParameters =
                        OBJECT_MAPPER.convertValue(config, McpSseClientProperties.SseParameters.class);
                properties.getSse().getConnections().put(name, sseParameters);
            } else {
                throw new IllegalArgumentException("Invalid MCP server configuration for " + name);
            }
        }
        return properties;
    }

    @Getter
    private static class ClaudeClientProperties {
        private final Map<String, Map<String, Object>> mcpServers = new HashMap<>();

    }

    public static void main(String[] args) throws IOException {
        McpClientProperties properties = McpClaudeClientPropertiesParser.parseFile("mcp-server.json");
        System.out.println("Parsed MCP Client Properties: " + properties);
    }
}
