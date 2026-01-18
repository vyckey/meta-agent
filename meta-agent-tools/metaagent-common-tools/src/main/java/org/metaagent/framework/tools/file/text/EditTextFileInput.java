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
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.metaagent.framework.core.tool.schema.ToolDisplayable;

@Builder
public record EditTextFileInput(
        @NotBlank(message = "target filePath is required")
        @JsonProperty(required = true)
        @JsonPropertyDescription("The absolute path to the file to modify. Must start with '/'.")
        String filePath,

        @NotNull(message = "oldString cannot be null")
        @JsonProperty(required = true)
        @JsonPropertyDescription("The exact literal text to replace, preferably unescaped. " +
                "For single replacements (default), include at least 3 lines of context " +
                "BEFORE and AFTER the target text, matching whitespace and indentation precisely. " +
                "For multiple replacements, specify expected_replacements parameter. " +
                "If this string is not the exact literal text (i.e. you escaped it) or does not match exactly, the tool will fail.")
        String oldString,

        @NotNull(message = "newString cannot be null")
        @JsonProperty(required = true)
        @JsonPropertyDescription("The exact literal text to replace `old_string` with, preferably unescaped. " +
                "Provide the EXACT text. Ensure the resulting code is correct and idiomatic.")
        String newString,

        @JsonPropertyDescription("Number of replacements expected. Defaults to 1 if not specified. " +
                "Use when you want to replace multiple occurrences.")
        Integer expectedReplacements) implements ToolDisplayable {

    @JsonCreator
    public EditTextFileInput(@JsonProperty("filePath") String filePath,
                             @JsonProperty("oldString") String oldString,
                             @JsonProperty("newString") String newString) {
        this(filePath, oldString, newString, 1);
    }

    @Override
    public String display() {
        return "Edit file " + filePath;
    }
}
