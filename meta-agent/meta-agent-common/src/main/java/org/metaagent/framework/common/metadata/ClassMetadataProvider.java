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

package org.metaagent.framework.common.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Abstract class metadata provider.
 *
 * @author vyckey
 */
public abstract class ClassMetadataProvider implements MetadataProvider {
    @JsonIgnore
    protected Map<String, PropertyAccessor> propertyAccessors;
    protected final Map<String, Object> extendProperties;

    public ClassMetadataProvider(Map<String, Object> extendProperties) {
        this.propertyAccessors = buildPropertyAccessors(getClass(), Set.of("propertyAccessors", "extendProperties", "properties", "class"));
        this.extendProperties = Objects.requireNonNull(extendProperties, "extendProperties is required");
    }

    public ClassMetadataProvider() {
        this(Maps.newHashMap());
    }

    static Map<String, PropertyAccessor> buildPropertyAccessors(Class<?> clazz, Set<String> excludedPropertyNames) {
        Map<String, PropertyAccessor> propertyAccessors = buildPropertyAccessorsByMethods(clazz, excludedPropertyNames);
        Map<String, PropertyAccessor> propertyAccessors2 = buildPropertyAccessorsByFields(clazz, excludedPropertyNames);
        for (PropertyAccessor fieldAccessor : propertyAccessors2.values()) {
            String propertyName = fieldAccessor.propertyName;
            PropertyAccessor methodAccessor = propertyAccessors.get(propertyName);

            // method accessor has the higher priority than field accessor
            Function<Object, Object> propertyGetter = Optional.ofNullable(methodAccessor).map(PropertyAccessor::propertyGetter)
                    .orElse(fieldAccessor.propertyGetter);
            BiConsumer<Object, Object> propertySetter = Optional.ofNullable(methodAccessor).map(PropertyAccessor::propertySetter)
                    .orElse(fieldAccessor.propertySetter);
            propertyAccessors.put(propertyName, new PropertyAccessor(propertyName, propertyGetter, propertySetter));
        }
        return Map.copyOf(propertyAccessors);
    }

    static Map<String, PropertyAccessor> buildPropertyAccessorsByFields(
            Class<?> clazz, Set<String> excludedPropertyNames) {
        Map<String, PropertyAccessor> propertyAccessors = new HashMap<>();
        ReflectionUtils.doWithFields(clazz,
                field -> {
                    BiConsumer<Object, Object> propertySetter = (target, value) -> {
                        field.setAccessible(true);
                        ReflectionUtils.setField(field, target, value);
                    };
                    PropertyAccessor propertyAccessor = new PropertyAccessor(
                            field.getName(),
                            target -> {
                                field.setAccessible(true);
                                return ReflectionUtils.getField(field, target);
                            },
                            Modifier.isFinal(field.getModifiers()) ? null : propertySetter
                    );
                    propertyAccessors.put(field.getName(), propertyAccessor);
                },
                field -> !excludedPropertyNames.contains(field.getName())
        );
        return propertyAccessors;
    }

    static Map<String, PropertyAccessor> buildPropertyAccessorsByMethods(Class<?> clazz, Set<String> excludedPropertyNames) {
        Function<String, String> propertyNameConverter = methodName -> {
            String propertyName = methodName.substring(3);
            return propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);
        };

        // find getter and setter methods
        Map<String, Method> propertyGetterMethods = Maps.newHashMap();
        Map<String, List<Method>> propertySetterMethods = Maps.newHashMap();
        ReflectionUtils.doWithMethods(clazz,
                method -> {
                    String methodName = method.getName();
                    String propertyName = propertyNameConverter.apply(methodName);
                    if (excludedPropertyNames.contains(propertyName)) {
                        return;
                    }
                    if (methodName.startsWith("get")) {
                        propertyGetterMethods.put(propertyName, method);
                    } else {
                        propertySetterMethods.computeIfAbsent(propertyName, p -> Lists.newArrayListWithCapacity(1))
                                .add(method);
                    }
                },
                method -> {
                    if (Modifier.isStatic(method.getModifiers())) {
                        return false;
                    }
                    if (method.getName().startsWith("get") && method.getParameterCount() == 0) {
                        return true;
                    }
                    return method.getName().startsWith("set") && method.getParameterCount() == 1;
                }
        );

        // construct getter and setter implementation
        Set<String> propertyNames = Sets.union(propertyGetterMethods.keySet(), propertySetterMethods.keySet());
        Map<String, PropertyAccessor> propertyAccessors = new HashMap<>(propertyNames.size());
        for (String propertyName : propertyNames) {
            Function<Object, Object> propertyGetter = null;
            if (propertyGetterMethods.containsKey(propertyName)) {
                propertyGetter = target -> {
                    Method getterMethod = propertyGetterMethods.get(propertyName);
                    getterMethod.setAccessible(true);
                    return ReflectionUtils.invokeMethod(getterMethod, target);
                };
            }
            BiConsumer<Object, Object> propertySetter = null;
            List<Method> setterMethods = propertySetterMethods.get(propertyName);
            if (CollectionUtils.isNotEmpty(setterMethods)) {
                if (setterMethods.size() == 1) {
                    propertySetter = (target, value) -> {
                        Method setterMethod = setterMethods.get(0);
                        setterMethod.setAccessible(true);
                        ReflectionUtils.invokeMethod(setterMethod, target, value);
                    };
                } else {
                    propertySetter = (target, value) -> {
                        for (Method setter : setterMethods) {
                            if (setter.getParameterTypes()[0].isAssignableFrom(value.getClass())) {
                                setter.setAccessible(true);
                                ReflectionUtils.invokeMethod(setter, target, value);
                                return;
                            }
                        }
                        setterMethods.get(0).setAccessible(true);
                        ReflectionUtils.invokeMethod(setterMethods.get(0), target, value);
                    };
                }
            }

            // add to property accessors
            propertyAccessors.put(propertyName, new PropertyAccessor(propertyName, propertyGetter, propertySetter));
        }
        return propertyAccessors;
    }

    @JsonValue
    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = Maps.newHashMap(extendProperties);
        for (PropertyAccessor propertyAccessor : propertyAccessors.values()) {
            if (propertyAccessor.propertyGetter != null) {
                Object propertyValue = propertyAccessor.propertyGetter.apply(this);
                properties.put(propertyAccessor.propertyName, propertyValue);
            }
        }
        return properties;
    }

    @Override
    public Object getProperty(String key) {
        if (propertyAccessors.containsKey(key)) {
            Function<Object, Object> propertyGetter = propertyAccessors.get(key).propertyGetter;
            if (propertyGetter != null) {
                return propertyGetter.apply(this);
            } else {
                throw new IllegalArgumentException("Cannot get value of property \"" + key + "\"");
            }
        }
        return extendProperties.get(key);
    }

    @Override
    public <T> T getProperty(String key, Class<T> type) {
        Object property = getProperty(key);
        if (property == null) {
            return null;
        } else if (type.isAssignableFrom(property.getClass())) {
            return type.cast(property);
        }
        throw new IllegalArgumentException("Cannot cast property value as " + type.getTypeName());
    }

    @Override
    public MetadataProvider setProperty(String key, Object value) {
        if (this instanceof ImmutableMetadataProvider) {
            throw new UnsupportedOperationException("Not supported operation for immutable metadata");
        }
        if (propertyAccessors.containsKey(key)) {
            BiConsumer<Object, Object> propertySetter = propertyAccessors.get(key).propertySetter;
            if (propertySetter != null) {
                propertySetter.accept(this, value);
            } else {
                throw new IllegalArgumentException("Cannot update value of property \"" + key + "\"");
            }
        } else {
            extendProperties.put(key, value);
        }
        return this;
    }

    @Override
    public void removeProperty(String key) {
        if (this instanceof ImmutableMetadataProvider) {
            throw new UnsupportedOperationException("Not supported operation for immutable metadata");
        }
        if (propertyAccessors.containsKey(key)) {
            setProperty(key, null);
        } else {
            extendProperties.remove(key);
        }
    }

    @Override
    public void clear() {
        if (this instanceof ImmutableMetadataProvider) {
            throw new UnsupportedOperationException("Not supported operation for immutable metadata");
        }
        extendProperties.clear();
    }

    @Override
    public void merge(MetadataProvider other) {
        for (Map.Entry<String, Object> entry : other.getProperties().entrySet()) {
            setProperty(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public MetadataProvider union(MetadataProvider other) {
        return MapMetadataProvider.builder()
                .setProperties(getProperties())
                .setProperties(other.getProperties())
                .build();
    }

    protected record PropertyAccessor(
            String propertyName,
            Function<Object, Object> propertyGetter,
            BiConsumer<Object, Object> propertySetter) {
    }


    public static abstract class Builder<B> {
        private final Map<String, Object> extendProperties = Maps.newHashMap();

        protected abstract B self();

        public B setExtendProperty(String key, Object value) {
            this.extendProperties.put(key, value);
            return self();
        }

        public B setExtendProperties(Map<String, Object> extendProperties) {
            this.extendProperties.putAll(extendProperties);
            return self();
        }

        public Map<String, Object> getExtendProperties() {
            return extendProperties;
        }

        public abstract ClassMetadataProvider build();
    }
}
