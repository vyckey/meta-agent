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

package org.metaagent.framework.core.agent.action.result;

/**
 * description is here
 *
 * @author vyckey
 */
public class DefaultActionResult implements ActionResult {
    private final boolean success;
    private Exception exception;

    private DefaultActionResult(boolean success) {
        this.success = success;
    }

    public static DefaultActionResult success(boolean success) {
        return new DefaultActionResult(success);
    }

    public static DefaultActionResult exception(Exception e) {
        DefaultActionResult result = new DefaultActionResult(false);
        result.exception = e;
        return result;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    public Exception getException() {
        return exception;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ActionResult{");
        if (exception != null) {
            sb.append("exception=").append(exception);
        } else {
            sb.append("success=").append(success);
        }
        return sb.append('}').toString();
    }
}
