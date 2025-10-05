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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.converter.ToolConvertException;
import org.metaagent.framework.core.tool.definition.DefaultToolDefinition;
import org.metaagent.framework.core.tool.definition.ToolDefinition;
import org.metaagent.framework.core.tool.toolkit.DefaultToolkit;
import org.metaagent.framework.core.tool.toolkit.Toolkit;
import org.metaagent.framework.common.json.JsonSchemaGenerator;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ToolReflectUtils {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static ToolDefinition resolveToolDefinition(Method toolMethod) {
        FunctionTool toolAnnotation = toolMethod.getAnnotation(FunctionTool.class);
        String toolName = toolMethod.getName();
        String toolDescription = "";
        String inputSchema = "";
        String outputSchema = "";
        if (toolAnnotation != null) {
            if (!toolAnnotation.name().isEmpty()) {
                toolName = toolAnnotation.name();
            }
            toolDescription = toolAnnotation.description();
            inputSchema = toolAnnotation.inputSchema();
            outputSchema = toolAnnotation.outputSchema();
        }

        if (StringUtils.isEmpty(inputSchema)) {
            inputSchema = generateInputSchema(toolMethod);
        }
        if (StringUtils.isEmpty(outputSchema)) {
            outputSchema = JsonSchemaGenerator.generateForType(toolMethod.getGenericReturnType());
        }

        DefaultToolDefinition.Builder definitionBuilder = ToolDefinition.builder(toolName)
                .description(toolDescription)
                .inputSchema(inputSchema)
                .outputSchema(outputSchema);
        if (toolAnnotation != null) {
            definitionBuilder.isConcurrencySafe(toolAnnotation.concurrencySafe())
                    .isReadOnly(toolAnnotation.readOnly());
        }
        return definitionBuilder.build();
    }

    private static String generateInputSchema(Method toolMethod) {
        String schema = JsonSchemaGenerator.generateForMethod(toolMethod);
        Optional<String> contextParamName = Arrays.stream(toolMethod.getParameters())
                .filter(parameter -> parameter.getType().isAssignableFrom(ToolContext.class))
                .map(Parameter::getName)
                .findFirst();
        if (contextParamName.isPresent()) {
            try {
                JsonNode rootNode = OBJECT_MAPPER.readTree(schema);
                ((ObjectNode) rootNode.at("/properties")).remove(contextParamName.get());
                schema = OBJECT_MAPPER.writeValueAsString(rootNode);
            } catch (JsonProcessingException e) {
                throw new ToolConvertException("Failed to generate input schema for method " + toolMethod.getName(), e);
            }
        }
        return schema;
    }

    public static Toolkit resolveToolkit(Object targetObject) {
        Class<?> objectClass = targetObject.getClass();
        FunctionToolkit functionToolkit = AnnotationUtils.findAnnotation(objectClass, FunctionToolkit.class);
        if (functionToolkit == null) {
            throw new IllegalArgumentException("Not found FunctionToolkit annotation for class " + objectClass.getName());
        }

        List<Tool<?, ?>> tools = Lists.newArrayList();
        ReflectionUtils.doWithMethods(objectClass, method -> {
            boolean hasToolAnnotation = method.isAnnotationPresent(FunctionTool.class);
            boolean isValidMethod = Modifier.isPublic(method.getModifiers())
                    && !Modifier.isStatic(method.getModifiers())
                    && !method.isSynthetic();
            if (hasToolAnnotation && isValidMethod) {
                tools.add(new MethodTool(method, targetObject));
            }
        });
        String name = functionToolkit.value().isEmpty() ? objectClass.getSimpleName() : functionToolkit.value();
        return new DefaultToolkit(name, functionToolkit.description(), tools);
    }

}
