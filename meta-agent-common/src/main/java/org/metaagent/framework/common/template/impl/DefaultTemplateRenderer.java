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

package org.metaagent.framework.common.template.impl;

import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.TextStringBuilder;
import org.metaagent.framework.common.template.TemplateRenderException;
import org.metaagent.framework.common.template.TemplateRenderer;
import org.metaagent.framework.common.template.TemplateVariableExtractor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of StringFormatter that uses Apache Commons Text for variable substitution.
 * This formatter supports both key-value pairs and a single map as input arguments.
 * It extracts variables from the template and allows for substitution in the format `${variableName}`.
 * e.g.
 * <pre>
 * String template = "Hello, ${name}! You have ${count} new messages.";
 * StringFormatter formatter = new DefaultStringFormatter();
 * String result = formatter.format(template, "name", "Alice", "count", 5);
 * // "Hello, Alice! You have 5 new messages."
 * List<String> variables = formatter.extractVariables(template);
 * // ["name", "count"]
 * </pre>
 *
 * @author vyckey
 * @see TemplateRenderer
 */
public class DefaultTemplateRenderer implements TemplateRenderer, TemplateVariableExtractor {
    public static final String NAME = "default";
    public static final DefaultTemplateRenderer INSTANCE = new DefaultTemplateRenderer();

    @Override
    public String name() {
        return DefaultTemplateRenderer.NAME;
    }

    static Map<String, Object> toMapVariables(Object... args) {
        Map<String, Object> variables = new HashMap<>();
        if ((args.length & 0x1) == 0) {
            for (int i = 0; i < args.length; i += 2) {
                variables.put(args[i].toString(), args[i + 1]);
            }
        } else {
            throw new TemplateRenderException("Arguments must be key-value pairs.");
        }
        return variables;
    }

    @Override
    public String render(String template, Object... args) {
        Map<String, Object> variables = toMapVariables(args);
        return render(template, variables);
    }

    @Override
    public String render(String template, Map<String, Object> variables) {
        VariableSubstitutor substitutor = new VariableSubstitutor(variables);
        try {
            return substitutor.replace(template);
        } catch (Exception e) {
            throw new TemplateRenderException("Failed to render template: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> extractVariables(String template) {
        VariableSubstitutor substitutor = new VariableSubstitutor(Collections.emptyMap());
        substitutor.setEnableUndefinedVariableException(false);
        substitutor.replace(template);
        return substitutor.variables;
    }

    static class VariableSubstitutor extends StringSubstitutor {
        List<String> variables = new ArrayList<>();

        VariableSubstitutor(Map<String, Object> variables) {
            super(variables);
            setValueDelimiter(':');
            setEnableUndefinedVariableException(true);
            setEnableSubstitutionInVariables(true);
        }

        @Override
        protected String resolveVariable(String variableName, TextStringBuilder buf, int startPos, int endPos) {
            variables.add(variableName);
            return super.resolveVariable(variableName, buf, startPos, endPos);
        }
    }
}
