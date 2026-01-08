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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.common.abort.AbortException;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.converter.ToolConverters;
import org.metaagent.framework.core.tool.definition.ToolDefinition;
import org.metaagent.framework.core.tool.exception.ToolExecutionException;
import org.metaagent.framework.core.tool.exception.ToolArgumentException;
import org.metaagent.framework.tools.todo.service.FileBasedTodoService;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.metaagent.framework.tools.todo.TodoReadTool.getTodoDirectory;

/**
 * TodoWriteTool is a tool that writes a todo item to a file.
 *
 * @author vyckey
 */
public class TodoWriteTool implements Tool<TodoWriteInput, TodoWriteOutput> {
    public static final String TOOL_NAME = "todo_write";
    private static final ToolDefinition TOOL_DEFINITION = ToolDefinition.builder(TOOL_NAME)
            .description("Create or update TODO items for plan and complex tasks")
            .inputSchema(TodoWriteInput.class)
            .outputSchema(TodoWriteOutput.class)
            .isConcurrencySafe(false)
            .isReadOnly(false)
            .build();

    @Override
    public ToolDefinition getDefinition() {
        return TOOL_DEFINITION;
    }

    @Override
    public ToolConverter<TodoWriteInput, TodoWriteOutput> getConverter() {
        return ToolConverters.jsonConverter(TodoWriteInput.class);
    }

    protected void validateInput(TodoWriteInput input) {
        if (StringUtils.isEmpty(input.todoId())) {
            throw new ToolArgumentException("Todo ID must be specified.");
        }
        if (CollectionUtils.isEmpty(input.todos())) {
            throw new ToolArgumentException("Todo items cannot be empty.");
        }
    }

    @Override
    public TodoWriteOutput run(ToolContext toolContext, TodoWriteInput input) throws ToolExecutionException {
        validateInput(input);

        if (toolContext.getAbortSignal().isAborted()) {
            throw new AbortException("Tool " + getName() + " is cancelled");
        }

        try {
            Path todoDirectory = getTodoDirectory(toolContext.workingDirectory());
            FileBasedTodoService todoService = new FileBasedTodoService(todoDirectory);
            TodoUpdateResult updateResult = todoService.updateTodos(input.todoId(), input.todos());
            return TodoWriteOutput.builder()
                    .oldTodos(updateResult.oldTodos())
                    .newTodos(updateResult.newTodos())
                    .summary(summary(input, updateResult.newTodos()))
                    .build();
        } catch (IllegalStateException e) {
            throw new ToolExecutionException("Failed to write TODO " + input.todoId() + ": " + e, e);
        }
    }

    protected String summary(TodoWriteInput input, List<TodoItem> todoItems) {
        int updatedTodoCount = input.todos().size();
        Map<TodoStatus, Long> statusCounts = todoItems.stream()
                .collect(Collectors.groupingBy(TodoItem::status, Collectors.counting()));
        return String.format(
                "Updated %d TODO(s) (%d pending, %d in progress, %d completed, %d cancelled)",
                updatedTodoCount,
                statusCounts.getOrDefault(TodoStatus.PENDING, 0L),
                statusCounts.getOrDefault(TodoStatus.IN_PROGRESS, 0L),
                statusCounts.getOrDefault(TodoStatus.COMPLETED, 0L),
                statusCounts.getOrDefault(TodoStatus.CANCELED, 0L)
        );
    }
}
