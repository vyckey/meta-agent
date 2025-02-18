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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * description is here
 *
 * @author vyckey
 */
public class TaskGraphImpl implements TaskGraph {
    protected final Map<String, Task> taskMap;
    protected List<Task> tasks;

    public TaskGraphImpl() {
        this.taskMap = Maps.newHashMap();
        this.tasks = Lists.newArrayList();
    }

    @Override
    public int size() {
        return taskMap.size();
    }

    @Override
    public boolean isEmpty() {
        return taskMap.isEmpty();
    }

    @Override
    public boolean hasTask(String taskName) {
        return taskMap.containsKey(taskName);
    }

    @Override
    public Task getTask(String taskName) {
        return taskMap.get(taskName);
    }

    @Override
    public List<Task> getTasks() {
        return List.of();
    }

    @Override
    public List<Task> findTasks(Predicate<Task> predicate) {
        return List.of();
    }

    @Override
    public void addTask(Task task) {
        if (hasTask(task.getName())) {
            throw new IllegalArgumentException("Task \"" + task.getName() +
                    "\" already exists, use replaceTask instead.");
        }
        tasks.add(task);
        taskMap.put(task.getName(), task);
    }

    @Override
    public void addTasks(Collection<Task> tasks) {
        if (CollectionUtils.isEmpty(tasks)) {
            return;
        }

        for (Task task : tasks) {
            if (hasTask(task.getName())) {
                throw new IllegalArgumentException("Task \"" + task.getName() +
                        "\" already exists, use replaceTask instead.");
            }
        }

        addNewTasks(tasks);
        for (Task task : tasks) {
            taskMap.put(task.getName(), task);
        }
    }

    @Override
    public void replaceTask(Task task) {
        if (!hasTask(task.getName())) {
            throw new IllegalArgumentException("Task \"" + task.getName() + "\" does not exist.");
        }
        taskMap.put(task.getName(), task);
        tasks = List.of();
        addNewTasks(taskMap.values());
    }

    protected void addNewTasks(Collection<Task> tasks) {
        List<Task> newTasks = topologicalSort(tasks);
        if (CollectionUtils.isEmpty(this.tasks)) {
            this.tasks = newTasks;
        } else {
            int prefixLength = 0;
            for (int i = 0; i < Math.min(this.tasks.size(), newTasks.size()); i++) {
                if (this.tasks.get(i).equals(newTasks.get(i))) {
                    prefixLength++;
                } else {
                    break;
                }
            }
            List<Task> rebuildTasks = Lists.newArrayList();
            rebuildTasks.addAll(this.tasks.subList(0, prefixLength));
            rebuildTasks.addAll(newTasks.subList(prefixLength, newTasks.size()));
            this.tasks = rebuildTasks;
        }
    }

    @Override
    public Task removeTask(String taskName) {
        Task removedTask = this.taskMap.remove(taskName);
        if (removedTask != null) {
            this.tasks = List.of();
            addNewTasks(this.taskMap.values());
        }
        return removedTask;
    }

    @Override
    public Iterator<Task> iterator() {
        return this.tasks.iterator();
    }

    void dfs(Task task, Set<Task> visited, List<Task> sortedTasks) {
        if (visited.contains(task)) {
            return;
        }
        visited.add(task);
        for (Task nextTask : task.getDependentTasks()) {
            dfs(nextTask, visited, sortedTasks);
        }
        sortedTasks.add(task);
    }

    protected List<Task> topologicalSort(Collection<Task> tasks) {
        List<Task> sortedTasks = Lists.newArrayList();
        Set<Task> visited = Sets.newHashSet();
        for (Task task : tasks) {
            dfs(task, visited, sortedTasks);
        }
        return sortedTasks;
    }
}
