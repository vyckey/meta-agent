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

package org.metaagent.framework.core.model;

import org.springframework.ai.model.Model;
import org.springframework.ai.model.StreamingModel;

/**
 * Model provider
 *
 * @author vyckey
 */
public interface ModelProvider {
    /**
     * Gets model provider name
     *
     * @return the model provider name
     */
    String getName();

    /**
     * Gets model name
     *
     * @return the model name
     */
    String getModelName();

    /**
     * Gets model metadata
     *
     * @return the model metadata
     */
    ModelMetadata getModelMetadata();

    /**
     * Gets the model instance
     *
     * @return the model instance if present
     */
    Model<?, ?> getModel();

    /**
     * Gets the streaming model instance
     *
     * @return the streaming model instance if present
     */
    StreamingModel<?, ?> getStreamingModel();
}
