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

import com.google.common.io.CharStreams;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.ToolExecutionException;
import org.metaagent.framework.core.tool.converter.JsonToolConverter;
import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.definition.ToolDefinition;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * description is here
 *
 * @author vyckey
 */
public class ShellCommandTool implements Tool<ShellCommandInput, ShellCommandOutput> {

    @Override
    public ToolDefinition getDefinition() {
        return ToolDefinition.builder("shell command tool")
                .description("Run shell command")
                .inputSchema(ShellCommandInput.class)
                .outputSchema(ShellCommandOutput.class)
                .build();
    }

    @Override
    public ToolConverter<ShellCommandInput, ShellCommandOutput> getConverter() {
        return JsonToolConverter.create(ShellCommandInput.class);
    }

    @Override
    public ShellCommandOutput run(ToolContext toolContext, ShellCommandInput commandInput) throws ToolExecutionException {
        int exitCode = -1;
        String output = null;
        String error;
        try {
            Process process = Runtime.getRuntime().exec(commandInput.getCommand(), commandInput.getEnvArray());

            output = CharStreams.toString(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            error = CharStreams.toString(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));
            exitCode = process.waitFor();
        } catch (IOException e) {
            error = "IOException: " + e.getMessage();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            error = "InterruptedException: " + e.getMessage();
        }
        return ShellCommandOutput.builder().exitCode(exitCode).output(output).error(error).build();
    }
}
