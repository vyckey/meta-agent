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

package org.metaagent.framework.core.skill.manager;

import org.metaagent.framework.core.skill.Skill;
import org.metaagent.framework.core.skill.exception.SkillException;
import org.metaagent.framework.core.skill.exception.SkillLoadException;
import org.metaagent.framework.core.skill.loader.SkillLoader;
import org.metaagent.framework.core.skill.metadata.SkillMetadata;
import org.metaagent.framework.core.skill.watcher.SkillChangeListener;
import org.metaagent.framework.core.skill.watcher.SkillWatcher;

import java.net.URL;
import java.util.Collection;
import java.util.List;

/**
 * A manager for loading, storing, and managing {@link Skill} instances.
 * <p>
 * The {@code SkillManager} provides a centralized registry for skills, supporting features such as:
 * <ul>
 *   <li>Dynamic loading of skills from various sources (file system, remote URLs, etc.)</li>
 *   <li>Multi-version skill management with semantic versioning support</li>
 *   <li>Hot-reloading through pluggable {@link SkillWatcher} implementations</li>
 *   <li>Event-driven notifications for skill lifecycle changes</li>
 *   <li>Thread-safe operations with read/write lock protection</li>
 * </ul>
 * <p>
 * Skills are organized by name and version, allowing multiple versions of the same skill
 * to coexist. When requesting a skill without specifying a version, the latest version
 * (according to semantic versioning rules) is returned.
 * <p>
 * Example usage:
 * <pre>{@code
 * // Create and configure skill manager
 * SkillManager manager = new DefaultSkillManager();
 *
 * // Register skill loaders for different protocols
 * manager.registerSkillLoader(SkillLoaders.fileSkillLoader());
 * manager.registerSkillLoader(SkillLoaders.remoteSkillLoader());
 *
 * // Register event listeners
 * manager.registerListener(new LoggingSkillChangeListener());
 *
 * // Load skills from a location
 * manager.loadSkillsFrom(new URL("file:///opt/skills"));
 * manager.loadSkillsFrom(new URL("https://skills.example.com"));
 *
 * // Get and use a skill
 * Skill mathSkill = manager.getSkill("math-helper");
 * String instruction = mathSkill.getInstruction();
 *
 * // Clean up resources
 * manager.close();
 * }</pre>
 * <p>
 * Implementations of this interface must be thread-safe and properly handle
 * resource cleanup when {@link #close()} is called.
 *
 * @author vyckey
 * @see Skill
 * @see SkillMetadata
 * @see SkillLoader
 * @see SkillWatcher
 * @see SkillChangeListener
 */
public interface SkillManager extends AutoCloseable {
    /**
     * Create a new skill manager.
     *
     * @return skill manager
     */
    static SkillManager create() {
        return new DefaultSkillManager();
    }

    /**
     * Register a skill loader.
     *
     * @param skillLoader skill loader
     */
    void registerSkillLoader(SkillLoader skillLoader);

    /**
     * Register a skill watcher.
     *
     * @param skillWatcher skill watcher
     */
    void registerSkillWatcher(SkillWatcher skillWatcher);

    /**
     * Load skills from a location.
     *
     * @param skillLocation skill location
     * @throws SkillLoadException skill load exception
     */
    void loadSkillsFrom(URL skillLocation) throws SkillLoadException;

    /**
     * List all skills.
     *
     * @return list of skills
     */
    List<SkillMetadata> listSkills();

    /**
     * Get a skill by name.
     *
     * @param skillName skill name
     * @return skill, or null if not found
     */
    Skill getSkill(String skillName);

    /**
     * Get a skill by name and version.
     *
     * @param skillName skill name
     * @param version   skill version, if null returns the latest version
     * @return skill, or null if not found
     */
    Skill getSkill(String skillName, String version);

    /**
     * Add skills.
     *
     * @param skills skills
     */
    void addSkills(Collection<Skill> skills);

    /**
     * Remove a skill.
     *
     * @param skillName skill name
     */
    List<Skill> removeSkill(String skillName);

    /**
     * Remove a skill with specific version.
     *
     * @param skillName skill name
     * @param version   skill version, if null removes all versions
     * @return removed skill
     */
    Skill removeSkill(String skillName, String version);

    /**
     * Remove all skills.
     */
    void removeAll();

    /**
     * Register a skill load listener.
     *
     * @param listener listener
     */
    void registerListener(SkillChangeListener listener);

    /**
     * Unregister a skill load listener.
     *
     * @param listener listener
     */
    void unregisterListener(SkillChangeListener listener);

    /**
     * Unregister all skill load listeners.
     */
    void unregisterAllListeners();

    /**
     * Close the skill manager.
     *
     * @throws SkillException skill exception
     */
    @Override
    void close() throws SkillException;
}
