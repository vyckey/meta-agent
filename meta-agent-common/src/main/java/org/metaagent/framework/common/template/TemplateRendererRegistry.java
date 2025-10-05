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

import com.google.common.collect.Maps;
import org.metaagent.framework.common.template.impl.DefaultTemplateRenderer;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * {@link TemplateRenderer} manager
 *
 * @author vyckey
 * @see TemplateRenderer
 */
public final class TemplateRendererRegistry {
    private static volatile TemplateRendererRegistry instance = null;
    private static volatile String defaultRenderer = DefaultTemplateRenderer.NAME;

    private final Map<String, TemplateRenderer> rendererMap = Maps.newConcurrentMap();

    private TemplateRendererRegistry() {
    }

    public static TemplateRendererRegistry getInstance() {
        if (instance == null) {
            synchronized (TemplateRendererRegistry.class) {
                if (instance == null) {
                    TemplateRendererRegistry manager = new TemplateRendererRegistry();
                    ServiceLoader.load(TemplateRenderer.class).forEach(manager::register);
                    instance = manager;
                }
            }
        }
        return instance;
    }

    public static void setDefaultRenderer(String rendererName) {
        TemplateRendererRegistry.defaultRenderer = rendererName;
    }

    public static TemplateRenderer getDefaultRenderer() {
        return getInstance().getRenderer(defaultRenderer);
    }

    public Set<String> getRendererNames() {
        return rendererMap.keySet();
    }

    public void register(TemplateRenderer templateRenderer) {
        rendererMap.put(templateRenderer.name(), templateRenderer);
    }

    public TemplateRenderer getRenderer(String rendererName) {
        return rendererMap.get(rendererName);
    }

}
