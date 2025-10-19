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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.definition.ToolDefinition;
import org.metaagent.framework.core.tool.toolkit.Toolkit;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolReflectUtilsTest {

    private Method testMethod;
    private Method anotherTestMethod;
    private Method methodWithoutAnnotation;
    private Method contextMethod;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        testMethod = TestSampleClass.class.getMethod("testMethod", String.class, int.class);
        anotherTestMethod = TestSampleClass.class.getMethod("anotherTestMethod", double.class);
        methodWithoutAnnotation = TestSampleClass.class.getMethod("methodWithoutAnnotation", String.class);
        contextMethod = TestSampleClass.class.getMethod("contextMethod", ToolContext.class, String.class);
    }

    @Test
    void testResolveToolDefinitionWithAnnotation() {
        ToolDefinition toolDefinition = ToolReflectUtils.resolveToolDefinition(testMethod);

        assertEquals("testMethod", toolDefinition.name());
        assertEquals("A test method for testing purposes", toolDefinition.description());
        assertNotNull(toolDefinition.inputSchema());
        assertNotNull(toolDefinition.outputSchema());
        assertFalse(toolDefinition.isConcurrencySafe());
        assertFalse(toolDefinition.isReadOnly());
    }

    @Test
    void testResolveToolDefinitionWithoutCustomName() {
        ToolDefinition toolDefinition = ToolReflectUtils.resolveToolDefinition(anotherTestMethod);

        assertEquals("anotherTestMethod", toolDefinition.name()); // Method name used as default
        assertEquals("A test method without custom name", toolDefinition.description());
        assertNotNull(toolDefinition.inputSchema());
        assertNotNull(toolDefinition.outputSchema());
    }

    @Test
    void testResolveToolDefinitionWithoutAnnotation() {
        ToolDefinition toolDefinition = ToolReflectUtils.resolveToolDefinition(methodWithoutAnnotation);

        assertEquals("methodWithoutAnnotation", toolDefinition.name()); // Method name used as default
        assertEquals("", toolDefinition.description()); // Empty description when no annotation
        assertNotNull(toolDefinition.inputSchema());
        assertNotNull(toolDefinition.outputSchema());
    }

    @Test
    void testResolveToolDefinitionWithContextParameter() {
        ToolDefinition toolDefinition = ToolReflectUtils.resolveToolDefinition(contextMethod);

        assertEquals("contextMethod", toolDefinition.name());
        assertEquals("A test method with ToolContext parameter", toolDefinition.description());
        assertNotNull(toolDefinition.inputSchema());
        assertNotNull(toolDefinition.outputSchema());

        // The schema should not include the ToolContext parameter
        String inputSchema = toolDefinition.inputSchema();
        assertTrue(inputSchema.contains("\"message\"")); // Should contain the String parameter
        assertFalse(inputSchema.contains("ToolContext")); // Should not contain ToolContext parameter
    }

    @FunctionToolkit(value = "TestToolkit", description = "Test description")
    static class ValidToolkit {
        @FunctionTool(name = "validTool", description = "Valid tool description")
        public String validMethod() {
            return "test";
        }

        private String privateMethod() {
            return "private";
        }

        public static String staticMethod() {
            return "static";
        }
    }

    @Test
    void resolveToolkit_success() {
        ValidToolkit toolkitInstance = new ValidToolkit();
        Toolkit result = ToolReflectUtils.resolveToolkit(toolkitInstance);

        assertEquals("TestToolkit", result.getName());
        assertEquals("Test description", result.getDescription());

        assertEquals(1, result.getToolNames().size());

        String toolName = result.getToolNames().iterator().next();
        Tool<?, ?> tool = result.getTool(toolName);
        assertEquals("validTool", tool.getName());
        assertEquals("Valid tool description", tool.getDefinition().description());
    }

    @Test
    void resolveToolkit_missingAnnotation() {
        assertThrows(IllegalArgumentException.class, () ->
                ToolReflectUtils.resolveToolkit(new Object())
        );
    }

    @Test
    void resolveToolkit_emptyNameUsesClassName() {
        @FunctionToolkit(description = "No name")
        class AnonymousToolkit {
            @FunctionTool
            public void method() {
            }
        }

        Toolkit result = ToolReflectUtils.resolveToolkit(new AnonymousToolkit());
        assertEquals("AnonymousToolkit", result.getName());
    }

    @Test
    void resolveToolkit_ignoresNonAnnotatedMethods() {
        @FunctionToolkit
        class MixedToolkit {
            @FunctionTool
            public void annotated() {
            }

            public void notAnnotated() {
            }
        }

        Toolkit result = ToolReflectUtils.resolveToolkit(new MixedToolkit());
        assertEquals(1, result.getToolNames().size());
        assertEquals("annotated", result.getToolNames().iterator().next());
    }

    @Test
    void resolveToolkit_ignoresInvalidMethods() {
        @FunctionToolkit
        class InvalidMethodsToolkit {
            @FunctionTool
            private void privateMethod() {
            }

            @FunctionTool
            public static void staticMethod() {
            }
        }

        // Create a proxy to simulate synthetic method
        InvalidMethodsToolkit toolkit = new InvalidMethodsToolkit();
        ReflectionUtils.doWithMethods(InvalidMethodsToolkit.class, method -> {
            if (method.getName().equals("syntheticMethod")) {
                method.setAccessible(true);
            }
        });

        Toolkit result = ToolReflectUtils.resolveToolkit(toolkit);
        assertTrue(result.getToolNames().isEmpty());
    }

}