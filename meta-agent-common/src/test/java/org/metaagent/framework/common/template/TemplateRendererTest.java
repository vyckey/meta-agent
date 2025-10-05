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

package org.metaagent.framework.common.template;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TemplateRendererTest {
    @Test
    void testJavaFormatter() {
        TemplateRenderer renderer = TemplateRendererRegistry.getInstance().getRenderer("java");
        assertEquals("Hello, World!", renderer.render("Hello, %s!", "World"));
    }

    @Test
    void testSlf4jFormatter() {
        TemplateRenderer renderer = TemplateRendererRegistry.getInstance().getRenderer("slf4j");
        assertEquals("Hello, World!", renderer.render("Hello, {}!", "World"));
    }

    @Test
    void testDefaultFormatter() {
        TemplateRenderer defaultRenderer = TemplateRendererRegistry.getDefaultRenderer();
        assertEquals("Hello, World!", defaultRenderer.render("Hello, ${name}!", "name", "World"));
        assertEquals(List.of("name"), ((TemplateVariableExtractor) defaultRenderer).extractVariables("Hello, ${name}!"));
    }

    @Test
    void testJinja2Formatter() {
        TemplateRenderer renderer = TemplateRendererRegistry.getInstance().getRenderer("jinja2");
        assertEquals("Hello, World!", renderer.render("Hello, {{name}}!", "name", "World"));
    }
}