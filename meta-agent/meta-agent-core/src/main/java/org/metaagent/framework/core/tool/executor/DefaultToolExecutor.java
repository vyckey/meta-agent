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

package org.metaagent.framework.core.tool.executor;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.core.security.SecurityLevel;
import org.metaagent.framework.core.security.approval.PermissionApproval;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.approval.ToolApprovalRequest;
import org.metaagent.framework.core.tool.config.ToolExecutionConfig;
import org.metaagent.framework.core.tool.config.ToolPattern;
import org.metaagent.framework.core.tool.exception.ToolExecutionException;
import org.metaagent.framework.core.tool.exception.ToolRejectException;
import org.metaagent.framework.core.tool.manager.ToolManager;

import java.util.List;
import java.util.UUID;

/**
 * Default implementation of {@link ToolExecutor}.
 *
 * @author vyckey
 */
@Slf4j
public class DefaultToolExecutor implements ToolExecutor {
    public static final DefaultToolExecutor INSTANCE = new DefaultToolExecutor();

    @Override
    public <I, O> O execute(ToolExecutorContext executorContext, Tool<I, O> tool, I input) throws ToolExecutionException {
        ToolExecuteDelegate<I, O> delegate = new ToolExecuteDelegate<>(tool, executorContext);
        return delegate.run(executorContext.getToolContext(), input);
    }

    @Override
    public <I, O> String execute(ToolExecutorContext executorContext, Tool<I, O> tool, String input) throws ToolExecutionException {
        ToolContext toolContext = executorContext.getToolContext();
        String toolCallId = UUID.randomUUID().toString().replaceAll("-", "").substring(16);
        toolContext.setExecutionId(toolCallId);

        boolean approvalRequired = approvalRequired(executorContext, tool, input);
        if (approvalRequired) {
            requestToolApproval(executorContext, tool, input);
        }

        ToolExecuteDelegate<I, O> delegate = new ToolExecuteDelegate<>(tool, executorContext);
        return delegate.call(executorContext.getToolContext(), input);
    }

    protected <I, O> boolean approvalRequired(ToolExecutorContext executorContext, Tool<I, O> tool, String input) {
        ToolContext toolContext = executorContext.getToolContext();
        if (toolContext.getSecurityLevel().compareTo(SecurityLevel.UNRESTRICTED_DANGEROUSLY) <= 0) {
            return false;
        } else if (toolContext.getSecurityLevel().compareTo(SecurityLevel.RESTRICTED_DEFAULT_SALE) <= 0) {
//            if (tool.getDefinition().isReadOnly()) {
//                return false;
//            }
        }

        String toolName = tool.getName();
        ToolExecutionConfig executionConfig = toolContext.getToolExecutionConfig();
        List<ToolPattern> disallowedTools = executionConfig.disallowedTools(toolName);
        for (ToolPattern disallowedTool : disallowedTools) {
            if (disallowedTool.toolName().equals(toolName) && StringUtils.isEmpty(disallowedTool.pattern())) {
                throw new ToolRejectException(toolName, "not allowed execution");
            }
        }

        List<ToolPattern> allowedTools = executionConfig.allowedTools(toolName);
        return allowedTools.isEmpty();
    }

    protected void requestToolApproval(ToolExecutorContext executorContext, Tool<?, ?> tool, String input) {
        ToolContext toolContext = executorContext.getToolContext();
        ToolApprovalRequest approvalRequest = ToolApprovalRequest.requestCall(toolContext.getExecutionId(), tool.getName(), input);
        PermissionApproval approval = toolContext.requestApproval(approvalRequest);
        if (!approval.isApproved()) {
            String reason = StringUtils.isNotEmpty(approval.getContent()) ? approval.getContent() : "user rejected tool execution";
            throw new ToolRejectException(tool.getName(), reason);
        }
    }

    @Override
    public BatchToolOutputs execute(ToolExecutorContext executorContext, BatchToolInputs toolInputs) throws ToolExecutionException {
        ToolManager toolManager = executorContext.getToolManager();
        ToolContext toolContext = executorContext.getToolContext();

        List<BatchToolOutputs.ToolOutput> outputs = Lists.newArrayList();
        for (BatchToolInputs.ToolInput toolInput : toolInputs.inputs()) {
            Tool<?, ?> tool = toolManager.getTool(toolInput.toolName());
            String output = execute(executorContext, tool, toolInput.input());
            outputs.add(new BatchToolOutputs.ToolOutput(toolContext.getExecutionId(), toolInput.toolName(), output));
        }
        return new BatchToolOutputs(outputs);
    }
}
