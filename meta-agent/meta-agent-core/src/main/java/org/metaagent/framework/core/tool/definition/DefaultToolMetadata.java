/*
 * MIT License
 *
 * Copyright (c) 2026 MetaAgent
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

package org.metaagent.framework.core.tool.definition;

import org.metaagent.framework.common.metadata.ClassMetadataProvider;

/**
 * Default implementation of {@link ToolMetadata}
 *
 * @author vyckey
 */
public class DefaultToolMetadata extends ClassMetadataProvider implements ToolMetadata {
    private boolean concurrencySafe;
    private boolean readOnly;
    private boolean returnDirectly;

    @Override
    public boolean isConcurrencySafe() {
        return concurrencySafe;
    }

    public void setConcurrencySafe(boolean concurrencySafe) {
        this.concurrencySafe = concurrencySafe;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public boolean isReturnDirectly() {
        return returnDirectly;
    }

    public void setReturnDirectly(boolean returnDirectly) {
        this.returnDirectly = returnDirectly;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public DefaultToolMetadata copy() {
        DefaultToolMetadata toolMetadata = new DefaultToolMetadata();
        toolMetadata.extendProperties.putAll(this.extendProperties);
        toolMetadata.concurrencySafe = this.concurrencySafe;
        toolMetadata.readOnly = this.readOnly;
        toolMetadata.returnDirectly = this.returnDirectly;
        return toolMetadata;
    }

}
