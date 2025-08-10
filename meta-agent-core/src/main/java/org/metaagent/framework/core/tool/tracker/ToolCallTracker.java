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

import java.util.List;
import java.util.function.Predicate;

/**
 * Interface for tracking tool calls.
 *
 * @author vyckey
 */
public interface ToolCallTracker {
    /**
     * Returns an empty implementation of ToolCallTracker.
     *
     * @return an empty ToolCallTracker
     */
    static ToolCallTracker empty() {
        return EmptyToolCallTracker.INSTANCE;
    }

    /**
     * Creates a new instance of ToolCallTracker.
     *
     * @return a new ToolCallTracker instance
     */
    static ToolCallTracker create() {
        return new DefaultToolCallTracker();
    }

    /**
     * Tracks a tool call record.
     *
     * @param record the tool call record to track
     */
    void track(ToolCallRecord record);

    /**
     * Finds tool call records that match the given predicate.
     *
     * @param predicate the predicate to filter records
     * @return a list of matching tool call records
     */
    List<ToolCallRecord> find(Predicate<ToolCallRecord> predicate);

    /**
     * Finds tool call records by tool name.
     *
     * @param toolName the name of the tool to search for
     * @return a list of tool call records matching the tool name
     */
    List<ToolCallRecord> findByToolName(String toolName);

    /**
     * Merges another ToolCallTracker into this one.
     *
     * @param other the ToolCallTracker to merge
     */
    void merge(ToolCallTracker other);

    /**
     * Clears all tracked tool call records.
     */
    void clear();
}
