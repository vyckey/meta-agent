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

package org.metaagent.framework.core.model.provider;

import org.metaagent.framework.core.model.ModelId;
import org.metaagent.framework.core.model.ModelInstance;
import org.metaagent.framework.core.model.chat.ChatModelInstance;
import org.springframework.ai.model.ModelRequest;
import org.springframework.ai.model.ModelResponse;

import java.util.Objects;

/**
 * Utility class for working with model providers.
 *
 * @author vyckey
 */
public class ModelProviderUtils {
    private ModelProviderUtils() {
    }

    /**
     * Get a model instance by its model identifier.
     *
     * @param providerRegistry the model provider registry
     * @param modelId          the model identifier
     * @return a model instance
     * @throws NullPointerException if the model id is invalid or not found
     */
    public static ModelInstance<?, ?> getModel(ModelProviderRegistry providerRegistry, ModelId modelId) {
        ModelProvider modelProvider = providerRegistry.getProvider(modelId.providerId());
        Objects.requireNonNull(modelProvider, "No model provider found for model id: " + modelId);
        return Objects.requireNonNull(modelProvider.getModel(modelId), "No model found for model id: " + modelId);
    }

    /**
     * Get a model instance by its model identifier and model class.
     *
     * @param providerRegistry the model provider registry
     * @param modelId          the model identifier
     * @param modelClass       the model class
     * @return a model instance
     * @throws NullPointerException if the model id is invalid or not found
     */
    public static <T extends ModelRequest<?>, R extends ModelResponse<?>, M extends ModelInstance<T, R>>
    M getModel(ModelProviderRegistry providerRegistry, ModelId modelId, Class<M> modelClass) {
        ModelProvider modelProvider = providerRegistry.getProvider(modelId.providerId());
        Objects.requireNonNull(modelProvider, "No model provider found for model id: " + modelId);
        return Objects.requireNonNull(modelProvider.getModel(modelId, modelClass), "No model found for model id: " + modelId);
    }

    /**
     * Get a chat model instance by its model identifier.
     *
     * @param providerRegistry the model provider registry
     * @param modelId          the model identifier
     * @return a chat model instance
     * @throws NullPointerException if the model id is invalid or not found
     */
    public static ChatModelInstance getChatModel(ModelProviderRegistry providerRegistry, ModelId modelId) {
        return getModel(providerRegistry, modelId, ChatModelInstance.class);
    }
}
