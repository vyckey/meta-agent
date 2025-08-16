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
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.ToolExecutionException;
import org.metaagent.framework.core.tool.ToolParameterException;
import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.converter.ToolConverters;
import org.metaagent.framework.core.tool.definition.ToolDefinition;
import org.metaagent.framework.core.util.abort.AbortException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * TodoWriteTool is a tool that writes a todo item to a file.
 *
 * @author vyckey
 */
public class TodoWriteTool implements Tool<TodoWriteInput, TodoWriteOutput> {
    private static final ToolDefinition TOOL_DEFINITION = ToolDefinition.builder("todo_write")
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
            throw new ToolParameterException("Todo ID must be specified.");
        }
        if (CollectionUtils.isEmpty(input.todos())) {
            throw new ToolParameterException("Todo items cannot be empty.");
        }
    }

    @Override
    public TodoWriteOutput run(ToolContext toolContext, TodoWriteInput input) throws ToolExecutionException {
        validateInput(input);

        Path todoFilePath = TodoReadTool.getTodoFilePath(toolContext.getWorkingDirectory(), input.todoId());
        if (!Files.exists(todoFilePath.getParent())) {
            try {
                Files.createDirectories(todoFilePath.getParent());
            } catch (IOException e) {
                throw new ToolExecutionException(e);
            }
        }

        if (toolContext.getAbortSignal().isAborted()) {
            throw new AbortException("Tool " + getName() + " is cancelled");
        }

        List<TodoItem> oldTodoItems;
        List<TodoItem> newTodoItems;
        try {
            oldTodoItems = TodoReadTool.readTodoFile(todoFilePath);
            newTodoItems = mergeTodoItems(oldTodoItems, input.todos());
            TodoWriteTool.writeTodoFile(todoFilePath, newTodoItems);
        } catch (IOException e) {
            throw new ToolExecutionException("Failed to write TODO file " + todoFilePath, e);
        }

        return TodoWriteOutput.builder()
                .oldTodos(oldTodoItems)
                .newTodos(newTodoItems)
                .summary(summary(input, newTodoItems))
                .build();
    }

    protected List<TodoItem> mergeTodoItems(List<TodoItem> oldTodoItems, List<TodoItemUpdate> todoItemUpdates) {
        Map<String, TodoItem> todoItemsMap = oldTodoItems.stream()
                .collect(Collectors.toMap(TodoItem::id, Function.identity()));
        for (TodoItemUpdate newTodoItem : todoItemUpdates) {
            newTodoItem = newTodoItem.withDefault();
            TodoItem oldTodoItem = todoItemsMap.get(newTodoItem.id());
            if (oldTodoItem != null) {
                TodoItem todoItem = oldTodoItem.toBuilder()
                        .content(newTodoItem.content())
                        .status(newTodoItem.status())
                        .priority(newTodoItem.priority())
                        .tags(newTodoItem.tags())
                        .previousStatus(oldTodoItem.status())
                        .updatedAt(new Date())
                        .build();
                todoItemsMap.put(todoItem.id(), todoItem);
            } else {
                TodoItem todoItem = TodoItem.create(newTodoItem.id(),
                        newTodoItem.content(), newTodoItem.priority(), newTodoItem.tags());
                todoItemsMap.put(todoItem.id(), todoItem);
            }
        }
        return todoItemsMap.values().stream().sorted().toList();
    }

    private static void writeTodoFile(Path todoFilePath, List<TodoItem> todoItems) throws IOException {
        TodoReadTool.OBJECT_MAPPER.writeValue(todoFilePath.toFile(), todoItems);
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
