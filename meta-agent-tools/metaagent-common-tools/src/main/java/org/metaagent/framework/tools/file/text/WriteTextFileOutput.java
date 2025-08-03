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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Builder;
import lombok.Getter;
import org.metaagent.framework.core.tool.schema.ToolDisplayable;
import org.metaagent.framework.core.tool.schema.ToolErrorOutput;

import java.io.IOException;

/**
 * Write text file output.
 *
 * @author vyckey
 */
@Getter
@Builder
public class WriteTextFileOutput implements ToolErrorOutput, ToolDisplayable {
    @JsonIgnore
    private String filePath;

    @JsonProperty(required = true)
    private boolean success;

    @JsonIgnore
    private IOException exception;

    @JsonPropertyDescription("The error message if fail to write")
    public String getError() {
        return exception != null ? exception.getMessage() : null;
    }

    @Override
    public String display() {
        if (exception != null) {
            return "Failed to write text file " + filePath + ": " + exception.getMessage();
        }
        return "Successfully write to text file " + filePath;
    }
}
