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

package org.metaagent.framework.tools.http;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.metaagent.framework.core.tool.schema.ToolDisplayable;

import java.util.Map;

/**
 * Represents an HTTP request with a URL, method, headers, and body.
 *
 * @author vyckey
 */
@Builder
public record HttpRequest(
        @NotBlank(message = "url cannot be blank")
        @JsonProperty(required = true)
        @JsonPropertyDescription("The request URL")
        String url,

        @JsonProperty(defaultValue = "GET")
        @JsonPropertyDescription("HTTP method (GET, POST, PUT, DELETE, etc.). Optional, default is GET")
        String method,

        @JsonPropertyDescription("HTTP request headers. Optional")
        Map<String, String> headers,

        @JsonPropertyDescription("HTTP request body if exists. Optional")
        String body) implements ToolDisplayable {

    @Override
    public String display() {
        return method + " " + url;
    }
}
