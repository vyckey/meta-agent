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

package org.metaagent.framework.core.tool.skill;

import org.metaagent.framework.common.metadata.MetadataProvider;

import java.nio.file.Path;

/**
 * Skill metadata
 *
 * @author vyckey
 */
public interface SkillMetadata extends MetadataProvider {
    String SKILLS_DIRNAME = "skills";
    String SKILLS_FILENAME = "SKILL.md";

    /**
     * Gets skill name
     *
     * @return the skill name
     */
    String name();

    /**
     * Gets skill description
     *
     * @return the skill description
     */
    String description();

    /**
     * Gets skill version
     *
     * @return the skill version
     */
    String version();

    /**
     * Gets the skill file path
     *
     * @return the file path of skill
     */
    Path filePath();
}
