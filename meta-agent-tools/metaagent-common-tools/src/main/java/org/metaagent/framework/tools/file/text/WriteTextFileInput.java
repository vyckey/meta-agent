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

package org.metaagent.framework.tools.file.text;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.metaagent.framework.core.tool.schema.ToolDisplayable;

/**
 * Write text file input.
 *
 * @author vyckey
 */
@Getter
@Builder
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class WriteTextFileInput implements ToolDisplayable {
    @NotBlank(message = "filePath cannot be blank")
    @JsonProperty(required = true)
    @JsonPropertyDescription("The absolute path to the file to write. Relative paths are not supported.")
    private final String filePath;

    @JsonProperty(required = true)
    @JsonPropertyDescription("The content to write to the file.")
    private String content;

    @Builder.Default
    @JsonPropertyDescription("Whether append content to exists file. Default is false")
    private boolean append = false;

    @JsonCreator
    public WriteTextFileInput(@JsonProperty("filePath") String filePath) {
        this.filePath = filePath;
    }

    @Override
    public String display() {
        String snippet = content != null ? content.substring(0, Math.min(content.length(), 50)) : "";
        return "Write text file " + filePath + " with content: " + snippet + "...";
    }
}
