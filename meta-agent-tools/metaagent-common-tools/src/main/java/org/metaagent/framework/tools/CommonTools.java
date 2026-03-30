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

import org.metaagent.framework.core.tool.Tool;
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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * CommonTools is a class that provides a collection of common tools for file operations, HTTP requests, scripting, and more.
 *
 * @author MetaAgent
 */
public enum CommonTools {
    GLOB_FILES_TOOL(GlobFileTool.TOOL_NAME, GlobFileTool::new),
    GREP_TOOL(GrepFileTool.TOOL_NAME, GrepFileTool::new),
    LIST_FILES_TOOL(ListFileTool.TOOL_NAME, ListFileTool::new),
    READ_TEX_FILE_TOOL(ReadTextFileTool.TOOL_NAME, ReadTextFileTool::new),
    READ_MANY_FILES_TOOL(ReadManyFilesTool.TOOL_NAME, ReadManyFilesTool::new),
    WRITE_TEXT_FILE_TOOL(WriteTextFileTool.TOOL_NAME, WriteTextFileTool::new),
    EDIT_TEX_FILE_TOOL(EditTextFileTool.TOOL_NAME, EditTextFileTool::new),
    READ_IMAGE_FILE_TOOL(ReadImageFileTool.TOOL_NAME, ReadImageFileTool::new),
    GET_CURRENT_TIME_TOOL(GetCurrentTimeTool.TOOL_NAME, GetCurrentTimeTool::new),
    TODO_READ_TOOL(TodoReadTool.TOOL_NAME, TodoReadTool::new),
    TODO_WRITE_TOOL(TodoWriteTool.TOOL_NAME, TodoWriteTool::new),
    SHELL_COMMAND_TOOL(ShellCommandTool.TOOL_NAME, ShellCommandTool::new),
    HTTP_REQUEST_TOOL(HttpRequestTool.TOOL_NAME, HttpRequestTool::new),
    HUMAN_INPUT_TOOL(HumanInputTool.TOOL_NAME, HumanInputTool::new),
    SCRIPT_ENGINE_TOOL(ScriptEngineTool.TOOL_NAME, ScriptEngineTool::new),
    ;

    private static volatile Toolkit toolkit;
    private final String name;
    private final Supplier<Tool<?, ?>> toolInstantiator;
    private volatile Tool<?, ?> instance;

    CommonTools(String name, Supplier<Tool<?, ?>> toolInstantiator) {
        this.name = name;
        this.toolInstantiator = toolInstantiator;
    }

    public String getName() {
        return name;
    }

    public Tool<?, ?> getInstance() {
        if (instance == null) {
            synchronized (this) {
                if (instance == null) {
                    instance = Objects.requireNonNull(toolInstantiator.get(), "Cannot instantiate tool " + name);
                }
            }
        }
        return instance;
    }

    public static List<String> getToolNames() {
        return Arrays.stream(CommonTools.values()).map(CommonTools::name).toList();
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
        Tool<?, ?>[] tools = Arrays.stream(CommonTools.values()).map(CommonTools::getInstance).toArray(Tool[]::new);
        return Toolkit.fromTools(
                "Common Tools",
                "A collection of common tools for file operations, HTTP requests, scripting, and more.",
                tools
        );
    }
}
