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

import org.metaagent.framework.common.metadata.MetadataProvider;
import org.metaagent.framework.core.model.ModelId;
import org.metaagent.framework.core.model.ModelInfo;
import org.metaagent.framework.core.model.ModelInstance;
import org.springframework.ai.model.ModelRequest;
import org.springframework.ai.model.ModelResponse;

import java.util.List;
import java.util.Objects;

/**
 * Contract for a provider that exposes AI/ML models to the MetaAgent runtime.
 *
 * <p>Implementations of this interface are responsible for enumerating available
 * models, providing access to a concrete model instance and exposing provider-level
 * metadata.</p>
 *
 * @author vyckey
 */
public interface ModelProvider extends AutoCloseable {

    /**
     * Create a new builder for a {@link DefaultModelProvider}.
     *
     * @return a new builder
     */
    static DefaultModelProvider.Builder builder() {
        return new DefaultModelProvider.Builder();
    }

    /**
     * Returns a stable identifier for this provider implementation.
     *
     * @return provider id (should be unique within the runtime)
     */
    ModelProviderId getId();

    /**
     * Human-readable name for this provider. Useful for logging and user-facing
     * diagnostics.
     *
     * @return provider display name
     */
    String getName();

    /**
     * Return provider-level metadata. This can include provider capabilities,
     * configuration hints or other descriptive information consumed by the runtime.
     *
     * @return the provider's {@link MetadataProvider}
     */
    MetadataProvider getMetadata();

    /**
     * List metadata about all models this provider exposes.
     *
     * @return list of {@link ModelInfo} describing available models; never null
     */
    List<ModelInfo> listModels();

    /**
     * List metadata about all models this provider exposes, filtered by the given
     * model info type.
     *
     * @param modelInfoType the model info type to filter by
     * @return list of {@link ModelInfo} describing available models; never null
     */
    <M extends ModelInfo> List<M> listModels(Class<M> modelInfoType);

    /**
     * Check if a model with the given identifier is available.
     *
     * @param modelId the provider-specific model id
     * @return true if the model is available, false otherwise
     */
    boolean hasModel(ModelId modelId);

    /**
     * Retrieve a model instance by its model identifier.
     *
     * <p>This returns a model instance with unknown request/response generics
     * (i.e. {@code ModelInstance<?, ?>}). Callers that need specific type
     * information should use the generic {@link #getModel(ModelId, Class)} overload.
     *
     * @param modelId the provider-specific model id
     * @return a {@link ModelInstance} for the given id or null if the model does not exist
     */
    ModelInstance<?, ?> getModel(ModelId modelId);

    /**
     * Retrieve a model instance by id and cast it to the expected typed
     * {@code ModelInstance} class.
     *
     * @param modelId    the provider-specific model id
     * @param modelClass the expected runtime class of the model instance
     * @param <T>        request type extending {@link ModelRequest}
     * @param <R>        response type extending {@link ModelResponse}
     * @return a typed {@link ModelInstance} if available, or null if not found or not assignable
     */
    default <T extends ModelRequest<?>, R extends ModelResponse<?>, M extends ModelInstance<T, R>>
    M getModel(ModelId modelId, Class<M> modelClass) {
        Objects.requireNonNull(modelId, "modelId must not be null");
        Objects.requireNonNull(modelClass, "modelClass must not be null");
        ModelInstance<?, ?> instance = getModel(modelId);
        if (instance == null) {
            return null;
        }
        if (!modelClass.isInstance(instance)) {
            throw new IllegalArgumentException("Model instance is not assignable to " + modelClass.getName());
        }
        return modelClass.cast(instance);
    }

    /**
     * Close this model provider.
     *
     * <p>This method is called when the model provider is no longer needed.</p>
     */
    @Override
    void close();
}
