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

package org.metaagent.thirdparty.google.api.searchapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;

public class SearchApiClient {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().enable(INDENT_OUTPUT);
    private final SearchApi api;

    @Builder
    public SearchApiClient(Duration timeout, String baseUrl) {
        Objects.requireNonNull(timeout, "timeout is null");
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("baseUrl is blank");
        }

        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                .callTimeout(timeout)
                .connectTimeout(timeout)
                .readTimeout(timeout)
                .writeTimeout(timeout);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl.trim())
                .client(okHttpClientBuilder.build())
                .addConverterFactory(JacksonConverterFactory.create(OBJECT_MAPPER))
                .build();
        this.api = retrofit.create(SearchApi.class);
    }

    public SearchApiWebSearchResponse search(SearchApiWebSearchRequest request) {
        try {
            Map<String, Object> params = OBJECT_MAPPER.convertValue(request, new TypeReference<>() {
            });
            String bearerToken = "Bearer " + request.getApiKey();
            Response<SearchApiWebSearchResponse> response = api.search(params, bearerToken).execute();
            return getBody(response);
        } catch (IOException e) {
            throw new SearchApiException(e);
        }
    }

    private SearchApiWebSearchResponse getBody(Response<SearchApiWebSearchResponse> response) throws IOException {
        if (response.isSuccessful()) {
            return response.body();
        } else {
            throw toException(response);
        }
    }

    private static RuntimeException toException(Response<?> response) throws IOException {
        try (ResponseBody responseBody = response.errorBody()) {
            int code = response.code();
            if (responseBody != null) {
                String body = responseBody.string();
                String errorMessage = String.format("status code: %s; body: %s", code, body);
                return new SearchApiException(errorMessage);
            } else {
                return new SearchApiException(String.format("status code: %s;", code));
            }
        }
    }
}
