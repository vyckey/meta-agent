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

package org.metaagent.framework.core.skill.loader;

import org.metaagent.framework.core.skill.Skill;

import java.net.URL;
import java.util.List;

/**
 * SkillLoader is responsible for loading skills from various locations.
 * <p>
 * Skills tree structure:
 * <pre>
 *     skills/
 *     ├── skill1/
 *     │   │── SKILL.md
 *     │   │── references/
 *     │   │   └── reference1.md
 *     │   │── scripts/
 *     │   │   └── script1.py
 *     │   │── assets/
 *     │   │   └── asset1.md
 *     │   └── examples/
 *     │       └── example1.md
 *     ├── skill2/
 *     │   └── SKILL.md
 * </pre>
 *
 * @author vyckey
 */
public interface SkillLoader {
    String SKILLS_DIRNAME = "skills";
    String SKILL_FILENAME = "SKILL.md";

    /**
     * Checks whether the loader supports the given location.
     *
     * @param location The location to check.
     * @return True if the loader supports the location, false otherwise.
     */
    boolean supports(URL location);

    /**
     * Loads skills from the specified location.
     *
     * @param location The location to load skills from.
     * @param errors   A list to collect any errors encountered during loading.
     * @return A list of loaded skills.
     */
    List<Skill> load(URL location, List<SkillLoadError> errors);

}
