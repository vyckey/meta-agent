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

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.function.Function;

/**
 * {@link OutputParser} factory.
 *
 * @author vyckey
 */
public class OutputParsers {
    private OutputParsers() {
    }

    public static <T> OutputParser<String, T> jsonParser(Class<T> classType) {
        return new JsonOutputParser<>(classType);
    }

    public static <T> OutputParser<String, T> jsonParser(TypeReference<T> typeRef) {
        return new JsonOutputParser<>(typeRef);
    }

    public static <T> RegexOutputParser<T> regexParser(String regex, Function<String, T> mapper) {
        return new RegexOutputParser<>(regex, mapper);
    }

    public static RegexGroupOutputParser regexGroupParser(String regex, String... groupNames) {
        return new RegexGroupOutputParser(regex, groupNames);
    }

    public static OutputParser<String, String> codeBlockParser(String extType, boolean backticksIncluded) {
        String regex = "```" + extType + "(?<content>((.|\\n)*?))```";
        String groupName = backticksIncluded ? null : "content";
        return new RegexOutputParser<>(regex, groupName, Function.identity());
    }

    public static OutputParser<String, String> htmlTagParser(String tagName, boolean tagIncluded) {
        String regex = "<" + tagName + ">(?<content>([\\s\\S]*?))</" + tagName + ">";
        String groupName = tagIncluded ? null : "content";
        return new RegexOutputParser<>(regex, groupName, Function.identity());
    }
}
