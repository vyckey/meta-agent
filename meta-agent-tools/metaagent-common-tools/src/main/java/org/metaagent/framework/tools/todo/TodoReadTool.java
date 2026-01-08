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

package org.metaagent.framework.tools.todo;

import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.common.abort.AbortException;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.converter.ToolConverters;
import org.metaagent.framework.core.tool.definition.ToolDefinition;
import org.metaagent.framework.core.tool.exception.ToolExecutionException;
import org.metaagent.framework.tools.todo.service.FileBasedTodoService;

import java.nio.file.Path;
import java.util.List;

/**
 * Tool for reading a todo list from a file.
 *
 * @author vyckey
 */
public class TodoReadTool implements Tool<TodoReadInput, TodoReadOutput> {
    public static final String TOOL_NAME = "todo_read";
    private static final ToolDefinition TOOL_DEFINITION = ToolDefinition.builder(TOOL_NAME)
            .description("Read TODO items with specialized TODO list ID")
            .inputSchema(TodoWriteInput.class)
            .outputSchema(TodoWriteOutput.class)
            .isConcurrencySafe(true)
            .isReadOnly(true)
            .build();

    @Override
    public ToolDefinition getDefinition() {
        return TOOL_DEFINITION;
    }

    @Override
    public ToolConverter<TodoReadInput, TodoReadOutput> getConverter() {
        return ToolConverters.jsonConverter(TodoReadInput.class);
    }

    protected void validateInput(TodoReadInput input) {
        if (StringUtils.isEmpty(input.todoId())) {
            throw new ToolExecutionException("Todo ID must be specified.");
        }
    }

    @Override
    public TodoReadOutput run(ToolContext toolContext, TodoReadInput todoReadInput) throws ToolExecutionException {
        validateInput(todoReadInput);
        if (toolContext.getAbortSignal().isAborted()) {
            throw new AbortException("Tool " + getName() + " is cancelled");
        }

        try {
            Path todoDirectory = getTodoDirectory(toolContext.workingDirectory());
            FileBasedTodoService todoService = new FileBasedTodoService(todoDirectory);
            List<TodoItem> todos = todoService.getTodos(todoReadInput.todoId());
            return new TodoReadOutput(todoReadInput.todoId(), todos);
        } catch (IllegalStateException e) {
            throw new ToolExecutionException("Failed to read TODO " + todoReadInput.todoId() + ": " + e, e);
        }
    }

    static Path getTodoDirectory(Path workingDirectory) {
        Path todoDirectory = workingDirectory;
        if (StringUtils.isNotEmpty(System.getenv("TODO_FILE_DIR"))) {
            todoDirectory = Path.of(System.getenv("TODO_FILE_DIR"));
        }
        return todoDirectory;
    }

}
