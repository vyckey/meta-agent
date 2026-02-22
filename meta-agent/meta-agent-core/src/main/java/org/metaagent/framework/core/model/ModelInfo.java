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

package org.metaagent.framework.core.model;

/**
 * Basic metadata describing a model exposed by a {@code ModelProvider}.
 *
 * <p>Implementations provide identifying information used by the runtime to
 * locate and describe available models.</p>
 */
public interface ModelInfo {
    /**
     * Provider-specific model identifier. This id should be unique within the
     * scope of the provider that exposes the model.
     *
     * @return the model id string
     */
    ModelId getId();

    /**
     * Human-readable name for the model.
     *
     * @return the model's display name
     */
    String getName();

    /**
     * Logical family or category of the model (for example: "chat", "embedding").
     * This is provider-defined and may be used for grouping or filtering models.
     *
     * @return the model family identifier
     */
    String getFamily();
}
