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

import org.metaagent.framework.core.skill.Skill;
import org.metaagent.framework.core.skill.loader.SkillLoadError;

import java.net.URL;
import java.util.List;

/**
 * Skill change listener
 *
 * @author vyckey
 */
public interface SkillChangeListener {
    /**
     * Callback when a skill is added
     *
     * @param location the skill location
     * @param skill    the skill
     */
    default void onSkillAdded(URL location, Skill skill) {
    }

    /**
     * Callback when a skill is updated
     *
     * @param location the skill location
     * @param skill    the skill
     */
    default void onSkillUpdated(URL location, Skill skill) {
    }

    /**
     * Callback when a skill is removed
     *
     * @param location  the skill location
     * @param skillName the skill name
     */
    default void onSkillRemoved(URL location, String skillName) {

    }

    /**
     * Callback when a skill load error
     *
     * @param location the skill location
     * @param error    the skill load error
     */
    default void onSkillLoadError(URL location, List<SkillLoadError> error) {
    }
}
