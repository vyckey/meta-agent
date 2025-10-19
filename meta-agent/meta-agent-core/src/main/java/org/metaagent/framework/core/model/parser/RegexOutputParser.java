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

import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.common.converter.Converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RegexOutputParser is a parser that uses a regular expression to parse the output of an agent.
 *
 * @param <T> The type of the parsed output.
 * @author vyckey
 */
public final class RegexOutputParser<T> implements StringOutputParser<T> {
    private final Pattern pattern;
    private final Converter<String, T> converter;
    private final String groupName;
    private final boolean optional;

    public Pattern getPattern() {
        return pattern;
    }

    public String getGroupName() {
        return groupName;
    }

    public RegexOutputParser(Pattern pattern, String groupName, Converter<String, T> converter, boolean optional) {
        this.pattern = Objects.requireNonNull(pattern, "pattern is required");
        this.converter = Objects.requireNonNull(converter, "converter is required");
        this.groupName = groupName;
        this.optional = optional;
    }

    public RegexOutputParser(Pattern pattern, String groupName, Converter<String, T> converter) {
        this(pattern, groupName, converter, false);
    }

    public RegexOutputParser(Pattern pattern, Converter<String, T> converter, boolean optional) {
        this(pattern, null, converter, optional);
    }

    public RegexOutputParser(Pattern pattern, Converter<String, T> converter) {
        this(pattern, null, converter, false);
    }

    @Override
    public T parse(String output) throws OutputParsingException {
        Matcher matcher = pattern.matcher(output);
        if (!matcher.find()) {
            if (optional) {
                return null;
            }
            throw new OutputParsingException("No match found for the given regex pattern: " + pattern);
        }
        String value = StringUtils.isEmpty(groupName) ? matcher.group() : matcher.group(groupName);
        return converter.convert(value);
    }

    public List<T> parseAll(String output) throws OutputParsingException {
        Matcher matcher = pattern.matcher(output);
        List<T> results = new ArrayList<>();
        while (matcher.find()) {
            String value = StringUtils.isEmpty(groupName) ? matcher.group() : matcher.group(groupName);
            results.add(converter.convert(value));
        }
        if (results.isEmpty() && !optional) {
            throw new OutputParsingException("No match found for the given regex pattern: " + pattern);
        }
        return results;
    }
}
