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

package org.metaagent.framework.core.tool.definition;

import org.metaagent.framework.core.util.json.JsonSchemaGenerator;

import java.lang.reflect.Type;

/**
 * description is here
 *
 * @author vyckey
 */
public record DefaultToolDefinition(String name, String description, String inputSchema, String outputSchema)
        implements ToolDefinition {

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static class Builder {
        private final String name;
        private String description;
        private String inputSchema;
        private String outputSchema;

        private Builder(String name) {
            this.name = name;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder inputSchema(String inputSchema) {
            this.inputSchema = inputSchema;
            return this;
        }

        public Builder inputSchema(Type inputType, JsonSchemaGenerator.SchemaOption... options) {
            this.inputSchema = JsonSchemaGenerator.generateForType(inputType, options);
            return this;
        }

        public Builder outputSchema(String outputSchema) {
            this.outputSchema = outputSchema;
            return this;
        }

        public Builder outputSchema(Type outputType, JsonSchemaGenerator.SchemaOption... options) {
            this.outputSchema = JsonSchemaGenerator.generateForType(outputType, options);
            return this;
        }

        public ToolDefinition build() {
            return new DefaultToolDefinition(name, description, inputSchema, outputSchema);
        }
    }

    @Override
    public String toString() {
        return "Tool - " + name + ": " + description +
                "\n\nInput Schema:\n" + inputSchema + "\n\nOutput Schema:\n" + outputSchema;
    }
}
