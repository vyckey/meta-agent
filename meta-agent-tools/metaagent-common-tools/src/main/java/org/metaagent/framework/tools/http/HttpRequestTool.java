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
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.ToolExecutionException;
import org.metaagent.framework.core.tool.converter.JsonToolConverter;
import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.definition.ToolDefinition;
import org.metaagent.framework.core.tool.human.HumanApprover;
import org.metaagent.framework.core.tool.human.SystemAutoApprover;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Setter
public class HttpRequestTool implements Tool<HttpRequest, HttpResponse> {
    private static final ToolDefinition TOOL_DEFINITION = ToolDefinition.builder("http_request")
            .description("Sends a HTTP request")
            .inputSchema(HttpRequest.class)
            .outputSchema(HttpResponse.class)
            .build();
    private static final JsonToolConverter<HttpRequest, HttpResponse> TOOL_CONVERTER =
            JsonToolConverter.create(HttpRequest.class);

    private final OkHttpClient httpClient;
    private HumanApprover humanApprover = SystemAutoApprover.INSTANCE;

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

    private Request buildRequest(HttpRequest request) {
        Request.Builder requestBuilder = new Request.Builder().url(request.getUrl());
        if (request.getHeaders() != null) {
            for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        RequestBody requestBody = null;
        if (request.getBody() != null) {
            requestBody = RequestBody.create(request.getBody(), MediaType.parse("application/json"));
        }
        requestBuilder.method(request.getMethod(), requestBody);
        return requestBuilder.build();
    }

    private HttpResponse buildResponse(Response response) throws IOException {
        int statusCode = response.code();
        HttpResponse.HttpResponseBuilder<?, ?> builder = HttpResponse.builder()
                .statusCode(statusCode)
                .headers(response.headers().toMultimap());
        if (response.body() != null) {
            builder.body(response.body().string());
        }
        return builder.build();
    }

    @Override
    public HttpResponse run(ToolContext toolContext, HttpRequest request) throws ToolExecutionException {
        Request realRequest = buildRequest(request);
        requestApprovalBeforeRequest(realRequest);
        try (Response response = httpClient.newCall(realRequest).execute()) {
            return buildResponse(response);
        } catch (IOException e) {
            log.warn("Error to send HTTP request {}", realRequest.url(), e);
            throw new ToolExecutionException("Error to send HTTP request", e);
        }
    }

    private void requestApprovalBeforeRequest(Request request) {
        String approval = request.method() + " " + request.url() + " " + request.body();
        HumanApprover.ApprovalInput approvalInput = new HumanApprover.ApprovalInput(approval, null);
        HumanApprover.ApprovalOutput approvalOutput = humanApprover.request(approvalInput);
        if (!approvalOutput.isApproved()) {
            throw new ToolExecutionException("User reject to request HTTP URL " + request.url());
        }
    }
}