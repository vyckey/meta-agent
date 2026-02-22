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

import com.google.common.collect.Maps;
import org.metaagent.framework.common.metadata.MetadataProvider;
import org.metaagent.framework.core.model.ModelId;
import org.metaagent.framework.core.model.ModelInfo;
import org.metaagent.framework.core.model.ModelInstance;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Default implementation of {@link ModelProvider}.
 *
 * @author vyckey
 */
public record DefaultModelProvider(
        ModelProviderId id,
        String name,
        MetadataProvider metadata,
        Map<ModelId, ModelInstance<?, ?>> models
) implements ModelProvider {
    public DefaultModelProvider {
        Objects.requireNonNull(id, "model provider id must not be null");
        Objects.requireNonNull(name, "model provider name must not be null");
        Objects.requireNonNull(models, "models must not be null");
        List<ModelId> invalidModelIds = models.values().stream()
                .map(ModelInstance::getInfo).map(ModelInfo::getId)
                .filter(modelId -> !modelId.providerId().equals(id))
                .toList();
        if (!invalidModelIds.isEmpty()) {
            throw new IllegalArgumentException("model ids " + invalidModelIds + " do not match provider id: " + id);
        }

        metadata = metadata != null ? metadata : MetadataProvider.empty();
        models = Map.copyOf(models);
    }

    public DefaultModelProvider(ModelProviderId id, String name, Map<ModelId, ModelInstance<?, ?>> models) {
        this(id, name, MetadataProvider.empty(), models);
    }

    public static Builder builder() {
        return new Builder();
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
    public void close() {
        // No resources to close in this implementation
    }


    public static class Builder {
        private ModelProviderId id;
        private String name;
        private MetadataProvider metadata;
        private Map<ModelId, ModelInstance<?, ?>> models;

        public Builder() {
            this.metadata = MetadataProvider.empty();
            this.models = Map.of();
        }

        public Builder id(String id) {
            this.id = ModelProviderId.of(id);
            return this;
        }

        public Builder id(ModelProviderId id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder metadata(MetadataProvider metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder models(Map<ModelId, ModelInstance<?, ?>> models) {
            this.models = models;
            return this;
        }

        public Builder addModel(ModelInstance<?, ?> model) {
            Objects.requireNonNull(model, "model must not be null");
            if (models == null) {
                models = Maps.newHashMap();
            }
            models.put(model.getInfo().getId(), model);
            return this;
        }

        public DefaultModelProvider build() {
            return new DefaultModelProvider(id, name, metadata, models);
        }
    }
}
