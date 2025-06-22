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

package org.metaagent.framework.core.agent.task;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of the Task interface.
 * This class provides a basic structure for tasks with properties such as ID, name, description, status,
 * parent task, dependent tasks, and result.
 *
 * @author vyckey
 */
@Getter
@EqualsAndHashCode(of = "id")
public class DefaultTask implements Task {
    private final String id;
    private String name;
    private String description;
    private TaskStatus status = TaskStatus.CREATED;
    private Task parentTask;
    private final List<Task> dependentTasks = new ArrayList<>(0);
    private TaskResult result;

    public DefaultTask(String id, String name) {
        if (StringUtils.isEmpty(id)) {
            throw new IllegalArgumentException("Task ID cannot be null or empty");
        }
        this.id = id.trim();
        setName(name);
    }

    public DefaultTask(String id, String name, String description) {
        this(id, name);
        this.description = description;
    }

    public void setName(String name) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Task name cannot be null or empty");
        }
        this.name = name;
    }

    @Override
    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    @Override
    public void setParentTask(Task parentTask) {
        this.parentTask = parentTask;
        this.parentTask.addDependentTasks(this);
    }

    @Override
    public void setDependentTasks(List<Task> dependentTasks) {
        this.dependentTasks.clear();
        this.dependentTasks.addAll(dependentTasks);
        for (Task task : dependentTasks) {
            task.setParentTask(this);
        }
    }

    @Override
    public void addDependentTasks(Task... tasks) {
        for (Task task : tasks) {
            if (!this.dependentTasks.contains(task)) {
                this.dependentTasks.add(task);
                task.setParentTask(this);
            }
        }
    }

    @Override
    public void setResult(TaskResult result) {
        this.result = result;
    }

    @Override
    public void reset() {
        this.status = TaskStatus.CREATED;
        this.result = null;
    }

    @Override
    public String toString() {
        return "Task{id=\"" + id + "\", name=\"" + name + "\"}";
    }
}
