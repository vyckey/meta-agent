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

package org.metaagent.framework.common.io;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownUtils {
    private static final YAMLMapper YAML_MAPPER = new YAMLMapper();
    private static final Pattern FRONT_MATTER_PATTERN = Pattern.compile("^---\\n(.*?)\\n^---\\n", Pattern.MULTILINE);

    private MarkdownUtils() {
    }

    public static <T> T parseFrontMatter(String frontMatterText, Class<T> frontMatterClass) throws IOException {
        try {
            return YAML_MAPPER.readValue(frontMatterText, frontMatterClass);
        } catch (JsonProcessingException e) {
            throw new IOException("Failed to parse markdown front-matter", e);
        }
    }

    public static <T> T parseFrontMatter(String frontMatterText, TypeReference<T> typeReference) throws IOException {
        try {
            return YAML_MAPPER.readValue(frontMatterText, typeReference);
        } catch (JsonProcessingException e) {
            throw new IOException("Failed to parse markdown front-matter", e);
        }
    }

    public static String readFrontMatter(Path path) throws IOException {
        if (!path.toFile().exists()) {
            throw new FileNotFoundException(path.toString());
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            LineIterator lineIterator = IOUtils.lineIterator(reader);

            List<String> lines = new ArrayList<>();
            int foundDashCount = 0;
            while (lineIterator.hasNext()) {
                String line = lineIterator.next();
                if (line.trim().equals("---")) {
                    foundDashCount++;
                } else if (foundDashCount == 0 && !line.trim().isEmpty()) {
                    break;
                }

                if (foundDashCount >= 2) {
                    break;
                } else if (foundDashCount == 1) {
                    lines.add(line);
                }
            }
            return String.join(System.lineSeparator(), lines);
        }
    }

    public static <T> T readFrontMatter(Path path, Class<T> metadataClass) throws IOException {
        String frontMatter = readFrontMatter(path);
        if (StringUtils.isEmpty(frontMatter)) {
            return null;
        }
        return parseFrontMatter(frontMatter, metadataClass);
    }

    public static <T> T readFrontMatter(Path path, TypeReference<T> typeReference) throws IOException {
        String frontMatter = readFrontMatter(path);
        if (StringUtils.isEmpty(frontMatter)) {
            return null;
        }
        return parseFrontMatter(frontMatter, typeReference);
    }

    public static Pair<String, String> parseFrontMatterAndMainText(String markdownText) {
        Matcher matcher = FRONT_MATTER_PATTERN.matcher(markdownText);
        if (matcher.find()) {
            String frontMatter = matcher.group(1).trim();
            String mainText = markdownText.substring(matcher.end());
            return Pair.of(frontMatter, mainText);
        }
        return Pair.of("", markdownText);
    }

}
