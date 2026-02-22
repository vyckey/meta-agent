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

import org.metaagent.framework.core.model.ModelInstance;

/**
 * Provider interface that supports adding and removing models at runtime.
 *
 * <p>This interface extends {@link ModelProvider} and adds operations to
 * register, replace and unregister models dynamically.</p>
 *
 * <p>Implementations should document and ensure thread-safety when required, and
 * describe behavior when adding a duplicate id or removing a non-existent id.
 * Recommended semantics:
 * <ul>
 *   <li>{@code addModel} returns {@code true} when a model is added successfully; if a model with the same id already exists it should
 *       return {@code false} or throw an exception.</li>
 *   <li>{@code replaceModel} returns {@code true} if a model was replaced; {@code false} if no model with the given id existed.</li>
 *   <li>{@code removeModel} returns {@code true} when removal succeeds, otherwise {@code false}.</li>
 * </ul>
 * </p>
 *
 * @author vyckey
 */
public interface DynamicModelProvider extends ModelProvider {

    /**
     * Create a new dynamic model provider with the given id and name.
     *
     * @param providerId   the unique id of the provider; must not be null
     * @param providerName the name of the provider; must not be null
     * @return a new dynamic model provider
     * @throws NullPointerException if {@code providerId} or {@code providerName} is null
     */
    static DynamicModelProvider create(ModelProviderId providerId, String providerName) {
        return new DefaultDynamicModelProvider(providerId, providerName);
    }

    /**
     * Register the given model instance with this provider.
     *
     * <p>The provider may extract the model id from the instance's metadata
     * or the instance may internally carry the id. Implementations should
     * document how the model id is determined.</p>
     *
     * @param instance the corresponding {@link ModelInstance}; must not be null
     * @return {@code true} if the model was added successfully; {@code false} if a model with the same id already exists
     * (implementations may choose to throw an exception instead)
     * @throws NullPointerException if {@code instance} is null
     */
    boolean addModel(ModelInstance<?, ?> instance);

    /**
     * Replace the model with the given id with a new instance.
     *
     * @param newInstance the new {@link ModelInstance} to associate with the id; must not be null
     * @return {@code true} if a model was replaced; {@code false} if no model with the given id existed
     * @throws NullPointerException if {@code modelId} or {@code newInstance} is null
     */
    boolean replaceModel(ModelInstance<?, ?> newInstance);

    /**
     * Remove the model with the specified id from this provider.
     *
     * @param modelId the id of the model to remove; must not be null
     * @return {@code true} if the model was removed; {@code false} if no model with the given id existed
     * @throws NullPointerException if {@code modelId} is null
     */
    boolean removeModel(String modelId);
}
