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
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.common.abort.AbortException;
import org.metaagent.framework.core.security.SecurityLevel;
import org.metaagent.framework.core.security.approval.ApprovalStatus;
import org.metaagent.framework.core.security.approval.PermissionApproval;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.approval.ToolApprovalRequest;
import org.metaagent.framework.core.tool.config.ToolExecutionConfig;
import org.metaagent.framework.core.tool.config.ToolPattern;
import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.converter.ToolConverters;
import org.metaagent.framework.core.tool.definition.ToolDefinition;
import org.metaagent.framework.core.tool.exception.ToolExecutionException;
import org.metaagent.framework.core.tool.exception.ToolRejectException;
import org.metaagent.framework.core.tool.schema.ToolArgsValidator;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Shell command tool.
 *
 * @author vyckey
 */
@Setter
public class ShellCommandTool implements Tool<ShellCommandInput, ShellCommandOutput> {
    public static final String TOOL_NAME = "execute_shell_command";
    private static final ToolDefinition TOOL_DEFINITION = ToolDefinition.builder("execute_shell_command")
            .description("Executes a shell command and returns standard output, error, and exit code.")
            .inputSchema(ShellCommandInput.class)
            .outputSchema(ShellCommandOutput.class)
            .isConcurrencySafe(false)
            .isReadOnly(false)
            .build();
    private static final ToolConverter<ShellCommandInput, ShellCommandOutput> TOOL_CONVERTER =
            ToolConverters.jsonConverter(ShellCommandInput.class);

    @Override
    public ToolDefinition getDefinition() {
        return TOOL_DEFINITION;
    }

    @Override
    public ToolConverter<ShellCommandInput, ShellCommandOutput> getConverter() {
        return TOOL_CONVERTER;
    }

    protected void validateInput(ToolContext toolContext, ShellCommandInput commandInput) throws ToolExecutionException {
        ToolArgsValidator.validate(commandInput);
    }

    protected void requestApprovalIfNeeded(ToolContext toolContext, ShellCommandInput commandInput) throws ToolExecutionException {
        String command = commandInput.command();
        if (toolContext.getSecurityLevel().compareTo(SecurityLevel.UNRESTRICTED_DANGEROUSLY) <= 0) {
            return;
        }

        List<String> subCommands = ShellCommandUtils.splitCommands(command);

        ToolExecutionConfig toolExecutionConfig = toolContext.getToolExecutionConfig();
        List<ToolPattern> disallowedTools = toolExecutionConfig.disallowedTools(getName());
        List<ToolPattern> allowedTools = toolExecutionConfig.allowedTools(getName());

        boolean needApproval = allowedTools.isEmpty();
        for (String subCommand : subCommands) {
            if (toolContext.getSecurityLevel().compareTo(SecurityLevel.RESTRICTED_HIGHLY_SAFE) <= 0
                    && ShellCommandUtils.hasCommandSubstitution(subCommand)) {
                throw new ToolExecutionException("Substitution command is not allowed in restricted highly safe environment.");
            }

            for (ToolPattern disallowedTool : disallowedTools) {
                if (!matchToolPattern(subCommand, disallowedTool.pattern())) {
                    throw new ToolRejectException(disallowedTool.toolName(),
                            "The command \"" + command + "\" has been disallowed by the rule " + disallowedTool);
                }
            }

            boolean subCommandAllowed = allowedTools.stream()
                    .anyMatch(allowedTool -> matchToolPattern(subCommand, allowedTool.pattern()));
            if (!subCommandAllowed) {
                needApproval = true;
                break;
            }
        }
        if (needApproval) {
            ToolApprovalRequest approvalRequest = ToolApprovalRequest.builder()
                    .id(toolContext.getExecutionId())
                    .toolName(getName())
                    .approvalContent("Request approval to execute shell command: " + command)
                    .input(commandInput)
                    .build();
            PermissionApproval approvalResult = toolContext.requestApproval(approvalRequest);
            if (approvalResult.getApprovalStatus() == ApprovalStatus.REJECTED) {
                logger.warn("The execution of tool {} with command \"{}\" is not approved, reason: {} ",
                        getName(), command, approvalResult.getContent());
                throw new ToolRejectException(getName(), approvalResult.getContent());
            }
        }
    }

    protected boolean matchToolPattern(String command, String pattern) {
        if (StringUtils.isEmpty(pattern)) {
            return true;
        }
        String regex = pattern.replaceAll("[*]+", ".*");
        return command.matches(regex);
    }

    @Override
    public ShellCommandOutput run(ToolContext toolContext, ShellCommandInput commandInput) throws ToolExecutionException {
        validateInput(toolContext, commandInput);
        requestApprovalIfNeeded(toolContext, commandInput);

        if (toolContext.getAbortSignal().isAborted()) {
            throw new AbortException("Tool " + getName() + " is cancelled");
        }
        return execute(toolContext, commandInput);
    }

    protected ShellCommandOutput execute(ToolContext toolContext, ShellCommandInput commandInput) throws ToolExecutionException {
        ShellCommandOutput.ShellCommandOutputBuilder<?, ?> outputBuilder = ShellCommandOutput.builder();
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
