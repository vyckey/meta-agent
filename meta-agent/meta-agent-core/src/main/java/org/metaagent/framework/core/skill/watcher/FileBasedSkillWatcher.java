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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.metaagent.framework.core.skill.Skill;
import org.metaagent.framework.core.skill.exception.SkillWatchException;
import org.metaagent.framework.core.skill.loader.SkillLoadError;
import org.metaagent.framework.core.skill.loader.SkillLoader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.metaagent.framework.core.skill.loader.SkillLoader.SKILL_FILENAME;

/**
 * FileBasedSkillWatcher is a skill watcher implementation that watches for changes in a directory of skill files.
 *
 * @author vyckey
 */
@Slf4j
public class FileBasedSkillWatcher implements SkillWatcher {
    public static final int DEFAULT_DEBOUNCE = 100;
    private final Map<String, URL> watchedLocations = Maps.newConcurrentMap();
    private final SkillLoader skillLoader;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final List<SkillChangeListener> listeners = Lists.newCopyOnWriteArrayList();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Map<String, ScheduledFuture<?>> pendingNotifiers = new ConcurrentHashMap<>();
    private final int debounceMillis;

    private WatchService watchService;
    private Thread watchThread;

    public FileBasedSkillWatcher(SkillLoader skillLoader, int debounceMillis) {
        if (debounceMillis < 0) {
            throw new IllegalArgumentException("debounceMillis must be >= 0");
        }
        this.skillLoader = Objects.requireNonNull(skillLoader, "skillLoader is required");
        this.debounceMillis = debounceMillis;
    }

    public FileBasedSkillWatcher(SkillLoader skillLoader) {
        this(skillLoader, DEFAULT_DEBOUNCE);
    }

    private synchronized void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        try {
            watchService = FileSystems.getDefault().newWatchService();

            watchThread = new Thread(this::watchLoop, "FileBasedSkillWatcher-Thread");
            watchThread.setDaemon(true);
            watchThread.start();
        } catch (IOException e) {
            throw new SkillWatchException("Failed to initialize skill watch service", e);
        }
    }

    @Override
    public List<URL> watchedLocations() {
        return List.copyOf(watchedLocations.values());
    }

    private void watchLoop() {
        try {
            while (running.get()) {
                WatchKey watchKey = watchService.take();
                Path dir = (Path) watchKey.watchable();
                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    Path relativePath = (Path) event.context();
                    Path fullPath = dir.resolve(relativePath);

                    onFileChanged(fullPath, kind);

                    if (kind == StandardWatchEventKinds.ENTRY_CREATE && Files.isDirectory(fullPath)) {
                        watchDirectory(fullPath);
                    }
                }
                watchKey.reset();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void watchDirectory(Path dir) {
        List<Path> dirsToWatch = Lists.newArrayList();
        try (Stream<Path> pathStream = Files.walk(dir)) {
            pathStream.filter(Files::isDirectory).forEach(dirsToWatch::add);

            for (Path toWatch : dirsToWatch) {
                toWatch.register(
                        watchService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.ENTRY_MODIFY
                );
                log.info("Watching skill directory: {}", dir);
            }
        } catch (IOException e) {
            log.error("Failed to watch skill directory: {}", dir, e);
        }
    }

    private static URL resolveLocation(Path filePath) {
        try {
            return filePath.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new SkillWatchException("Invalid skill location " + filePath, e);
        }
    }

    private boolean checkIfWatched(URL location) {
        try {
            Path locationPath = Path.of(location.getPath()).toAbsolutePath();
            return watchedLocations.values().stream()
                    .anyMatch(watched -> {
                        try {
                            Path watchedPath = Path.of(watched.getPath()).toAbsolutePath();
                            return locationPath.startsWith(watchedPath);
                        } catch (Exception e) {
                            return false;
                        }
                    });
        } catch (Exception e) {
            return false;
        }
    }

    private void onFileChanged(Path filePath, WatchEvent.Kind<?> kind) {
        if (isTemporaryFile(filePath)) {
            return;
        }

        URL skillLocation = null;
        if (StandardWatchEventKinds.ENTRY_DELETE == kind) {
            if (SKILL_FILENAME.equalsIgnoreCase(filePath.getFileName().toString())) {
                filePath = filePath.getParent();
            }
            if (!Files.exists(filePath.resolve(SKILL_FILENAME))) {
                URL location = resolveLocation(filePath);
                if (checkIfWatched(location)) {
                    notifyListeners(listener -> listener.onSkillRemoved(location, null));
                }
            }
        } else if (Files.isDirectory(filePath) && Files.exists(filePath.resolve(SKILL_FILENAME))) {
            skillLocation = resolveLocation(filePath);
        } else if (Files.isRegularFile(filePath)) {
            Path skillDir = filePath.getParent();
            do {
                if (Files.exists(skillDir.resolve(SKILL_FILENAME))) {
                    skillLocation = resolveLocation(skillDir);
                    break;
                }
                skillDir = skillDir.getParent();
            } while (skillDir != null);
        }

        if (skillLocation != null && checkIfWatched(skillLocation)) {
            final URL location = skillLocation;
            ScheduledFuture<?> existNotifier = pendingNotifiers.get(location.toString());
            // cancel previous notifier
            if (existNotifier != null && !existNotifier.isDone()) {
                existNotifier.cancel(false);
            }

            // set a debounce delay to avoid repeated notifications
            ScheduledFuture<?> newNotifier = scheduler.schedule(() -> {
                try {
                    List<SkillLoadError> errors = Lists.newArrayList();
                    List<Skill> skills = skillLoader.load(location, errors);
                    if (StandardWatchEventKinds.ENTRY_CREATE == kind) {
                        skills.forEach(skill -> notifyListeners(listener -> listener.onSkillAdded(location, skill)));
                    } else {
                        skills.forEach(skill -> notifyListeners(listener -> listener.onSkillUpdated(location, skill)));
                    }
                    if (!errors.isEmpty()) {
                        notifyListeners(listener -> listener.onSkillLoadError(location, errors));
                    }
                } finally {
                    pendingNotifiers.remove(location.toString());
                }
            }, debounceMillis, TimeUnit.MILLISECONDS);
            pendingNotifiers.put(location.toString(), newNotifier);
        }
    }

    private boolean isTemporaryFile(Path filePath) {
        String fileName = filePath.getFileName().toString();
        return fileName.startsWith(".") ||
                fileName.endsWith("~") ||
                fileName.endsWith(".swp") ||
                fileName.endsWith(".tmp");
    }

    @Override
    public void watch(URL skillLocation) {
        if (!skillLoader.supports(skillLocation)) {
            throw new SkillWatchException("Unsupported skill location: " + skillLocation);
        }
        Path filePath = Path.of(skillLocation.getPath());
        if (!Files.exists(filePath)) {
            throw new SkillWatchException("Not found skill location: " + skillLocation);
        }

        // lazy start the watcher
        start();

        if (!Files.isDirectory(filePath)) {
            filePath = filePath.getParent();
        }
        try {
            URL watchLocation = filePath.toUri().toURL();
            if (watchedLocations.putIfAbsent(watchLocation.toString(), watchLocation) == null) {
                watchDirectory(filePath);
            }
        } catch (IOException e) {
            throw new SkillWatchException("Invalid skill location: " + skillLocation, e);
        }
    }

    @Override
    public void unwatch(URL skillLocation) {
        this.watchedLocations.remove(skillLocation.toString());
    }

    @Override
    public void addListener(SkillChangeListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeListener(SkillChangeListener listener) {
        this.listeners.remove(listener);
    }

    protected void notifyListeners(Consumer<SkillChangeListener> consumer) {
        for (SkillChangeListener listener : listeners) {
            try {
                consumer.accept(listener);
            } catch (Exception e) {
                log.error("Error notifying listener: {}", listener, e);
            }
        }
    }

    @Override
    public void close() throws IOException {
        scheduler.shutdown();

        if (running.getAndSet(false)) {
            pendingNotifiers.values().forEach(future -> future.cancel(false));
            pendingNotifiers.clear();

            if (watchThread != null) {
                watchThread.interrupt();
                try {
                    watchThread.join(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            if (watchService != null) {
                watchService.close();
            }
        }

        try {
            if (!scheduler.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        watchedLocations.clear();
        listeners.clear();
    }

}
