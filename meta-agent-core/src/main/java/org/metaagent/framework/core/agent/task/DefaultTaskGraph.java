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

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Default implementation of TaskGraph interface.
 *
 * @author vyckey
 */
public class DefaultTaskGraph implements TaskGraph {
    protected final Map<String, Task> taskMap;
    protected final Set<String> rootTaskIds;
    protected List<Task> tasks;

    public DefaultTaskGraph() {
        this.taskMap = Maps.newHashMap();
        this.rootTaskIds = Sets.newHashSet();
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
    public boolean hasTask(String taskId) {
        return taskMap.containsKey(taskId);
    }

    @Override
    public Task getTask(String taskId) {
        return taskMap.get(taskId);
    }

    @Override
    public List<Task> getSortedTasks() {
        return Collections.unmodifiableList(tasks);
    }

    @Override
    public List<Task> findSortedTasks(Predicate<Task> predicate) {
        return tasks.stream().filter(predicate).collect(Collectors.toList());
    }

    private boolean checkAddTasks(Collection<Task> tasks) {
        boolean allNewTasks = true;
        Map<String, Task> addTaskMap = tasks.stream().collect(Collectors.toMap(Task::getId, task -> task));
        for (Task task : tasks) {
            if (hasTask(task.getId())) {
                throw new IllegalArgumentException("Task \"" + task.getId() +
                        "\" already exists, use replaceTask instead.");
            }
            for (Task dependentTask : task.getDependentTasks()) {
                String dependentTaskId = dependentTask.getId();
                if (!hasTask(dependentTaskId) && !addTaskMap.containsKey(dependentTaskId)) {
                    throw new IllegalArgumentException("Dependent task \"" + dependentTaskId +
                            "\" does not exist for task \"" + task.getId() + "\".");
                }
                if (hasTask(dependentTaskId)) {
                    allNewTasks = false;
                }
            }
        }
        return allNewTasks;
    }

    @Override
    public void addTask(Task task) {
        if (checkAddTasks(Collections.singleton(task))) {
            taskMap.put(task.getId(), task);
            tasks.add(task);
        } else {
            this.tasks = List.of();
            taskMap.put(task.getId(), task);
            addNewTasks(taskMap.values());
        }
    }

    @Override
    public void addTasks(Collection<Task> tasks) {
        if (CollectionUtils.isEmpty(tasks)) {
            return;
        }
        if (checkAddTasks(tasks)) {
            addNewTasks(tasks);
            tasks.forEach(task -> taskMap.put(task.getId(), task));
        } else {
            this.tasks = List.of();
            tasks.forEach(task -> taskMap.put(task.getId(), task));
            addNewTasks(taskMap.values());
        }
    }

    @Override
    public void replaceTask(Task task) {
        checkAddTasks(Collections.singleton(task));

        taskMap.put(task.getId(), task);
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
    public Task removeTask(String taskId) {
        Task removedTask = this.taskMap.remove(taskId);
        if (removedTask != null) {
            this.tasks = List.of();
            addNewTasks(this.taskMap.values());
        }
        return removedTask;
    }

    void dfs(Task task, Set<Task> visited, List<Task> path, List<Task> sortedTasks) {
        if (visited.contains(task)) {
            String circle = path.stream().map(Task::getId).collect(Collectors.joining("->"));
            throw new IllegalArgumentException("Task graph contains a circle: " + circle);
        }
        visited.add(task);
        path.add(task);
        for (Task nextTask : task.getDependentTasks()) {
            dfs(nextTask, visited, path, sortedTasks);
        }
        path.remove(path.size() - 1);
        sortedTasks.add(task);
    }

    protected List<Task> topologicalSort(Collection<Task> tasks) {
        List<Task> sortedTasks = Lists.newArrayList();
        List<Task> path = Lists.newArrayList();
        Set<Task> visited = Sets.newHashSet();
        for (Task task : tasks) {
            dfs(task, visited, path, sortedTasks);
        }
        return sortedTasks;
    }
}
