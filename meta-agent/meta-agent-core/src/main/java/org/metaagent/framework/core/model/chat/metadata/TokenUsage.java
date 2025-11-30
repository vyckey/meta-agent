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

package org.metaagent.framework.core.model.chat.metadata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import org.springframework.ai.chat.metadata.Usage;

/**
 * Record to hold token usage information.
 *
 * @author vyckey
 */
@EqualsAndHashCode(callSuper = false)
public class TokenUsage implements Usage {
    private final int promptTokens;
    private final int completionTokens;
    private final int totalTokens;
    private final Object nativeUsage;

    public static TokenUsage empty() {
        return new TokenUsage(0, 0, 0, null);
    }

    @JsonCreator
    public static TokenUsage from(@JsonProperty("promptTokens") Integer promptTokens,
                                  @JsonProperty("completionTokens") Integer completionTokens,
                                  @JsonProperty("totalTokens") Integer totalTokens,
                                  @JsonProperty("nativeUsage") Object nativeUsage) {
        return new TokenUsage(promptTokens, completionTokens, totalTokens, nativeUsage);
    }

    public TokenUsage(Integer promptTokens, Integer completionTokens, Integer totalTokens, Object nativeUsage) {
        this.promptTokens = promptTokens != null ? promptTokens : 0;
        this.completionTokens = completionTokens != null ? completionTokens : 0;
        this.totalTokens = totalTokens != null ? totalTokens : this.promptTokens + this.completionTokens;
        this.nativeUsage = nativeUsage;
    }

    public TokenUsage(Integer promptTokens, Integer completionTokens, Integer totalTokens) {
        this(promptTokens, completionTokens, totalTokens, null);
    }

    public TokenUsage(Integer promptTokens, Integer completionTokens) {
        this(promptTokens, completionTokens, null, null);
    }

    public TokenUsage accumulate(Usage other) {
        if (other == null) {
            return this;
        }
        return new TokenUsage(
                this.promptTokens + other.getPromptTokens(),
                this.completionTokens + other.getCompletionTokens(),
                this.totalTokens + other.getTotalTokens()
        );
    }

    @Override
    public Integer getPromptTokens() {
        return promptTokens;
    }

    @Override
    public Integer getCompletionTokens() {
        return completionTokens;
    }

    public Integer getTotalTokens() {
        return totalTokens;
    }

    @Override
    public Object getNativeUsage() {
        return nativeUsage;
    }

    public String toString() {
        return "TokenUsage{promptTokens=" + this.promptTokens + ", completionTokens=" + this.completionTokens + ", totalTokens=" + this.totalTokens + "}";
    }
}
