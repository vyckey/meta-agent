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

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.InterpretException;
import org.metaagent.framework.common.template.TemplateRenderException;
import org.metaagent.framework.common.template.TemplateRenderer;

import java.util.Map;

/**
 * Jinja2TemplateRenderer is an implementation of TemplateRenderer that uses Jinjava for template rendering.
 * It provides methods to format strings using Jinja2 syntax and extract variables from templates.
 *
 * @author vyckey
 * @see TemplateRenderer
 */
public class Jinja2TemplateRenderer implements TemplateRenderer {
    public static final Jinja2TemplateRenderer INSTANCE = new Jinja2TemplateRenderer();
    public static final String NAME = "jinja2";
    private static final Jinjava JINJAVA = new Jinjava();

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String render(String template, Object... args) {
        Map<String, Object> variables = DefaultTemplateRenderer.toMapVariables(args);
        return render(template, variables);
    }

    @Override
    public String render(String template, Map<String, Object> args) {
        try {
            return JINJAVA.render(template, args);
        } catch (InterpretException e) {
            throw new TemplateRenderException(e.getMessage(), e);
        }
    }

}
