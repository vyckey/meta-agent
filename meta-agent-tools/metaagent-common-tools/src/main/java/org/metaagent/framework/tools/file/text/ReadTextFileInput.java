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
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.metaagent.framework.core.tool.schema.ToolDisplayable;

import java.util.Objects;

/**
 * ReadTextFileInput represents the input to the {@link ReadTextFileTool}.
 *
 * @author vyckey
 * @see ReadTextFileTool
 */
@Getter
@SuperBuilder
public class ReadTextFileInput implements ToolDisplayable {
    @JsonProperty(required = true)
    @JsonPropertyDescription("The absolute path to the file to read. Relative paths are not supported.")
    private final String filePath;

    @JsonPropertyDescription("The line number to start reading from for text file. Optional, default 0")
    private long offset;

    @Builder.Default
    @JsonPropertyDescription("The maximum number of lines to read for text file. If omitted, reads the entire file. Optional, default -1")
    private int limit = -1;

    @Builder.Default
    @JsonPropertyDescription("Whether to truncate file content if needed. Optional, default true")
    private boolean truncate = true;

    @JsonCreator
    public ReadTextFileInput(@JsonProperty("filePath") String filePath) {
        this.filePath = Objects.requireNonNull(filePath, "filePath is required");
    }

    public ReadTextFileInput(String filePath, long offset, int limit) {
        this(filePath);
        this.offset = offset;
        this.limit = limit;
    }

    @Override
    public String display() {
        String display = "Read text file " + filePath;
        if (offset >= 0) {
            display += "#L" + (offset + 1) + "-" + (limit > 0 ? (offset + limit) : "eof");
        }
        return display;
    }
}
