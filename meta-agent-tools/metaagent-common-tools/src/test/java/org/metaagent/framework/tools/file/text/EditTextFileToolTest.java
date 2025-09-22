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

import org.junit.jupiter.api.Test;
import org.metaagent.framework.core.tool.ToolContext;
import org.mockito.MockedStatic;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;

class EditTextFileToolTest {
    @Test
    void testRun() {
        Path mockPath = Path.of("test.txt");
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.exists(mockPath)).thenReturn(true);
            mockedFiles.when(() -> Files.readAllLines(mockPath)).thenReturn(List.of(
                    "This is a sample text file.",
                    "It contains multiple lines.",
                    "This line will be replaced."
            ));

            EditTextFileTool tool = new EditTextFileTool();
            EditTextFileInput toolInput = EditTextFileInput.builder()
                    .filePath(mockPath.toAbsolutePath().toString())
                    .oldString("This line will be replaced.")
                    .newString("This line has been replaced.\nAnd this is a new line.")
                    .build();
            EditTextFileOutput output = tool.run(ToolContext.create(), toolInput);
            TextFileEditDiff editDiff = output.editDiff();
            assertEquals("", editDiff.diffText());
        }
    }
}