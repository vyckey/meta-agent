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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Parses a string output into a list of strings based on a specified delimiter.
 * Optionally trims whitespace from each string in the list.
 *
 * @author vyckey
 */
public class ListStringOutputParser implements OutputParser<String, List<String>> {
    public static final ListStringOutputParser INSTANCE = new ListStringOutputParser();
    private final String delimiter;
    private final boolean trim;

    public ListStringOutputParser(String delimiter, boolean trim) {
        this.delimiter = Objects.requireNonNull(delimiter, "Delimiter cannot be null");
        this.trim = trim;
    }

    public ListStringOutputParser() {
        this(",", true);
    }

    @Override
    public List<String> parse(String output) throws OutputParsingException {
        if (output == null) {
            return List.of();
        }
        try {
            return Arrays.stream(output.split(delimiter))
                    .map(s -> trim ? s.trim() : s)
                    .toList();
        } catch (Exception e) {
            throw new OutputParsingException("Failed to parse output as string list: " + e.getMessage(), e);
        }
    }
}
