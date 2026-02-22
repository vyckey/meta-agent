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

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple thread-safe {@link ModelProviderRegistry} backed by a
 * {@link ConcurrentHashMap}. This implementation provides basic register,
 * unregister and lookup semantics. Registering a provider with an id that
 * already exists will replace the previous provider.
 */
public class DefaultModelProviderRegistry implements ModelProviderRegistry {
    public static final DefaultModelProviderRegistry INSTANCE = new DefaultModelProviderRegistry();
    private final Map<ModelProviderId, ModelProvider> providers = new ConcurrentHashMap<>();

    /**
     * Register a provider. If a provider with the same id already exists it
     * will be replaced.
     *
     * @param provider the provider to register; must not be null
     */
    @Override
    public void registerProvider(ModelProvider provider) {
        Objects.requireNonNull(provider, "provider must not be null");
        ModelProviderId id = Objects.requireNonNull(provider.getId(), "provider.id must not be null");
        providers.put(id, provider);
    }

    /**
     * Unregister a provider.
     *
     * @param provider the provider to remove; must not be null
     */
    @Override
    public void unregisterProvider(ModelProvider provider) {
        Objects.requireNonNull(provider, "provider must not be null");
        ModelProviderId id = Objects.requireNonNull(provider.getId(), "provider.id must not be null");
        providers.remove(id);
    }

    /**
     * Return a snapshot of currently registered provider ids.
     *
     * @return an unmodifiable set of provider ids; never null
     */
    @Override
    public Set<ModelProviderId> getProviderIds() {
        return Set.copyOf(providers.keySet());
    }

    /**
     * Check whether a provider with the given id is registered.
     *
     * @param providerId the provider id to check; must not be null
     * @return true if the provider id is present, false otherwise
     */
    @Override
    public boolean hasProvider(ModelProviderId providerId) {
        Objects.requireNonNull(providerId, "providerId must not be null");
        return providers.containsKey(providerId);
    }

    /**
     * Lookup a provider by id.
     *
     * @param providerId the provider id to lookup; must not be null
     * @return the provider instance or null if not found
     */
    @Override
    public ModelProvider getProvider(ModelProviderId providerId) {
        Objects.requireNonNull(providerId, "providerId must not be null");
        return providers.get(providerId);
    }

    /**
     * Clear the registry. This is idempotent and thread-safe.
     */
    @Override
    public void close() {
        providers.clear();
    }
}

