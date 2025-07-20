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

package org.metaagent.framework.tools.file.util;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

class GitIgnoreLikeFileFilterTest {
    @Test
    void ignoreFileTest() throws IOException {
        Path mockPath = Path.of(".gitignore");
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.readAllLines(mockPath)).thenReturn(List.of(
                    "# example patterns",
                    "!*.java",
                    "*.log",
                    "build/",
                    "/.github/",
                    "/config/",
                    "!/config/*.json",
                    "!README.md",
                    "test_?.py",
                    "data_[0-9].csv",
                    "$*.<*> \\*+ (?).docx"
            ));

            GitIgnoreLikeFileFilter filter = new GitIgnoreLikeFileFilter(mockPath);

            assertTrue(filter.ignoreFile(".git/HEAD"));
            assertTrue(filter.ignoreFile(".git/logs/HEAD"));

            assertFalse(filter.ignoreFile("HelloWorld.java"));
            assertFalse(filter.ignoreFile("module-example/src/main/java/com/example/app/App.java"));

            assertTrue(filter.ignoreFile("app.log"));
            assertTrue(filter.ignoreFile("logs/app.log"));

            assertTrue(filter.ignoreFile(".github/workflow.yml"));
            assertFalse(filter.ignoreFile("tests/.github/workflow.yml"));

            assertTrue(filter.ignoreFile("build/app-test.jar"));
            assertTrue(filter.ignoreFile("build/release/app.jar"));
            assertTrue(filter.ignoreFile("module-example/build/app-test.jar"));

            assertTrue(filter.ignoreFile("config/app.txt"));
            assertFalse(filter.ignoreFile("config/app.json"));
            assertTrue(filter.ignoreFile("config/app.icon"));

            assertFalse(filter.ignoreFile("README.md"));
            assertFalse(filter.ignoreFile("docs/README.md"));

            assertTrue(filter.ignoreFile("tests/test_0.py"));
            assertTrue(filter.ignoreFile("tests/test_a.py"));
            assertFalse(filter.ignoreFile("tests/test_ab.py"));

            assertTrue(filter.ignoreFile("data_0.csv"));
            assertTrue(filter.ignoreFile("data/data_8.csv"));
            assertFalse(filter.ignoreFile("data/data_a.csv"));

            assertTrue(filter.ignoreFile("$Animal.<cat> \\black+ (1).docx"));
        }
    }
}