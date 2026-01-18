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

package org.metaagent.framework.core.tool;

import lombok.Getter;
import org.metaagent.framework.common.abort.AbortController;
import org.metaagent.framework.common.abort.AbortSignal;
import org.metaagent.framework.core.agent.Agent;
import org.metaagent.framework.core.security.SecurityLevel;
import org.metaagent.framework.core.security.approval.AsyncEventPermissionApprovalManager;
import org.metaagent.framework.core.security.approval.PermissionApproval;
import org.metaagent.framework.core.security.approval.PermissionApprovalManager;
import org.metaagent.framework.core.security.approval.PermissionApprover;
import org.metaagent.framework.core.tool.approval.ToolApprovalRequest;
import org.metaagent.framework.core.tool.config.DefaultToolExecutionConfig;
import org.metaagent.framework.core.tool.config.ToolExecutionConfig;
import org.metaagent.framework.core.tool.exception.ToolRejectException;

import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

/**
 * Default implementation of {@link ToolContext} interface.
 *
 * @author vyckey
 */
@Getter
public class DefaultToolContext implements ToolContext {
    private final Agent<?, ?> agent;
    private final ToolExecutionConfig toolExecutionConfig;
    private final SecurityLevel securityLevel;
    private final PermissionApprovalManager<ToolApprovalRequest> approvalManager;
    private final AbortSignal abortSignal;
    private String executionId;

    protected DefaultToolContext(Builder builder) {
        this.agent = builder.agent;
        this.toolExecutionConfig = Objects.requireNonNull(builder.toolExecutionConfig, "ToolExecutionConfig must not be null");
        this.securityLevel = Objects.requireNonNull(builder.securityLevel, "SecurityLevel must not be null");
        this.approvalManager = Objects.requireNonNull(builder.approvalManager, "ApprovalManager must not be null");
        this.abortSignal = Objects.requireNonNull(builder.abortSignal, "AbortSignal must not be null");
    }

    public static Path defaultWorkingDirectory() {
        if (System.getProperty("CWD") != null) {
            return Path.of(System.getProperty("CWD")).toAbsolutePath();
        } else {
            return Path.of(".").toAbsolutePath().normalize();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public <I, O> Agent<I, O> getAgent() {
        //noinspection unchecked
        return (Agent<I, O>) agent;
    }

    @Override
    public PermissionApproval requestApproval(ToolApprovalRequest approvalRequest) throws ToolRejectException {
        try {
            CompletableFuture<PermissionApproval> approvalFuture = getApprovalManager().initiateApproval(approvalRequest);
            return approvalFuture.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ToolRejectException(approvalRequest.toolName(), "approval process was interrupted");
        } catch (ExecutionException e) {
            Throwable reason = e.getCause();
            throw new ToolRejectException(approvalRequest.toolName(), "approval process occurred exception:" + reason.getMessage(), reason);
        }
    }

    @Override
    public void setExecutionId(String executionId) {
        this.executionId = Objects.requireNonNull(executionId, "executionId cannot be null");
    }

    public static class Builder implements ToolContext.Builder {
        private Agent<?, ?> agent;
        private ToolExecutionConfig toolExecutionConfig;
        private SecurityLevel securityLevel;
        private PermissionApprovalManager<ToolApprovalRequest> approvalManager;
        private AbortSignal abortSignal;

        @Override
        public ToolContext.Builder agent(Agent<?, ?> agent) {
            this.agent = agent;
            return this;
        }

        @Override
        public ToolContext.Builder toolExecutionConfig(ToolExecutionConfig toolExecutionConfig) {
            this.toolExecutionConfig = toolExecutionConfig;
            return this;
        }

        @Override
        public ToolContext.Builder securityLevel(SecurityLevel securityLevel) {
            this.securityLevel = securityLevel;
            return this;
        }

        @Override
        public ToolContext.Builder approvalManager(PermissionApprovalManager<ToolApprovalRequest> approvalManager) {
            this.approvalManager = approvalManager;
            return this;
        }

        @Override
        public ToolContext.Builder abortSignal(AbortSignal abortSignal) {
            this.abortSignal = abortSignal;
            return this;
        }

        @Override
        public DefaultToolContext build() {
            if (toolExecutionConfig == null) {
                toolExecutionConfig = DefaultToolExecutionConfig.builder().build();
            }
            if (securityLevel == null) {
                this.securityLevel = SecurityLevel.RESTRICTED_DEFAULT_SALE;
            }
            if (approvalManager == null) {
                this.approvalManager = AsyncEventPermissionApprovalManager.assign(
                        PermissionApprover.alwaysApproved(),
                        ForkJoinPool.commonPool()
                );
            }
            if (abortSignal == null) {
                this.abortSignal = AbortController.global().signal();
            }
            return new DefaultToolContext(this);
        }
    }
}
