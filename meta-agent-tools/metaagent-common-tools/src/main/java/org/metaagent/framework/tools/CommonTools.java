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

package org.metaagent.framework.tools;

import org.metaagent.framework.core.tool.toolkit.Toolkit;
import org.metaagent.framework.tools.file.find.GlobFileTool;
import org.metaagent.framework.tools.file.find.GrepFileTool;
import org.metaagent.framework.tools.file.image.ReadImageFileTool;
import org.metaagent.framework.tools.file.list.ListFileTool;
import org.metaagent.framework.tools.file.text.EditTextFileTool;
import org.metaagent.framework.tools.file.text.ReadManyFilesTool;
import org.metaagent.framework.tools.file.text.ReadTextFileTool;
import org.metaagent.framework.tools.file.text.WriteTextFileTool;
import org.metaagent.framework.tools.http.HttpRequestTool;
import org.metaagent.framework.tools.human.HumanInputTool;
import org.metaagent.framework.tools.script.engine.ScriptEngineTool;
import org.metaagent.framework.tools.script.shell.ShellCommandTool;
import org.metaagent.framework.tools.time.GetCurrentTimeTool;
import org.metaagent.framework.tools.todo.TodoReadTool;
import org.metaagent.framework.tools.todo.TodoWriteTool;

import java.util.List;

/**
 * CommonTools is a class that provides a collection of common tools for file operations, HTTP requests, scripting, and more.
 * It is designed to be used as a singleton, and all methods are static.
 *
 * @author MetaAgent
 */
public abstract class CommonTools {
    public static final String GLOB_FILES_TOOL_NAME = GlobFileTool.TOOL_NAME;
    public static final String GREP_TOOL_NAME = GrepFileTool.TOOL_NAME;
    public static final String LIST_FILES_TOOL_NAME = ListFileTool.TOOL_NAME;
    public static final String READ_TEX_FILE_TOOL_NAME = ReadTextFileTool.TOOL_NAME;
    public static final String READ_MANY_FILES_TOOL_NAME = ReadManyFilesTool.TOOL_NAME;
    public static final String WRITE_TEXT_FILE_TOOL_NAME = WriteTextFileTool.TOOL_NAME;
    public static final String EDIT_TEX_FILE_TOOL_NAME = EditTextFileTool.TOOL_NAME;
    public static final String READ_IMAGE_FILE_NAME = ReadImageFileTool.TOOL_NAME;
    public static final String GET_CURRENT_TIME_TOOL_NAME = GetCurrentTimeTool.TOOL_NAME;
    public static final String TODO_READ_TOOL_NAME = TodoReadTool.TOOL_NAME;
    public static final String TODO_WRITE_TOOL_NAME = TodoWriteTool.TOOL_NAME;
    public static final String SHELL_COMMAND_TOOL_NAME = ShellCommandTool.TOOL_NAME;
    public static final String HTTP_REQUEST_TOOL_NAME = HttpRequestTool.TOOL_NAME;
    public static final String HUMAN_INPUT_TOOL_NAME = HumanInputTool.TOOL_NAME;
    public static final String SCRIPT_ENGINE_TOOL_NAME = ScriptEngineTool.TOOL_NAME;
    private static final List<String> TOOL_NAMES;
    private static volatile Toolkit toolkit;

    static {
        TOOL_NAMES = List.of(
                GLOB_FILES_TOOL_NAME,
                GREP_TOOL_NAME,
                LIST_FILES_TOOL_NAME,
                READ_TEX_FILE_TOOL_NAME,
                READ_MANY_FILES_TOOL_NAME,
                WRITE_TEXT_FILE_TOOL_NAME,
                EDIT_TEX_FILE_TOOL_NAME,
                READ_IMAGE_FILE_NAME,
                GET_CURRENT_TIME_TOOL_NAME,
                TODO_READ_TOOL_NAME,
                TODO_WRITE_TOOL_NAME,
                SHELL_COMMAND_TOOL_NAME,
                HTTP_REQUEST_TOOL_NAME,
                HUMAN_INPUT_TOOL_NAME,
                SCRIPT_ENGINE_TOOL_NAME
        );
    }

    public static List<String> getToolNames() {
        return TOOL_NAMES;
    }

    public static Toolkit getToolkit() {
        if (toolkit == null) {
            synchronized (CommonTools.class) {
                if (toolkit == null) {
                    toolkit = createToolkit();
                }
            }
        }
        return toolkit;
    }

    private static Toolkit createToolkit() {
        return Toolkit.fromTools(
                "Common Tools",
                "A collection of common tools for file operations, HTTP requests, scripting, and more.",
                new GlobFileTool(),
                new GrepFileTool(),
                new ListFileTool(),
                new ReadTextFileTool(),
                new ReadManyFilesTool(),
                new WriteTextFileTool(),
                new EditTextFileTool(),
                new ReadImageFileTool(),
                new GetCurrentTimeTool(),
                new TodoReadTool(),
                new TodoWriteTool(),
                new ShellCommandTool(),
                new HttpRequestTool(),
                new HumanInputTool(),
                new ScriptEngineTool()
        );
    }
}
