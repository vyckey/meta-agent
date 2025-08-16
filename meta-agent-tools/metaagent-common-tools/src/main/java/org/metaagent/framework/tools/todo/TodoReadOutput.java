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

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.apache.commons.collections.CollectionUtils;
import org.metaagent.framework.core.tool.schema.ToolDisplayable;

import java.util.List;
import java.util.stream.Collectors;

public record TodoReadOutput(
        @JsonPropertyDescription("TODO list ID")
        String todoId,

        @JsonPropertyDescription("TODO items")
        List<TodoItem> todos
) implements ToolDisplayable {
    @Override
    public String display() {
        if (CollectionUtils.isEmpty(todos)) {
            return "No TODO items found";
        }
        final int displaySize = Math.min(10, todos.size());
        String todoList = todos.subList(0, displaySize).stream()
                .map(todo -> "- " + todo).collect(Collectors.joining("\n"));
        if (displaySize < todos.size()) {
            todoList += "\n...and " + (todos.size() - displaySize) + " more";
        }
        return "TODO (ID=" + todoId + ") items:\n" + todoList;
    }
}
