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

package org.metaagent.framework.core.skill.watcher;

import java.net.URL;
import java.util.List;

/**
 * SkillWatcher watches skill locations for changes and notifies listeners of any changes.
 *
 * @author vyckey
 */
public interface SkillWatcher extends AutoCloseable {
    /**
     * Returns the skill locations being watched.
     *
     * @return the skill locations being watched
     */
    List<URL> watchedLocations();

    /**
     * Watches the specified skill location for changes.
     *
     * @param skillLocation the skill location to watch
     */
    void watch(URL skillLocation);

    /**
     * Stops watching the specified skill location.
     *
     * @param skillLocation the skill location to stop watching
     */
    void unwatch(URL skillLocation);

    /**
     * Adds a listener to receive notifications of skill changes.
     *
     * @param listener the listener to add
     */
    void addListener(SkillChangeListener listener);

    /**
     * Removes a listener from receiving notifications of skill changes.
     *
     * @param listener the listener to remove
     */
    void removeListener(SkillChangeListener listener);

}
