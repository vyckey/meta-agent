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
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.definition.ToolDefinition;
import org.metaagent.framework.core.tool.exception.ToolExecutionException;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MethodToolTest {

    private TestSampleClass testSampleObject;
    private Method testMethod;
    private Method contextMethod;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        testSampleObject = new TestSampleClass();
        testMethod = TestSampleClass.class.getMethod("testMethod", String.class, int.class);
        contextMethod = TestSampleClass.class.getMethod("contextMethod", ToolContext.class, String.class);
    }

    @Test
    void testConstructorWithAllParameters() {
        ToolDefinition toolDefinition = ToolReflectUtils.resolveToolDefinition(testMethod);
        ToolConverter<Map<String, Object>, Object> converter = MethodTool.DEFAULT_TOOL_CONVERTER;
        Method method = testMethod;
        Object object = testSampleObject;

        MethodTool methodTool = new MethodTool(toolDefinition, converter, method, object);

        assertEquals(toolDefinition, methodTool.getDefinition());
        assertEquals(converter, methodTool.getConverter());
    }

    @Test
    void testConstructorWithMethodAndObject() {
        Method method = testMethod;
        Object object = testSampleObject;

        MethodTool methodTool = new MethodTool(method, object);

        assertNotNull(methodTool.getDefinition());
        assertEquals(MethodTool.DEFAULT_TOOL_CONVERTER, methodTool.getConverter());
    }

    @Test
    void testGetDefinition() {
        MethodTool methodTool = new MethodTool(testMethod, testSampleObject);
        ToolDefinition definition = methodTool.getDefinition();

        assertEquals("testMethod", definition.name());
        assertEquals("A test method for testing purposes", definition.description());
        assertNotNull(definition.inputSchema());
        assertNotNull(definition.inputSchema());
    }

    @Test
    void testRunSuccessfully() throws ToolExecutionException {
        MethodTool methodTool = new MethodTool(testMethod, testSampleObject);
        ToolContext context = mock(ToolContext.class);
        Map<String, Object> input = new HashMap<>();
        input.put("param1", "testValue");
        input.put("param2", 42);

        Object result = methodTool.run(context, input);

        assertEquals("param1: testValue, param2: 42", result);
    }

    @Test
    void testRunWithContextParameter() throws ToolExecutionException {
        MethodTool methodTool = new MethodTool(contextMethod, testSampleObject);
        ToolContext context = mock(ToolContext.class);
        when(context.toString()).thenReturn("MockContext");
        Map<String, Object> input = new HashMap<>();
        input.put("message", "Hello");

        Object result = methodTool.run(context, input);

        assertEquals("context: MockContext, message: Hello", result);
    }

    @Test
    void testRunWithMissingParameter() throws ToolExecutionException {
        MethodTool methodTool = new MethodTool(testMethod, testSampleObject);
        ToolContext context = mock(ToolContext.class);
        Map<String, Object> input = new HashMap<>();
        input.put("param1", "testValue");
        // Missing param2

        // Should handle null parameter gracefully
        assertThrows(ToolExecutionException.class, () -> methodTool.run(context, input));
    }

    @Test
    void testToString() throws NoSuchMethodException {
        MethodTool methodTool = new MethodTool(testMethod, testSampleObject);
        String toStringResult = methodTool.toString();

        assertTrue(toStringResult.contains("testMethod"));
        assertTrue(toStringResult.contains("MethodTool"));
    }

    @Test
    void testConstructorWithNullToolDefinition() throws NoSuchMethodException {
        ToolConverter<Map<String, Object>, Object> converter = MethodTool.DEFAULT_TOOL_CONVERTER;
        Method method = testMethod;
        Object object = testSampleObject;

        assertThrows(NullPointerException.class, () ->
                new MethodTool(null, converter, method, object));
    }

    @Test
    void testConstructorWithNullToolConverter() throws NoSuchMethodException {
        ToolDefinition toolDefinition = ToolReflectUtils.resolveToolDefinition(testMethod);
        Method method = testMethod;
        Object object = testSampleObject;

        assertThrows(NullPointerException.class, () ->
                new MethodTool(toolDefinition, null, method, object));
    }

    @Test
    void testConstructorWithNullMethod() {
        ToolDefinition toolDefinition = ToolReflectUtils.resolveToolDefinition(testMethod);
        ToolConverter<Map<String, Object>, Object> converter = MethodTool.DEFAULT_TOOL_CONVERTER;
        Object object = testSampleObject;

        assertThrows(NullPointerException.class, () ->
                new MethodTool(toolDefinition, converter, null, object));
    }
}