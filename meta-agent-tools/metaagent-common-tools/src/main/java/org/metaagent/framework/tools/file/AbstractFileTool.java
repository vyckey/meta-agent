/*
 * MIT License
 *
 * Copyright (c) 2026 MetaAgent
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

package org.metaagent.framework.tools.file;

import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.core.security.SecurityLevel;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.config.ToolPattern;
import org.metaagent.framework.core.tool.exception.ToolRejectException;
import org.metaagent.framework.tools.file.util.FileUtils;

import java.nio.file.Path;

/**
 * AbstractFileTool is an abstract class that provides a common implementation for file-related tools.
 *
 * @param <I> The input type of the tool.
 * @param <O> The output type of the tool.
 * @author vyckey
 */
public abstract class AbstractFileTool<I, O> implements Tool<I, O> {
    /**
     * Checks whether a file is accessible based on the provided tool context and file path.
     * If the file is not accessible, a ToolRejectException is thrown.
     *
     * @param toolContext The tool context.
     * @param filePath    The file path.
     * @return True if the file is accessible, false otherwise.
     * @throws ToolRejectException If the file is not accessible.
     */
    protected boolean checkFileAccessible(ToolContext toolContext, Path filePath) {
        // If the security level is unrestricted, allow all files
        if (toolContext.getSecurityLevel().compareTo(SecurityLevel.UNRESTRICTED_DANGEROUSLY) <= 0) {
            return true;
        }

        Path normalizedFilePath = filePath.normalize();
        // If the file is within the working directory, allow it
        if (FileUtils.inFileDirectory(normalizedFilePath, toolContext.getWorkingDirectory())) {
            return true;
        }

        // Check if the file is within the disallowed or allowed directories
        Path relativizeFilePath = filePath.relativize(toolContext.getWorkingDirectory());
        for (ToolPattern disallowedTool : toolContext.getToolExecutionConfig().disallowedTools()) {
            if (StringUtils.isEmpty(disallowedTool.pattern())) {
                throw new ToolRejectException(disallowedTool.toolName(), "not allowed execution");
            } else if (FileUtils.matchPath(normalizedFilePath, disallowedTool.pattern())
                    || FileUtils.matchPath(relativizeFilePath, disallowedTool.pattern())) {
                throw new ToolRejectException(disallowedTool.toolName(), "disallowed execution: " + disallowedTool.pattern());
            }
        }
        for (ToolPattern allowedTool : toolContext.getToolExecutionConfig().allowedTools()) {
            if (StringUtils.isEmpty(allowedTool.pattern())
                    || FileUtils.matchPath(normalizedFilePath, allowedTool.pattern())
                    || FileUtils.matchPath(relativizeFilePath, allowedTool.pattern())) {
                return true;
            }
        }
        return false;
    }
}
