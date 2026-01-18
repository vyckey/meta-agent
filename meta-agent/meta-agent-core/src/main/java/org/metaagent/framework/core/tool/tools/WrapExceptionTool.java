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

package org.metaagent.framework.core.tool.tools;

import org.metaagent.framework.common.converter.Converter;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.converter.DefaultToolConverter;
import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.definition.ToolDefinition;
import org.metaagent.framework.core.tool.exception.ToolExecutionException;
import org.metaagent.framework.core.tool.exception.ToolArgumentException;

import java.util.Objects;

/**
 * WrapExceptionTool is a tool that wraps an exception into a ToolExecutionException.
 *
 * @author vyckey
 */
public class WrapExceptionTool<I, O> implements Tool<I, O> {
    private final Tool<I, O> targetTool;
    private final ToolConverter<I, O> toolConverter;

    public WrapExceptionTool(Tool<I, O> targetTool) {
        this.targetTool = Objects.requireNonNull(targetTool, "Target tool is required");
        this.toolConverter = new DefaultToolConverter<>(
                wrapConverter(targetTool.getConverter().inputConverter()),
                wrapConverter(targetTool.getConverter().outputConverter())
        );
    }

    @Override
    public String getName() {
        return targetTool.getName();
    }

    @Override
    public ToolDefinition getDefinition() {
        return targetTool.getDefinition();
    }

    private <S, T> Converter<S, T> wrapConverter(Converter<S, T> converter) {
        return source -> {
            try {
                return converter.convert(source);
            } catch (ToolArgumentException e) {
                throw e;
            } catch (Exception e) {
                throw new ToolArgumentException("Failed to convert tool input or output", e);
            }
        };
    }

    @Override
    public ToolConverter<I, O> getConverter() {
        return toolConverter;
    }

    @Override
    public O run(ToolContext context, I input) throws ToolExecutionException {
        try {
            return targetTool.run(context, input);
        } catch (ToolExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new ToolExecutionException("Call tool " + targetTool.getName() + " fail", e);
        }
    }

    @Override
    public String call(ToolContext context, String input) throws ToolExecutionException {
        return targetTool.call(context, input);
    }
}
