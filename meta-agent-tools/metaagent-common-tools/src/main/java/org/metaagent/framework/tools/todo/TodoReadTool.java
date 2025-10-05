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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.common.abort.AbortException;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.converter.ToolConverters;
import org.metaagent.framework.core.tool.definition.ToolDefinition;
import org.metaagent.framework.core.tool.exception.ToolExecutionException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Tool for reading a todo list from a file.
 *
 * @author vyckey
 */
public class TodoReadTool implements Tool<TodoReadInput, TodoReadOutput> {
    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ToolDefinition TOOL_DEFINITION = ToolDefinition.builder("todo_read")
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

        Path todoFilePath = getTodoFilePath(toolContext.getToolConfig().workingDirectory(), todoReadInput.todoId().trim());
        try {
            List<TodoItem> todoItems = readTodoFile(todoFilePath);
            return new TodoReadOutput(todoReadInput.todoId(), todoItems);
        } catch (IOException e) {
            throw new ToolExecutionException("Failed to read TODO file " + todoFilePath, e);
        }
    }

    static Path getTodoFilePath(Path workingDirectory, String todoId) {
        Path todoFileDir = workingDirectory;
        if (StringUtils.isNotEmpty(System.getenv("TODO_FILE_DIR"))) {
            todoFileDir = Path.of(System.getenv("TODO_FILE_DIR"));
        }
        return todoFileDir.resolve(todoId + ".json");
    }

    static List<TodoItem> readTodoFile(Path todoFilePath) throws IOException {
        File file = todoFilePath.toFile();
        if (!file.exists()) {
            return List.of();
        }
        try {
            return OBJECT_MAPPER.readValue(file, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new IOException("Read todo file error: " + e.getMessage());
        }
    }
}
