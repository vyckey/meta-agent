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

package org.metaagent.framework.core.tool.tracker;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * description is here
 *
 * @author vyckey
 */
public class DefaultToolCallTracker implements ToolCallTracker {
    protected final List<ToolCallRecord> records;

    public DefaultToolCallTracker(List<ToolCallRecord> records) {
        this.records = Objects.requireNonNull(records);
    }

    public DefaultToolCallTracker() {
        this(Lists.newArrayList());
    }

    @Override
    public void track(ToolCallRecord record) {
        this.records.add(record);
    }

    @Override
    public List<ToolCallRecord> find(Predicate<ToolCallRecord> predicate) {
        return this.records.stream().filter(predicate).toList();
    }

    @Override
    public List<ToolCallRecord> findByToolName(String toolName) {
        return find(record -> record.getToolName().equals(toolName));
    }

    @Override
    public void merge(ToolCallTracker other) {
        this.records.addAll(other.find(record -> true));
    }

    @Override
    public void clear() {
        this.records.clear();
    }

}
