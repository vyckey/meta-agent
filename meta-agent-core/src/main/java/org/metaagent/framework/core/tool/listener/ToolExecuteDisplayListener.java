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

package org.metaagent.framework.core.tool.listener;

import lombok.extern.slf4j.Slf4j;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolExecutionException;
import org.metaagent.framework.core.tool.schema.ToolDisplayable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class ToolExecuteDisplayListener implements ToolExecuteListener {
    private static final Logger logger = LoggerFactory.getLogger("Tool");
    public static final ToolExecuteDisplayListener INSTANCE = new ToolExecuteDisplayListener();

    private ToolExecuteDisplayListener() {
    }

    @Override
    public <I> void onToolInput(Tool<I, ?> tool, I input) {
        if (input instanceof ToolDisplayable) {
            String display = ((ToolDisplayable) input).display();
            logger.info("Tool [{}] Input > {}", tool.getName(), display);
        }
    }

    @Override
    public <I, O> void onToolOutput(Tool<I, O> tool, I input, O output) {
        if (input instanceof ToolDisplayable) {
            String inputDisplay = ((ToolDisplayable) output).display();
            logger.info("Tool [{}] Output > {}", tool.getName(), inputDisplay);
        }
    }

    @Override
    public <I> void onToolException(Tool<I, ?> tool, I input, ToolExecutionException exception) {
        logger.error("Tool [{}] Exception > {}", tool.getName(), exception.getMessage(), exception);
    }
}
