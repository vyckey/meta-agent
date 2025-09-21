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

package org.metaagent.framework.core.common.security.approver;

import org.metaagent.framework.core.common.metadata.MetadataProvider;

/**
 * HumanApprovalInput represents the input required for human approval.
 *
 * @param content  The content that needs approval.
 * @param metadata Additional metadata related to the approval request.
 * @author vyckey
 */
public record HumanApprovalInput(String content, MetadataProvider metadata) {
    public static final String METADATA_TOOL_NAME = "TOOL_NAME";

    public static HumanApprovalInput ofTool(String toolName, String content, MetadataProvider metadata) {
        metadata.setProperty(METADATA_TOOL_NAME, toolName);
        return new HumanApprovalInput(content, metadata);
    }
}
