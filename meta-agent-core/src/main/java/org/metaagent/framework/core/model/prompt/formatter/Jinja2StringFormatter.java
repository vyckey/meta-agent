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

package org.metaagent.framework.core.model.prompt.formatter;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.InterpretException;
import org.metaagent.framework.core.model.prompt.PromptFormatException;

import java.util.List;
import java.util.Map;

/**
 * Jinjia2StringFormatter is an implementation of StringFormatter that uses Jinjava for template rendering.
 * It provides methods to format strings using Jinja2 syntax and extract variables from templates.
 *
 * @author vyckey
 */
public class Jinja2StringFormatter implements StringFormatter {
    public static final String NAME = "jinja2";
    private static final Jinjava JINJAVA = new Jinjava();

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String format(String template, Object... args) {
        Map<String, Object> variables = DefaultStringFormatter.toMapVariables(args);
        return format(template, variables);
    }

    @Override
    public String format(String template, Map<String, Object> args) {
        try {
            return JINJAVA.render(template, args);
        } catch (InterpretException e) {
            throw new PromptFormatException(e.getMessage(), e);
        }
    }


    @Override
    public List<String> extractVariables(String template) {
        throw new UnsupportedOperationException("Variable extraction is not supported in Jinja2StringFormatter");
    }
}
