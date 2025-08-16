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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Builder;

import java.util.List;

/**
 * {@link TodoItem} update POJO.
 *
 * @author vyckey
 */
@Builder
public record TodoItemUpdate(
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
        List<String> tags
) {

    public TodoItemUpdate withDefault() {
        return TodoItemUpdate.builder()
                .id(id)
                .content(content)
                .status(status != null ? status : TodoStatus.PENDING)
                .priority(priority != null ? priority : TodoPriority.MEDIUM)
                .tags(tags != null ? tags : List.of())
                .build();
    }

}
