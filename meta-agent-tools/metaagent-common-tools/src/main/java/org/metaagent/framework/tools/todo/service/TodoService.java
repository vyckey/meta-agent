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

import org.metaagent.framework.tools.todo.TodoItem;
import org.metaagent.framework.tools.todo.TodoItemUpdate;
import org.metaagent.framework.tools.todo.TodoUpdateResult;

import java.util.List;

/**
 * TodoService is an interface for managing TODO items.
 *
 * @author vyckey
 */
public interface TodoService {
    /**
     * Get all TODO items for a given todoId.
     *
     * @param todoId the todoId to get TODO items for
     * @return a list of TODO items for the given todoId
     */
    List<TodoItem> getTodos(String todoId);

    /**
     * Update TODO items for a given todoId.
     *
     * @param todoId the todoId to update TODO items for
     * @param todos  a list of TODO items to update
     * @return a TodoUpdateResult containing the old and new TODO items
     */
    TodoUpdateResult updateTodos(String todoId, List<TodoItemUpdate> todos);
}
