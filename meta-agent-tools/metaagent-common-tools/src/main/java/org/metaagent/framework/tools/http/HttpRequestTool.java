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

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.common.abort.AbortException;
import org.metaagent.framework.core.security.approval.ApprovalStatus;
import org.metaagent.framework.core.security.approval.PermissionApproval;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.approval.ToolApprovalRequest;
import org.metaagent.framework.core.tool.config.ToolPattern;
import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.converter.ToolConverters;
import org.metaagent.framework.core.tool.definition.ToolDefinition;
import org.metaagent.framework.core.tool.exception.ToolExecutionException;
import org.metaagent.framework.core.tool.exception.ToolRejectException;
import org.metaagent.framework.core.tool.schema.ToolArgsValidator;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * Tool for sending HTTP requests.
 *
 * @author vyckey
 */
@Slf4j
@Setter
public class HttpRequestTool implements Tool<HttpRequest, HttpResponse> {
    public static final String TOOL_NAME = "http_request";
    private static final ToolDefinition TOOL_DEFINITION = ToolDefinition.builder(TOOL_NAME)
            .description("Sends a HTTP request and returns the response.")
            .inputSchema(HttpRequest.class)
            .outputSchema(HttpResponse.class)
            .isConcurrencySafe(true)
            .isReadOnly(false)
            .build();
    private static final ToolConverter<HttpRequest, HttpResponse> TOOL_CONVERTER =
            ToolConverters.jsonConverter(HttpRequest.class);

    private final OkHttpClient httpClient;

    public HttpRequestTool(OkHttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");
    }

    public HttpRequestTool() {
        this(new OkHttpClient());
    }

    @Override
    public ToolDefinition getDefinition() {
        return TOOL_DEFINITION;
    }

    @Override
    public ToolConverter<HttpRequest, HttpResponse> getConverter() {
        return TOOL_CONVERTER;
    }

    private void validateInput(ToolContext toolContext, HttpRequest request) {
        ToolArgsValidator.validate(request);
        for (ToolPattern disallowedTool : toolContext.getToolExecutionConfig().disallowedTools(getName())) {
            if (matchPattern(request, disallowedTool.pattern())) {
                throw new ToolRejectException(disallowedTool.toolName(),
                        "disallowed execution by rule: " + disallowedTool.pattern());
            }
        }
        for (ToolPattern allowedTool : toolContext.getToolExecutionConfig().allowedTools(getName())) {
            if (matchPattern(request, allowedTool.pattern())) {
                return;
            }
        }

        ToolApprovalRequest approvalRequest = ToolApprovalRequest.builder()
                .id(toolContext.getExecutionId())
                .toolName(getName())
                .input(request)
                .build();
        PermissionApproval approvalResult = toolContext.requestApproval(approvalRequest);
        if (approvalResult.getApprovalStatus() == ApprovalStatus.REJECTED) {
            throw new ToolRejectException(getName(), "http request was rejected by approval: " + approvalResult.getContent());
        }
    }

    /**
     * Checks if the request matches the given pattern.
     *
     * <p>
     * For example:
     * <ul>
     *     <li>http_request(get|head|options)</li>
     *     <li>http_request(get|head|options https://google\.com/.*)</li>
     *     <li>http_request(get|head|options https://.*\/github\.com/.*)</li>
     * </ul>
     *
     * @param request the request to check
     * @param pattern the pattern to match
     * @return true if the request matches the pattern, false otherwise
     */
    private boolean matchPattern(HttpRequest request, String pattern) {
        if (StringUtils.isEmpty(pattern)) {
            return true;
        }
        pattern = pattern.trim();
        int whitespaceIndex = pattern.indexOf(' ');
        if (whitespaceIndex < 0) {
            return false;
        }
        boolean methodMatched = Arrays.stream(pattern.substring(0, whitespaceIndex).trim().split("\\|"))
                .map(String::trim).anyMatch(method -> method.equalsIgnoreCase(request.method()));
        if (!methodMatched) {
            return false;
        }
        String urlRegex = pattern.substring(whitespaceIndex + 1).trim();
        return urlRegex.isEmpty() || request.url().matches(urlRegex);
    }

    private Request buildRequest(HttpRequest request) {
        Request.Builder requestBuilder = new Request.Builder().url(request.url());
        if (request.headers() != null) {
            for (Map.Entry<String, String> entry : request.headers().entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        RequestBody requestBody = null;
        if (request.body() != null) {
            requestBody = RequestBody.create(request.body(), MediaType.parse("application/json"));
        }
        requestBuilder.method(request.method().toUpperCase(), requestBody);
        return requestBuilder.build();
    }

    private HttpResponse buildResponse(HttpRequest request, Response response) throws IOException {
        int statusCode = response.code();
        HttpResponse.HttpResponseBuilder<?, ?> builder = HttpResponse.builder()
                .statusCode(statusCode)
                .headers(response.headers().toMultimap());
        if (response.body() != null) {
            builder.body(response.body().string());
        }
        StringBuilder displayBuilder = new StringBuilder(request.method()).append(" ").append(request.url());
        if (statusCode >= 200 && statusCode < 300) {
            displayBuilder.append(" - Success");
        } else {
            displayBuilder.append(" - Error: ").append(statusCode);
        }
        builder.display(displayBuilder.toString());
        return builder.build();
    }

    @Override
    public HttpResponse run(ToolContext toolContext, HttpRequest request) throws ToolExecutionException {
        validateInput(toolContext, request);
        Request realRequest = buildRequest(request);
        if (toolContext.getAbortSignal().isAborted()) {
            throw new AbortException("Tool " + getName() + " is cancelled");
        }

        if (toolContext.getAbortSignal().isAborted()) {
            throw new AbortException("Tool " + getName() + " is cancelled");
        }

        try (Response response = httpClient.newCall(realRequest).execute()) {
            return buildResponse(request, response);
        } catch (IOException e) {
            log.warn("Error to send HTTP request {}", realRequest.url(), e);
            throw new ToolExecutionException("Error to send HTTP request", e);
        }
    }

}