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

package org.metaagent.framework.core.agent.ability;

import org.metaagent.framework.core.agent.AgentExecutionContext;

/**
 * description is here
 *
 * @author vyckey
 */
public abstract class AbstractAgentAbility implements AgentAbility {
    protected final String name;
    protected boolean activated;

    public AbstractAgentAbility(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isActivated() {
        return activated;
    }

    protected void checkActivated() {
        if (!activated) {
            throw new IllegalStateException("Ability not activated: " + name);
        }
    }

    @Override
    public void activate(AgentExecutionContext context) {
        this.activated = true;
    }

    @Override
    public void deactivate(AgentExecutionContext context) {
        this.activated = false;
    }

    @Override
    public String toString() {
        return name;
    }
}
