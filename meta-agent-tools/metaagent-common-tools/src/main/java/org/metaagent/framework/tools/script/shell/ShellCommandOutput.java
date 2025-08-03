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

package org.metaagent.framework.tools.script.shell;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.metaagent.framework.core.tool.schema.ToolDisplayable;
import org.metaagent.framework.core.tool.schema.ToolErrorOutput;

/**
 * description is here
 *
 * @author vyckey
 */
@Getter
@SuperBuilder
public class ShellCommandOutput implements ToolErrorOutput, ToolDisplayable {
    @JsonPropertyDescription("The process ID")
    private Long pid;

    @JsonProperty(required = true)
    @JsonPropertyDescription("The exit code of the command execution. 0 indicates success, non-zero indicates failure.")
    private int exitCode;

    @JsonPropertyDescription("The standard output of the command execution")
    private String stdOutput;

    @JsonPropertyDescription("The standard output of the command execution")
    private String stdError;

    @JsonPropertyDescription("An error message if the command execution failed")
    private String error;

    public boolean isSuccessful() {
        return exitCode == 0;
    }

    @Override
    public String display() {
        StringBuilder displayBuilder = new StringBuilder();
        displayBuilder.append("Exit Code: ").append(exitCode).append("\n");
        if (stdOutput != null && !stdOutput.isEmpty()) {
            displayBuilder.append("Standard Output:\n").append(stdOutput).append("\n");
        }
        if (stdError != null && !stdError.isEmpty()) {
            displayBuilder.append("Standard Error:\n").append(stdError).append("\n");
        }
        if (error != null && !error.isEmpty()) {
            displayBuilder.append("Error:\n").append(error).append("\n");
        }
        return displayBuilder.toString();
    }
}
