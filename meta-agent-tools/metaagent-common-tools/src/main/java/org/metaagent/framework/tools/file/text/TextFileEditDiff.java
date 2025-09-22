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

package org.metaagent.framework.tools.file.text;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

/**
 * Text file edit diff result.
 *
 * @author vyckey
 */
public record TextFileEditDiff(
        Path originalFilePath,
        Path revisedFilePath,
        List<String> originalLines,
        List<String> revisedLines,
        Patch<String> diffPatch
) {
    public static TextFileEditDiff from(Path originalFilePath, Path revisedFilePath, String originalText, String revisedText) {
        List<String> originalLines = List.of(originalText.split("\n"));
        List<String> revisedLines = List.of(revisedText.split("\n"));
        Patch<String> diffPatch = DiffUtils.diff(originalLines, revisedLines);
        return new TextFileEditDiff(originalFilePath, revisedFilePath, originalLines, revisedLines, diffPatch);
    }

    public static TextFileEditDiff from(Path filePath, String originalText, String revisedText) {
        return from(filePath, filePath, originalText, revisedText);
    }

    public String originalText() {
        return String.join("\n", originalLines);
    }

    public String revisedText() {
        return String.join("\n", revisedLines);
    }

    public String diffText() {
        List<String> diffLines = UnifiedDiffUtils.generateUnifiedDiff(
                originalFilePath.toString(), revisedFilePath.toString(), originalLines(), diffPatch, 3);
        return String.join("\n", diffLines);
    }

    public DiffStats diffStats() {
        return DiffStats.from(diffPatch);
    }

    @NotNull
    @Override
    public String toString() {
        String stats;
        if (diffPatch.getDeltas().isEmpty()) {
            stats = "no changes.";
        } else {
            DiffStats diffStats = diffStats();
            stats = String.format("+%d -%d ~%d lines changed\n%s",
                    diffStats.addedLines(), diffStats.removedLines(), diffStats.changedLines(),
                    diffText()
            );
        }

        if (originalFilePath.equals(revisedFilePath)) {
            return String.format("Changes to file '%s': %s", originalFilePath, stats);
        } else {
            return String.format("Changes file '%s' to '%s': %s", originalFilePath, revisedFilePath, stats);
        }
    }

    public record DiffStats(
            int addedLines,
            int removedLines,
            int changedLines) {
        public static DiffStats from(Patch<String> patch) {
            int added = 0;
            int removed = 0;
            int changed = 0;
            for (var delta : patch.getDeltas()) {
                switch (delta.getType()) {
                    case INSERT -> added += delta.getTarget().getLines().size();
                    case DELETE -> removed += delta.getSource().getLines().size();
                    case CHANGE -> {
                        int originalSize = delta.getSource().getLines().size();
                        int revisedSize = delta.getTarget().getLines().size();
                        changed += Math.max(originalSize, revisedSize);
                    }
                }
            }
            return new DiffStats(added, removed, changed);
        }
    }
}
