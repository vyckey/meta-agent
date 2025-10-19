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

import java.util.List;

/**
 * Task interface represents a unit of work in the agent framework.
 *
 * @author vyckey
 */
public interface Task {
    /**
     * Unique identifier for the task.
     *
     * @return the task ID
     */
    String getId();

    /**
     * Name of the task.
     *
     * @return the task name
     */
    String getName();

    /**
     * Description of the task.
     *
     * @return the task description
     */
    String getDescription();

    /**
     * Status of the task.
     *
     * @return the current status of the task
     */
    TaskStatus getStatus();

    /**
     * Sets the status of the task.
     *
     * @param status the new status to set
     */
    void setStatus(TaskStatus status);

    /**
     * Parent task of this task.
     *
     * @return the parent task
     */
    Task getParentTask();

    /**
     * Sets the parent task of this task.
     *
     * @param parentTask the parent task to set
     */
    void setParentTask(Task parentTask);

    /**
     * List of dependent tasks that must be completed before this task can be executed.
     *
     * @return the list of dependent tasks
     */
    List<Task> getDependentTasks();

    /**
     * Sets the dependent tasks for this task.
     *
     * @param dependentTasks the list of dependent tasks to set
     */
    void setDependentTasks(List<Task> dependentTasks);

    /**
     * Adds dependent tasks to this task.
     *
     * @param tasks the tasks to add as dependencies
     */
    void addDependentTasks(Task... tasks);

    /**
     * Result of the task execution.
     *
     * @return the result of the task execution
     */
    TaskResult getResult();

    /**
     * Sets the result of the task execution.
     *
     * @param result the result to set
     */
    void setResult(TaskResult result);

    /**
     * Reset the task to its initial state.
     */
    void reset();
}
