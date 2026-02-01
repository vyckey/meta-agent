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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.metaagent.framework.core.skill.Skill;
import org.metaagent.framework.core.skill.exception.SkillException;
import org.metaagent.framework.core.skill.exception.SkillLoadException;
import org.metaagent.framework.core.skill.loader.SkillLoader;
import org.metaagent.framework.core.skill.metadata.SkillMetadata;
import org.metaagent.framework.core.skill.metadata.SkillVersionComparator;
import org.metaagent.framework.core.skill.watcher.SkillChangeListener;
import org.metaagent.framework.core.skill.watcher.SkillWatcher;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

/**
 * Default implementation of {@link SkillManager}.
 *
 * @author vyckey
 */
@Slf4j
public class DefaultSkillManager implements SkillManager {
    private final Map<String, NavigableMap<String, Skill>> skills = Maps.newConcurrentMap();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final List<SkillLoader> skillLoaders = Lists.newCopyOnWriteArrayList();
    private final List<SkillWatcher> skillWatchers = Lists.newCopyOnWriteArrayList();
    private final List<SkillChangeListener> listeners = Lists.newCopyOnWriteArrayList();
    private final SkillChangeListener watcherListener = new SkillWatcherListener();
    private final List<URL> loadedSkillLocations = Lists.newCopyOnWriteArrayList();

    @Override
    public void registerSkillLoader(SkillLoader skillLoader) {
        skillLoaders.add(Objects.requireNonNull(skillLoader, "Skill loader cannot be null"));
    }

    @Override
    public List<SkillLoader> getSkillLoaders() {
        return List.copyOf(skillLoaders);
    }

    @Override
    public void registerSkillWatcher(SkillWatcher skillWatcher) {
        Objects.requireNonNull(skillWatcher, "Skill watcher cannot be null");
        skillWatcher.addListener(watcherListener);
        this.skillWatchers.add(skillWatcher);
    }

    @Override
    public List<SkillWatcher> getSkillWatchers() {
        return List.copyOf(skillWatchers);
    }

    @Override
    public void loadSkillsFrom(URL skillLocation) throws SkillLoadException {
        SkillLoader selectedLoader = null;
        for (SkillLoader skillLoader : skillLoaders) {
            if (skillLoader.supports(skillLocation)) {
                selectedLoader = skillLoader;
                List<Skill> loadedSkills = skillLoader.load(skillLocation);
                addSkills(loadedSkills);
                loadedSkillLocations.add(skillLocation);
                break;
            }
        }
        if (selectedLoader == null) {
            throw new SkillLoadException("No skill loader found for skill location: " + skillLocation);
        }
    }

    @Override
    public boolean isSkillLoaded(URL skillLocation) {
        return loadedSkillLocations.contains(skillLocation);
    }

    @Override
    public List<SkillMetadata> listSkills() {
        lock.readLock().lock();
        try {
            return skills.values().stream()
                    .flatMap(versionMap -> versionMap.values().stream())
                    .map(Skill::getMetadata).toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Skill getSkill(String skillName) {
        return getSkill(skillName, null);
    }

    @Override
    public Skill getSkill(String skillName, String version) {
        Objects.requireNonNull(skillName, "Skill name cannot be null");

        lock.readLock().lock();
        try {
            NavigableMap<String, Skill> versionMap = skills.get(skillName);
            if (versionMap == null || versionMap.isEmpty()) {
                return null;
            }

            if (version == null) {
                // Return the latest version
                return versionMap.lastEntry().getValue();
            } else {
                return versionMap.get(version);
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void addSkills(Collection<Skill> skills) {
        Objects.requireNonNull(skills, "Skills cannot be null");
        lock.writeLock().lock();
        try {
            for (Skill skill : skills) {
                String version = skill.getMetadata().version();
                NavigableMap<String, Skill> versionMap = this.skills.computeIfAbsent(skill.getName(),
                        k -> new ConcurrentSkipListMap<>(SkillVersionComparator.INSTANCE)
                );
                Skill existing = versionMap.get(version);
                if (existing != null) {
                    versionMap.put(version, skill);
                    notifyListeners(listener -> listener.onSkillUpdated(skill.getMetadata().location(), skill));
                } else {
                    versionMap.put(version, skill);
                    notifyListeners(listener -> listener.onSkillAdded(skill.getMetadata().location(), skill));
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public List<Skill> removeSkill(String skillName) {
        return removeSkillInternal(skillName, null);
    }

    @Override
    public Skill removeSkill(String skillName, String version) {
        List<Skill> removed = removeSkillInternal(skillName, version);
        return removed.isEmpty() ? null : removed.get(0);
    }

    private List<Skill> removeSkillInternal(String skillName, String version) {
        Objects.requireNonNull(skillName, "Skill name cannot be null");
        lock.writeLock().lock();
        try {
            NavigableMap<String, Skill> versionMap = skills.get(skillName);
            if (versionMap == null || versionMap.isEmpty()) {
                return Collections.emptyList();
            }

            List<Skill> removed = new ArrayList<>();
            if (version == null) {
                // Remove all versions
                removed.addAll(versionMap.values());
                skills.remove(skillName);

                for (Skill skill : removed) {
                    notifyListeners(listener -> listener.onSkillRemoved(skill.getMetadata().location(), skillName));
                }
            } else {
                // Remove a specific version
                Skill removedSkill = versionMap.remove(version);
                if (removedSkill != null) {
                    removed.add(removedSkill);

                    // If no more versions exist, remove the skill entry
                    if (versionMap.isEmpty()) {
                        skills.remove(skillName);
                    }

                    notifyListeners(listener -> listener.onSkillRemoved(removedSkill.getMetadata().location(), skillName));
                }
            }
            return removed;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void removeAll() {
        lock.writeLock().lock();
        try {
            Map<String, NavigableMap<String, Skill>> oldSkills = Maps.newHashMap(skills);
            skills.clear();

            for (Map.Entry<String, NavigableMap<String, Skill>> entry : oldSkills.entrySet()) {
                for (Skill skill : entry.getValue().values()) {
                    notifyListeners(listener -> listener.onSkillRemoved(skill.getMetadata().location(), entry.getKey()));
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void registerListener(SkillChangeListener listener) {
        this.listeners.add(Objects.requireNonNull(listener, "Listener cannot be null"));
    }

    @Override
    public void unregisterListener(SkillChangeListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public void unregisterAllListeners() {
        this.listeners.clear();
    }

    @Override
    public void close() throws SkillException {
        this.skillWatchers.forEach(watcher -> {
            try {
                watcher.removeListener(watcherListener);
                watcher.close();
            } catch (Exception e) {
                log.error("Error closing skill watcher: {}", watcher, e);
            }
        });
        this.skillWatchers.clear();
        unregisterAllListeners();
        this.skillLoaders.clear();
        this.skills.clear();
    }

    void notifyListeners(Consumer<SkillChangeListener> consumer) {
        for (SkillChangeListener listener : listeners) {
            try {
                consumer.accept(listener);
            } catch (Exception e) {
                log.error("Error notifying listener: {}", listener, e);
            }
        }
    }


    class SkillWatcherListener implements SkillChangeListener {
        @Override
        public void onSkillAdded(URL location, Skill skill) {
            lock.writeLock().lock();
            try {
                String skillName = skill.getName();
                String version = skill.getMetadata().version();

                NavigableMap<String, Skill> versionMap = skills.computeIfAbsent(skillName,
                        k -> new ConcurrentSkipListMap<>(SkillVersionComparator.INSTANCE)
                );
                versionMap.put(version, skill);
                notifyListeners(listener -> listener.onSkillAdded(location, skill));
            } finally {
                lock.writeLock().unlock();
            }
        }

        @Override
        public void onSkillUpdated(URL location, Skill skill) {
            lock.writeLock().lock();
            try {
                String skillName = skill.getName();
                String version = skill.getMetadata().version();

                NavigableMap<String, Skill> versionMap = skills.get(skillName);
                if (versionMap != null) {
                    versionMap.put(version, skill);
                    notifyListeners(listener -> listener.onSkillUpdated(location, skill));
                }
            } finally {
                lock.writeLock().unlock();
            }
        }

        @Override
        public void onSkillRemoved(URL location, String skillName) {
            lock.writeLock().lock();
            try {
                NavigableMap<String, Skill> versionMap = skills.get(skillName);
                if (versionMap != null) {
                    // Remove all versions matching the location
                    boolean removed = versionMap.entrySet().removeIf(entry ->
                            Objects.equals(location, entry.getValue().getMetadata().location()));
                    if (removed) {
                        notifyListeners(listener -> listener.onSkillRemoved(location, skillName));
                    }

                    if (versionMap.isEmpty()) {
                        skills.remove(skillName);
                    }
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
    }
}
