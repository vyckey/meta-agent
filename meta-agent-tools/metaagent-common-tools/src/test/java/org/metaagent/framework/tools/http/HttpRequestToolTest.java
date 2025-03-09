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

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.metaagent.framework.core.tool.ToolExecutionException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HttpRequestToolTest {
    private OkHttpClient mockHttpClient;
    private HttpRequestTool httpRequestTool;

    @BeforeEach
    void setUp() {
        mockHttpClient = mock(OkHttpClient.class);
        httpRequestTool = new HttpRequestTool(mockHttpClient);
    }

    @Test
    void testSuccessfulHttpRequest() throws Exception {
        Headers mockHeaders = new Headers.Builder()
                .add("Content-Type", "application/json")
                .build();
        Response mockResponse = mock(Response.class);
        when(mockResponse.code()).thenReturn(200);
        when(mockResponse.headers()).thenReturn(mockHeaders);
        when(mockResponse.body()).thenReturn(ResponseBody.create("OK", MediaType.parse("text/plain")));

        Call call = mock(Call.class);
        when(call.execute()).thenReturn(mockResponse);
        when(mockHttpClient.newCall(any(Request.class))).thenReturn(call);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        HttpRequest request = HttpRequest.builder()
                .url("https://example.com")
                .method("GET").headers(headers).build();

        HttpResponse response = httpRequestTool.run(null, request);

        assertEquals(200, response.getStatusCode());
        assertEquals("OK", response.getBody());
        assertEquals(List.of("application/json"), response.getHeaders().get("Content-Type"));
    }

    @Test
    void testFailedHttpRequest() throws IOException {
        Call call = mock(Call.class);
        doThrow(new IOException("Network error")).when(call).execute();
        when(mockHttpClient.newCall(any(Request.class))).thenReturn(call);

        HttpRequest request = HttpRequest.builder()
                .url("https://example.com")
                .method("GET").build();

        Exception exception = assertThrows(ToolExecutionException.class, () -> httpRequestTool.run(null, request));
        assertTrue(exception.getMessage().contains("Error to send HTTP request"));
    }

}