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

package org.metaagent.framework.core.agent.action.history;

import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Implementation of the ActionHistory interface.
 * This class maintains a history of action records.
 * It provides methods to add, retrieve, and filter action records.
 * It also allows iteration over the action records.
 *
 * @see ActionHistory
 * @see ActionRecord
 * <p>
 * author vyckey
 */
public class DefaultActionHistory implements ActionHistory {
    private final List<ActionRecord> records;

    public DefaultActionHistory(List<ActionRecord> records) {
        this.records = records;
    }

    public DefaultActionHistory() {
        this.records = Lists.newArrayList();
    }

    @Override
    public void addRecord(ActionRecord record) {
        records.add(record);
    }

    @Override
    public void removeRecord(ActionRecord record) {
        this.records.remove(record);
    }

    @Override
    public void clearAll() {
        this.records.clear();
    }

    @Override
    public List<ActionRecord> findRecords(Predicate<ActionRecord> predicate) {
        return records.stream().filter(predicate).collect(Collectors.toList());
    }

    @Override
    public Optional<ActionRecord> findRecord(Predicate<ActionRecord> predicate) {
        return records.stream().filter(predicate).findFirst();
    }

    @Override
    public Optional<ActionRecord> lastRecord() {
        return records.stream().reduce((first, second) -> second);
    }

    @Override
    public Iterator<ActionRecord> iterator() {
        return records.iterator();
    }
}