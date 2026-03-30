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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of {@link DynamicModelProvider}.
 *
 * <p>This implementation stores models in a thread-safe {@link ConcurrentHashMap}
 * keyed by the model id returned from {@link ModelInstance#getInfo()#getId()}.
 * </p>
 * <p>
 * Behavior summary:
 * <ul>
 *   <li>{@link #addModel(ModelInstance)} adds the model if an entry with the same id
 *       does not already exist.</li>
 *   <li>{@link #replaceModel(ModelInstance)} replaces an existing model with the same id.</li>
 *   <li>{@link #removeModel(String)} removes the model by id.</li>
 * </ul>
 */
public record DefaultDynamicModelProvider(
        ModelProviderId id,
        String name,
        MetadataProvider metadata,
        Map<ModelId, ModelInstance<?, ?>> models
) implements DynamicModelProvider {
    public DefaultDynamicModelProvider {
        Objects.requireNonNull(id, "model provider id must not be null");
        Objects.requireNonNull(name, "model provider name must not be null");
        metadata = metadata != null ? metadata : MetadataProvider.empty();
        Objects.requireNonNull(models, "models must not be null");
    }

    public DefaultDynamicModelProvider(ModelProviderId id, String name, MetadataProvider metadata) {
        this(id, name, metadata, new ConcurrentHashMap<>());
    }

    public DefaultDynamicModelProvider(ModelProviderId id, String name) {
        this(id, name, MetadataProvider.empty());
    }

    @Override
    public ModelProviderId getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public MetadataProvider getMetadata() {
        return this.metadata;
    }

    @Override
    public List<ModelInfo> listModels() {
        return models.values().stream().map(ModelInstance::getInfo).toList();
    }

    @Override
    public <M extends ModelInfo> List<M> listModels(Class<M> modelInfoType) {
        Objects.requireNonNull(modelInfoType, "modelInfoType must not be null");
        return models.values().stream().map(ModelInstance::getInfo)
                .filter(modelInfoType::isInstance).map(modelInfoType::cast).toList();
    }

    @Override
    public boolean hasModel(ModelId modelId) {
        return models.containsKey(modelId);
    }

    @Override
    public ModelInstance<?, ?> getModel(ModelId modelId) {
        Objects.requireNonNull(modelId, "modelId must not be null");
        return models.get(modelId);
    }

    @Override
    public boolean addModel(ModelInstance<?, ?> instance) {
        Objects.requireNonNull(instance, "instance must not be null");
        ModelInfo info = instance.getInfo();
        Objects.requireNonNull(info, "instance.getInfo() must not be null");
        ModelId modelId = Objects.requireNonNull(info.getId(), "model id must not be null");
        return models.putIfAbsent(modelId, instance) == null;
    }

    @Override
    public boolean replaceModel(ModelInstance<?, ?> newInstance) {
        Objects.requireNonNull(newInstance, "newInstance must not be null");
        ModelInfo info = newInstance.getInfo();
        ModelInstance<?, ?> oldInstance = models.get(info.getId());
        if (oldInstance == null) {
            return false;
        }
        return models.replace(info.getId(), oldInstance, newInstance);
    }

    @Override
    public boolean removeModel(String modelId) {
        Objects.requireNonNull(modelId, "modelId must not be null");
        return models.remove(modelId) != null;
    }

    @Override
    public void close() {
        models.clear();
    }
}

