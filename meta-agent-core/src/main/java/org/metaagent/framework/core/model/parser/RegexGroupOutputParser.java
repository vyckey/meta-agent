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

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RegexGroupOutputParser is a parser that uses regular expressions to parse the output of a model.
 *
 * @author vyckey
 */
public final class RegexGroupOutputParser implements OutputParser<String, RegexGroupOutputParser.GroupResult> {
    private final Pattern pattern;
    private final String[] groupNames;

    public RegexGroupOutputParser(Pattern pattern, String... groupNames) {
        this.pattern = Objects.requireNonNull(pattern, "pattern is required");
        this.groupNames = Objects.requireNonNull(groupNames, "groupNames is required");
    }

    public RegexGroupOutputParser(String regex, String... groupNames) {
        this(Pattern.compile(regex), groupNames);
    }

    @Override
    public GroupResult parse(String output) throws OutputParsingException {
        Matcher matcher = pattern.matcher(output);
        if (!matcher.find()) {
            throw new OutputParsingException("No match found for the given regex pattern: " + pattern);
        }
        Map<String, String> groups = Maps.newHashMap();
        if (groupNames.length == 0) {
            for (int i = 0; i < matcher.groupCount(); i++) {
                groups.put(String.valueOf(i), matcher.group(i));
            }
        } else {
            for (String groupName : groupNames) {
                groups.put(groupName, matcher.group(groupName));
            }
        }
        return new GroupResult(matcher.group(), groups);
    }

    public record GroupResult(String value, Map<String, String> groups) {
    }
}
