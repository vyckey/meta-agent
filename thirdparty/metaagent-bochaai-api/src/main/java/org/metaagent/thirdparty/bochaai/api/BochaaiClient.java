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

package org.metaagent.thirdparty.bochaai.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import okhttp3.OkHttpClient;
import org.metaagent.thirdparty.bochaai.api.websearch.WebSearchData;
import org.metaagent.thirdparty.bochaai.api.websearch.WebSearchRequest;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.Set;

public class BochaaiClient {
    public static final String BASE_URL = "https://api.bochaai.com";
    private static final Set<Integer> ERR_HTTP_STATUS = Set.of(403, 400, 401, 429, 500);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private final BochaaiApi bochaaiApi;
    private final String apiKey;

    public BochaaiClient(OkHttpClient okHttpClient, String apiKey) {
        if (apiKey == null) {
            apiKey = System.getenv("BOCHAAI_API_KEY");
        }
        this.apiKey = Objects.requireNonNull(apiKey, "BOCHAAI_API_KEY is required");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(JacksonConverterFactory.create(OBJECT_MAPPER))
                .build();

        this.bochaaiApi = retrofit.create(BochaaiApi.class);
    }

    public BochaaiClient(String apiKey, Duration timeout) {
        this(new OkHttpClient.Builder()
                .callTimeout(timeout)
                .connectTimeout(timeout)
                .readTimeout(timeout)
                .writeTimeout(timeout).build(), apiKey);
    }

    public BochaaiClient(String apiKey) {
        this(apiKey, Duration.ofSeconds(5));
    }

    public BochaaiResponse<WebSearchData> search(WebSearchRequest searchRequest) {
        try {
            Response<BochaaiResponse<WebSearchData>> retrofitResponse = bochaaiApi
                    .webSearch(this.apiKey, searchRequest)
                    .execute();
            if (retrofitResponse.isSuccessful()) {
                return retrofitResponse.body();
            } else {
                throw toException(retrofitResponse);
            }
        } catch (IOException e) {
            throw new BochaaiApiException(e);
        }
    }

    private static BochaaiApiException toException(Response<?> response) throws IOException {
        int code = response.code();
        String body = response.errorBody().string();
        if (ERR_HTTP_STATUS.contains(code)) {
            BochaaiResponse<Void> errorResponse = OBJECT_MAPPER.readValue(body, new TypeReference<>() {
            });
            return new BochaaiApiException(errorResponse);
        }
        String errorMessage = String.format("status code: %s; body: %s", code, body);
        return new BochaaiApiException(errorMessage);
    }
}
