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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.common.collect.Lists;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * A simple POJO representing a TodoItem.
 *
 * @author vyckey
 */
@Builder(toBuilder = true)
public record TodoItem(
        @JsonProperty(required = true)
        @JsonPropertyDescription("Unique ID of TODO item. (e.g. 1, 2, or a, b)")
        String id,

        @JsonProperty(required = true)
        @JsonPropertyDescription("Content of TODO item")
        String content,

        @JsonPropertyDescription("TODO item status (e.g. pending, in_progress, completed, cancelled). Optional, default is pending.")
        TodoStatus status,

        @JsonPropertyDescription("TODO item priority (e.g. high, medium, low). Optional, default is medium.")
        TodoPriority priority,

        @JsonPropertyDescription("Tags of TODO item. Optional, default is empty.")
        List<String> tags,

        @JsonPropertyDescription("Previous status of TODO item")
        TodoStatus previousStatus,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        @JsonPropertyDescription("Create time of TODO item")
        Date createdAt,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        @JsonPropertyDescription("Last update time of TODO item")
        Date updatedAt
) implements Comparable<TodoItem> {
    public static TodoItem create(String id, String content, TodoPriority priority, List<String> tags) {
        Date now = new Date();
        return TodoItem.builder()
                .id(id)
                .content(content)
                .status(TodoStatus.PENDING)
                .priority(priority)
                .tags(Optional.ofNullable(tags).orElse(Lists.newArrayList()))
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Override
    public int compareTo(@NotNull TodoItem other) {
        return Comparator.comparing(TodoItem::status)
                .thenComparing(TodoItem::priority)
                .thenComparing(TodoItem::updatedAt)
                .compare(this, other);
    }

    @NotNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        if (TodoStatus.COMPLETED.equals(status)) {
            sb.append("x");
        } else if (TodoStatus.CANCELED.equals(status)) {
            sb.append("-");
        } else {
            sb.append(" ");
        }
        sb.append("] ").append(content).append(" ");
        sb.append("(").append(priority).append(" ").append(String.join(",", tags)).append(")");
        return sb.toString();
    }
}
