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

package org.metaagent.framework.core.model.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OutputParserTest {
    @Test
    void codeBlockParseTest() {
        String text = "The following code shows how the sort algorithm works:\n"
                + "```python\ndef quick_sort(elements):\n\t...\n\tpass\n```";
        assertEquals(
                "```python\ndef quick_sort(elements):\n\t...\n\tpass\n```",
                OutputParsers.codeBlockParser("python", true).parse(text)
        );
        assertEquals(
                "\ndef quick_sort(elements):\n\t...\n\tpass\n",
                OutputParsers.codeBlockParser("python", false).parse(text)
        );
    }

    @Test
    void htmlTagParseTest() {
        String text = "The following HTML code shows how the sort algorithm works:\n"
                + "<html>\n<body>\n<h1>Sorting Algorithm</h1>\n<p>...</p>\n</body>\n</html>";
        assertEquals(
                "<html>\n<body>\n<h1>Sorting Algorithm</h1>\n<p>...</p>\n</body>\n</html>",
                OutputParsers.htmlTagParser("html", true).parse(text)
        );
        assertEquals(
                "\n<h1>Sorting Algorithm</h1>\n<p>...</p>\n",
                OutputParsers.htmlTagParser("body", false).parse(text)
        );
    }
}