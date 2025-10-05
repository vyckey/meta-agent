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

package org.metaagent.framework.core.tool.tools.reflect;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.converter.ToolConverters;
import org.metaagent.framework.core.tool.definition.ToolDefinition;
import org.metaagent.framework.core.tool.exception.ToolExecutionError;
import org.metaagent.framework.core.tool.exception.ToolExecutionException;
import org.springframework.ai.util.json.JsonParser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * MethodTool is an implementation of the Tool interface that wraps a Java Method.
 * It allows invoking the method with input parameters and converting the input and output
 * using the specified ToolConverter.
 *
 * @author vyckey
 */
@Slf4j
public class MethodTool implements Tool<Map<String, Object>, Object> {
    public static final ToolConverter<Map<String, Object>, Object> DEFAULT_TOOL_CONVERTER =
            ToolConverters.jsonConverter(new TypeReference<>() {
            });

    private final ToolDefinition toolDefinition;
    private final ToolConverter<Map<String, Object>, Object> toolConverter;
    private final Method toolMethod;
    private final Object toolObject;

    public MethodTool(ToolDefinition toolDefinition,
                      ToolConverter<Map<String, Object>, Object> toolConverter,
                      Method toolMethod,
                      Object toolObject) {
        this.toolDefinition = Objects.requireNonNull(toolDefinition, "ToolDefinition is required");
        this.toolConverter = Objects.requireNonNull(toolConverter, "ToolConverter is required");
        this.toolMethod = Objects.requireNonNull(toolMethod, "Tool method is required");
        this.toolObject = toolObject;
    }

    public MethodTool(Method toolMethod, Object toolObject) {
        this(
                ToolReflectUtils.resolveToolDefinition(toolMethod),
                DEFAULT_TOOL_CONVERTER,
                toolMethod,
                toolObject
        );
    }

    @Override
    public ToolDefinition getDefinition() {
        return toolDefinition;
    }

    @Override
    public ToolConverter<Map<String, Object>, Object> getConverter() {
        return toolConverter;
    }

    @Override
    public Object run(ToolContext context, Map<String, Object> input) throws ToolExecutionException {
        if (toolObject != null && !Modifier.isPublic(toolObject.getClass().getModifiers())
                || !Modifier.isPublic(toolMethod.getModifiers())) {
            toolMethod.setAccessible(true);
        }

        try {
            Object[] arguments = buildMethodArguments(context, input);
            return toolMethod.invoke(toolObject, arguments);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new ToolExecutionError("Failed to execute tool " + getName()
                    + " caused by invoking method " + toolMethod + " occurs errors", e);
        } catch (Exception e) {
            throw new ToolExecutionException("Failed to execute tool " + getName()
                    + " caused by invoking method " + toolMethod + " occurs exceptions", e);
        }
    }

    private Object[] buildMethodArguments(ToolContext toolContext, Map<String, Object> toolInputArguments) {
        return Stream.of(toolMethod.getParameters()).map(parameter -> {
            if (parameter.getType().isAssignableFrom(ToolContext.class)) {
                return toolContext;
            }
            Object rawArgument = toolInputArguments.get(parameter.getName());
            return buildTypedArgument(rawArgument, parameter.getParameterizedType());
        }).toArray();
    }

    private Object buildTypedArgument(Object value, Type type) {
        if (value == null) {
            return null;
        }

        if (type instanceof Class<?>) {
            return JsonParser.toTypedObject(value, (Class<?>) type);
        }

        // For generic types, use the fromJson method that accepts Type
        String json = JsonParser.toJson(value);
        return JsonParser.fromJson(json, type);
    }

    @Override
    public String toString() {
        return "MethodTool{name=\"" + getName() + "\", method=" + toolMethod + '}';
    }
}
