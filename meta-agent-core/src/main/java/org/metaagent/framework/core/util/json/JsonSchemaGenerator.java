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

package org.metaagent.framework.core.util.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.Option;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import com.github.victools.jsonschema.module.jackson.JacksonOption;
import com.github.victools.jsonschema.module.swagger2.Swagger2Module;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Utilities to generate JSON Schemas from Java entities.
 *
 * @author vyckey
 */
public final class JsonSchemaGenerator {

    private static final SchemaGenerator TYPE_SCHEMA_GENERATOR;

    private static final SchemaGenerator SUBTYPE_SCHEMA_GENERATOR;

    /*
     * Initialize JSON Schema generators.
     */
    static {
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(
                SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON)
                .with(new JacksonModule(JacksonOption.RESPECT_JSONPROPERTY_REQUIRED))
                .with(new Swagger2Module())
                .with(Option.EXTRA_OPEN_API_FORMAT_VALUES)
                .with(Option.PLAIN_DEFINITION_KEYS);

        SchemaGeneratorConfig typeSchemaGeneratorConfig = configBuilder.without(Option.SCHEMA_VERSION_INDICATOR).build();
        TYPE_SCHEMA_GENERATOR = new SchemaGenerator(typeSchemaGeneratorConfig);

        SchemaGeneratorConfig subtypeSchemaGeneratorConfig = configBuilder.build();
        SUBTYPE_SCHEMA_GENERATOR = new SchemaGenerator(subtypeSchemaGeneratorConfig);
    }

    private JsonSchemaGenerator() {
    }

    /**
     * Generate a JSON Schema for a method's input parameters.
     */
    public static String generateForMethod(Method method, SchemaOption... schemaOptions) {
        ObjectNode schema = new ObjectMapper().createObjectNode();
        schema.put("$schema", SchemaVersion.DRAFT_2020_12.getIdentifier());
        schema.put("type", "object");

        ObjectNode properties = schema.putObject("properties");
        List<String> requiredParameters = new ArrayList<>();

        for (int i = 0; i < method.getParameterCount(); i++) {
            String parameterName = method.getParameters()[i].getName();
            Type parameterType = method.getGenericParameterTypes()[i];
            properties.set(parameterName, SUBTYPE_SCHEMA_GENERATOR.generateSchema(parameterType));
            if (isParameterRequired(method, i)) {
                requiredParameters.add(parameterName);
            }
        }

        ArrayNode requiredArray = schema.putArray("required");
        if (SchemaOption.hasOption(schemaOptions, SchemaOption.RESPECT_JSON_PROPERTY_REQUIRED)) {
            requiredParameters.forEach(requiredArray::add);
        } else {
            Stream.of(method.getParameters()).map(Parameter::getName).forEach(requiredArray::add);
        }

        if (SchemaOption.noOption(schemaOptions, SchemaOption.ALLOW_ADDITIONAL_PROPERTIES_BY_DEFAULT)) {
            schema.put("additionalProperties", false);
        }

        if (SchemaOption.hasOption(schemaOptions, SchemaOption.UPPER_CASE_TYPE_VALUES)) {
            convertTypeValuesToUpperCase(schema);
        }
        return schema.toPrettyString();
    }

    private static boolean isParameterRequired(Method method, int index) {
        JsonProperty jsonPropertyAnnotation = method.getParameters()[index].getAnnotation(JsonProperty.class);
        return jsonPropertyAnnotation != null && jsonPropertyAnnotation.required();
    }

    /**
     * Generate a JSON Schema for a class type.
     */
    public static String generateForType(Type type, SchemaOption... schemaOptions) {
        ObjectNode schema = TYPE_SCHEMA_GENERATOR.generateSchema(
                Objects.requireNonNull(type, "type cannot be null"));
        if ((type == Void.class) && !schema.has("properties")) {
            schema.putObject("properties");
        }
        if (SchemaOption.noOption(schemaOptions, SchemaOption.ALLOW_ADDITIONAL_PROPERTIES_BY_DEFAULT)) {
            schema.put("additionalProperties", false);
        }
        if (SchemaOption.hasOption(schemaOptions, SchemaOption.UPPER_CASE_TYPE_VALUES)) {
            convertTypeValuesToUpperCase(schema);
        }
        return schema.toPrettyString();
    }

    private static void convertTypeValuesToUpperCase(ObjectNode node) {
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                JsonNode value = entry.getValue();
                if (value.isObject()) {
                    convertTypeValuesToUpperCase((ObjectNode) value);
                } else if (value.isArray()) {
                    value.elements().forEachRemaining(element -> {
                        if (element.isObject() || element.isArray()) {
                            convertTypeValuesToUpperCase((ObjectNode) element);
                        }
                    });
                } else if (value.isTextual() && entry.getKey().equals("type")) {
                    String oldValue = node.get("type").asText();
                    node.put("type", oldValue.toUpperCase());
                }
            });
        } else if (node.isArray()) {
            node.elements().forEachRemaining(element -> {
                if (element.isObject() || element.isArray()) {
                    convertTypeValuesToUpperCase((ObjectNode) element);
                }
            });
        }
    }

    /**
     * Options for generating JSON Schemas.
     */
    public enum SchemaOption {

        /**
         * Properties are only required if marked as such via the Jackson annotation
         * "@JsonProperty(required = true)".
         */
        RESPECT_JSON_PROPERTY_REQUIRED,

        /**
         * Allow additional properties by default.
         */
        ALLOW_ADDITIONAL_PROPERTIES_BY_DEFAULT,

        /**
         * Convert all "type" values to upper case.
         */
        UPPER_CASE_TYPE_VALUES,
        ;

        public static boolean hasOption(SchemaOption[] options, SchemaOption option) {
            return Stream.of(options).anyMatch(o -> o == option);
        }

        public static boolean noOption(SchemaOption[] options, SchemaOption option) {
            return Stream.of(options).noneMatch(o -> o == option);
        }
    }
}
