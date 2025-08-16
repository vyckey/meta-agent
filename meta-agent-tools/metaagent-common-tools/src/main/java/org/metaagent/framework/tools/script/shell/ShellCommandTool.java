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
import lombok.Setter;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.ToolExecutionException;
import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.converter.ToolConverters;
import org.metaagent.framework.core.tool.definition.ToolDefinition;
import org.metaagent.framework.core.tool.human.HumanApprover;
import org.metaagent.framework.core.tool.human.SystemAutoApprover;
import org.metaagent.framework.core.util.abort.AbortException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Shell command tool.
 *
 * @author vyckey
 */
@Setter
public class ShellCommandTool implements Tool<ShellCommandInput, ShellCommandOutput> {
    private static final ToolDefinition TOOL_DEFINITION = ToolDefinition.builder("execute_shell_command")
            .description("Executes a shell command and returns standard output, error, and exit code.")
            .inputSchema(ShellCommandInput.class)
            .outputSchema(ShellCommandOutput.class)
            .isConcurrencySafe(false)
            .isReadOnly(false)
            .build();
    private static final ToolConverter<ShellCommandInput, ShellCommandOutput> TOOL_CONVERTER =
            ToolConverters.jsonConverter(ShellCommandInput.class);
    private HumanApprover humanApprover = SystemAutoApprover.INSTANCE;

    @Override
    public ToolDefinition getDefinition() {
        return TOOL_DEFINITION;
    }

    @Override
    public ToolConverter<ShellCommandInput, ShellCommandOutput> getConverter() {
        return TOOL_CONVERTER;
    }

    private boolean confirmBeforeExecution(ShellCommandInput commandInput) {
        String value = System.getenv("SHELL_CONFIRM_BEFORE_EXECUTION");
        boolean confirmRequired = StringUtils.isNotEmpty(value) && BooleanUtils.toBoolean(value);
        if (!confirmRequired) {
            return true;
        }

        String approval = "Whether execute the command [\"" + commandInput.command() + "\"] ?";
        HumanApprover.ApprovalOutput approvalOutput = humanApprover.request(new HumanApprover.ApprovalInput(approval, null));
        return approvalOutput.isApproved();
    }

    @Override
    public ShellCommandOutput run(ToolContext toolContext, ShellCommandInput commandInput) throws ToolExecutionException {
        ShellCommandOutput.ShellCommandOutputBuilder<?, ?> outputBuilder = ShellCommandOutput.builder();
        if (!confirmBeforeExecution(commandInput)) {
            outputBuilder.exitCode(-1).error("User cancelled the command execution.");
            return outputBuilder.build();
        }
        if (toolContext.getAbortSignal().isAborted()) {
            throw new AbortException("Tool " + getName() + " is cancelled");
        }

        try {
            Process process = Runtime.getRuntime().exec(commandInput.command(), commandInput.getEnvArray());
            outputBuilder.pid(process.pid());

            toolContext.getAbortSignal().addAbortListener(signal -> {
                // graceful kill process
                process.destroy();

                // force kill process
                Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                    if (process.isAlive()) {
                        process.destroyForcibly();
                    }
                }, 3, TimeUnit.SECONDS);
            });

            if (commandInput.timeoutSeconds() != null) {
                boolean exited = process.waitFor(commandInput.timeoutSeconds(), TimeUnit.SECONDS);
                if (exited) {
                    outputBuilder.exitCode(process.exitValue());
                } else {
                    outputBuilder.exitCode(-1).error("Command execution timeout.");
                }
            } else {
                outputBuilder.exitCode(process.waitFor());
            }

            String stdOutput = CharStreams.toString(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            outputBuilder.stdOutput(stdOutput);
            String stdError = CharStreams.toString(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));
            outputBuilder.stdError(stdError);
        } catch (IOException e) {
            outputBuilder.exitCode(-1).error(e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            outputBuilder.exitCode(-1).error("InterruptedException: " + e.getMessage());
        }
        return outputBuilder.build();
    }
}
