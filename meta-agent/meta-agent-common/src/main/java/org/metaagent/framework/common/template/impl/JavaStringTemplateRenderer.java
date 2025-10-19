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

import org.metaagent.framework.common.template.TemplateRenderException;
import org.metaagent.framework.common.template.TemplateRenderer;

import java.util.Map;

/**
 * JavaStringTemplateRenderer is a TemplateRenderer implementation that uses Java's built-in String formatting.
 *
 * @author vyckey
 * @see TemplateRenderer
 */
public final class JavaStringTemplateRenderer implements TemplateRenderer {
    public static final JavaStringTemplateRenderer INSTANCE = new JavaStringTemplateRenderer();

    @Override
    public String name() {
        return "java";
    }

    @Override
    public String render(String template, Object... args) {
        return template.formatted(args);
    }

    @Override
    public String render(String template, Map<String, Object> variables) {
        throw new TemplateRenderException("Not supported Key-Value formatting in " + getClass().getSimpleName());
    }
}
