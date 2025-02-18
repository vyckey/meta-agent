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

package org.metaagent.framework.core.agent.action.actions;

import org.metaagent.framework.core.agent.action.Action;
import org.metaagent.framework.core.agent.action.ActionExecuteContext;
import org.metaagent.framework.core.agent.action.ActionExecutionException;
import org.metaagent.framework.core.agent.action.result.ActionResult;

/**
 * description is here
 *
 * @author vyckey
 */
public abstract class AbstractAction implements Action {
    protected final String name;
    protected String description;
    protected int priority;

    protected AbstractAction(String name) {
        this.name = name;
        this.priority = PRIORITY_DEFAULT;
    }

    protected AbstractAction(String name, String description, int priority) {
        this.name = name;
        this.description = description;
        this.priority = priority;
    }

    protected AbstractAction(String name, String description) {
        this(name, description, PRIORITY_DEFAULT);
    }

    @Override
    public ActionResult execute(ActionExecuteContext context) throws ActionExecutionException {
        try {
            return doExecute(context);
        } catch (ActionExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new ActionExecutionException(e);
        }
    }

    protected abstract ActionResult doExecute(ActionExecuteContext context);

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "<" + name + "> " + (description == null ? "" : description);
    }
}
