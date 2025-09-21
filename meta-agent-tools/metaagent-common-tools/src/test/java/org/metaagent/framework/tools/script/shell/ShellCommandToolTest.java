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

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.metaagent.framework.core.tool.ToolContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * description is here
 *
 * @author vyckey
 */
class ShellCommandToolTest {

    @Test
    void testEchoCommand() {
        ShellCommandTool commandTool = new ShellCommandTool();
        ShellCommandInput commandInput = new ShellCommandInput("echo hello");
        ShellCommandOutput commandOutput = commandTool.run(ToolContext.create(), commandInput);
        assertEquals(0, commandOutput.getExitCode());
        assertEquals("hello\n", commandOutput.getStdOutput());
        assertTrue(StringUtils.isEmpty(commandOutput.getError()));
    }

    @Test
    void testInvalidCommand() {
        ShellCommandTool commandTool = new ShellCommandTool();
        ShellCommandInput commandInput = new ShellCommandInput("not_exist_command");
        ShellCommandOutput commandOutput = commandTool.run(ToolContext.create(), commandInput);
        assertEquals(-1, commandOutput.getExitCode());
        assertTrue(StringUtils.isNotEmpty(commandOutput.getError()));
    }
}