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
import com.google.common.collect.Sets;
import org.apache.commons.collections.MapUtils;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Parser for MCP client properties from JSON or file.
 *
 * @author vyckey
 */
public class McpClientPropertiesParser {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

    private McpClientPropertiesParser() {
    }

    public static McpClientProperties fromFile(String fileName) throws IOException {
        Objects.requireNonNull(fileName, "MCP client properties file name is required.");
        ClassLoader classLoader = McpClientPropertiesParser.class.getClassLoader();
        try (InputStream stream = classLoader.getResourceAsStream(fileName)) {
            if (stream == null) {
                throw new IOException("Resource not found: " + fileName);
            }
            String json = new String(stream.readAllBytes());
            return fromJson(json);
        }
    }

    public static McpClientProperties fromJson(String json) throws IOException {
        Map<String, Object> map = OBJECT_MAPPER.readValue(json, new TypeReference<>() {
        });
        return fromMap(map);
    }

    @SuppressWarnings("unchecked")
    public static McpClientProperties fromMap(Map<String, Object> map) throws IOException {
        try {
            McpClientProperties properties = OBJECT_MAPPER.convertValue(map, McpClientProperties.class);
            if (map.containsKey("requestTimeout")) {
                properties.setRequestTimeout(Duration.ofMillis(MapUtils.getLongValue(map, "requestTimeout")));
            }

            if (map.containsKey("stdio")) {
                Map<String, Object> stdioMap = (Map<String, Object>) map.get("stdio");
                if (stdioMap.containsKey("requestTimeout")) {
                    properties.getStdio().setRequestTimeout(Duration.ofMillis(MapUtils.getLongValue(map, "requestTimeout")));
                }
                mergeProperties(properties, properties.getStdio(), stdioMap);
            }
            if (map.containsKey("sse")) {
                Map<String, Object> sseMap = (Map<String, Object>) map.get("sse");
                if (sseMap.containsKey("requestTimeout")) {
                    properties.getSse().setRequestTimeout(Duration.ofMillis(MapUtils.getLongValue(map, "requestTimeout")));
                }
                mergeProperties(properties, properties.getSse(), sseMap);
            }
            checkProperties(properties);
            return properties;
        } catch (Exception e) {
            throw new IOException("Failed to parse MCP client properties", e);
        }
    }

    private static void mergeProperties(McpClientCommonProperties globalProperties,
                                        McpClientCommonProperties targetProperties,
                                        Map<String, Object> propertiesMap) {
        if (!propertiesMap.containsKey("enabled")) {
            targetProperties.setEnabled(globalProperties.isEnabled());
        }
        if (!propertiesMap.containsKey("name")) {
            targetProperties.setName(globalProperties.getName());
        }
        if (!propertiesMap.containsKey("version")) {
            targetProperties.setVersion(globalProperties.getVersion());
        }
        if (!propertiesMap.containsKey("initialized")) {
            targetProperties.setInitialized(globalProperties.isInitialized());
        }
        if (!propertiesMap.containsKey("requestTimeout")) {
            targetProperties.setRequestTimeout(globalProperties.getRequestTimeout());
        }
        if (!propertiesMap.containsKey("type")) {
            targetProperties.setType(globalProperties.getType());
        }
    }

    private static void checkProperties(McpClientProperties properties) {
        Set<String> stdioNames = properties.getStdio().getConnections().keySet();
        Set<String> sseNames = properties.getSse().getConnections().keySet();
        Set<String> duplicatedNames = Sets.intersection(stdioNames, sseNames);
        if (!duplicatedNames.isEmpty()) {
            throw new IllegalArgumentException("Duplicate connection names found: " + String.join(", ", duplicatedNames));
        }
    }

    public static void main(String[] args) throws IOException {
        McpClientProperties clientProperties =
                McpClientPropertiesParser.fromFile(McpClientProperties.DEFAULT_PROPERTIES_FILE);
        System.out.println(clientProperties);
    }
}
