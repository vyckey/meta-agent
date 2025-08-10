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

package org.metaagent.framework.core.tool.toolkit;

import com.google.common.collect.Maps;
import org.metaagent.framework.core.tool.Tool;

import java.util.Map;
import java.util.ServiceLoader;

/**
 * Service Provider Interface (SPI) toolkit.
 *
 * @author vyckey
 */
public final class SpiToolkit extends ImmutableToolkit {
    public static final String NAME = "SPI_TOOLKIT";
    public static final SpiToolkit INSTANCE = new SpiToolkit();

    private SpiToolkit() {
        super(NAME, "Toolkit loaded from SPI", loadTools());
    }

    private static Map<String, Tool<?, ?>> loadTools() {
        Map<String, Tool<?, ?>> toolMap = Maps.newHashMap();
        for (Tool<?, ?> tool : ServiceLoader.load(Tool.class)) {
            toolMap.put(tool.getName(), tool);
        }
        return toolMap;
    }

    public static <I, O> Tool<I, O> findTool(String name) {
        return INSTANCE.getTool(name);
    }

}
