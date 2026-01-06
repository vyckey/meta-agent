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

package org.metaagent.framework.tools.todo.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.metaagent.framework.tools.todo.TodoItem;
import org.metaagent.framework.tools.todo.TodoItemUpdate;
import org.metaagent.framework.tools.todo.TodoUpdateResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * FileBasedTodoService is a simple implementation of TodoService that uses a file system to store TODO items.
 *
 * @author vyckey
 */
public class FileBasedTodoService implements TodoService {
    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final Path todoDirectory;

    public FileBasedTodoService(Path todoDirectory) {
        this.todoDirectory = Objects.requireNonNull(todoDirectory, "todoDirectory is required");
    }

    @Override
    public List<TodoItem> getTodos(String todoId) {
        Path filePath = todoDirectory.resolve(todoId + ".json");
        File todoFile = filePath.toFile();
        if (!todoFile.exists()) {
            return List.of();
        }
        try {
            return OBJECT_MAPPER.readValue(todoFile, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new IllegalStateException("Read todo file error: " + e.getMessage());
        }
    }

    @Override
    public TodoUpdateResult updateTodos(String todoId, List<TodoItemUpdate> todos) {
        if (!Files.exists(todoDirectory)) {
            try {
                Files.createDirectories(todoDirectory);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to create TODO directory " + todoDirectory, e);
            }
        }

        List<TodoItem> oldTodoItems = getTodos(todoId);
        List<TodoItem> newTodoItems = mergeTodoItems(oldTodoItems, todos);
        Path filePath = todoDirectory.resolve(todoId + ".json");
        try {
            File todoFile = filePath.toFile();
            OBJECT_MAPPER.writeValue(todoFile, newTodoItems);
            return new TodoUpdateResult(oldTodoItems, newTodoItems);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write TODO file " + filePath, e);
        }
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

}
