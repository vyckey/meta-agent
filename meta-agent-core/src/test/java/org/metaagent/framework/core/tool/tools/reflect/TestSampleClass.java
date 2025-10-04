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

import org.metaagent.framework.core.tool.ToolContext;

public class TestSampleClass {

    @FunctionTool(name = "testMethod", description = "A test method for testing purposes")
    public String testMethod(String param1, int param2) {
        return "param1: " + param1 + ", param2: " + param2;
    }

    @FunctionTool(description = "A test method without custom name")
    public int anotherTestMethod(double value) {
        return (int) Math.round(value);
    }

    public String methodWithoutAnnotation(String input) {
        return "input: " + input;
    }

    @FunctionTool(name = "contextMethod", description = "A test method with ToolContext parameter")
    public String contextMethod(ToolContext context, String message) {
        return "context: " + context + ", message: " + message;
    }
}