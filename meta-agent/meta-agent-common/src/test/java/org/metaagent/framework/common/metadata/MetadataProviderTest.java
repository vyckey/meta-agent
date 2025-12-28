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

import org.junit.jupiter.api.Test;
import org.metaagent.framework.common.json.JsonObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MetadataProviderTest {
    @Test
    void testMapMetadataProviderSerialize() {
        MapMetadataProvider metadata = MapMetadataProvider.builder()
                .setProperty("name", "meta-agent")
                .setProperty("author", "vyckey")
                .setProperty("version", 1.0)
                .build();
        String json = JsonObjectMapper.CAMEL_CASE.toJson(metadata);
        MapMetadataProvider metadata2 = JsonObjectMapper.CAMEL_CASE.fromJson(json, MapMetadataProvider.class);
        assertEquals(metadata, metadata2);

        ImmutableMetadataProvider metadata3 = metadata.immutable();
        String json2 = JsonObjectMapper.CAMEL_CASE.toJson(metadata3);
        ImmutableMetadataWrapper metadata4 = JsonObjectMapper.CAMEL_CASE.fromJson(json2, ImmutableMetadataWrapper.class);
        assertEquals(metadata3, metadata4);
    }


    @Test
    void testClassMetadataProvider() {
        TestClassMetadataProvider metadata = new TestClassMetadataProvider("Test");
        assertEquals("Test", metadata.getName());
        assertEquals("Test", metadata.getName());
        assertNull(metadata.getProperty("description"));
        assertEquals("1.0.0", metadata.getProperty("version"));

        metadata.setProperty("description", "  This is a test description  ");
        assertEquals("This is a test description", metadata.getProperty("description"));
        metadata.setProperty("description", null);
        assertEquals("", metadata.getProperty("description"));

        assertThrows(IllegalArgumentException.class, () -> metadata.setProperty("name", "any name"));
        assertThrows(IllegalArgumentException.class, () -> metadata.setProperty("version", "2.0.0"));
    }

    static class TestClassMetadataProvider extends ClassMetadataProvider {
        final static String version = "1.0.0";
        private final String name;
        private String description;

        TestClassMetadataProvider(String name) {
            this.name = name;
        }

        String getName() {
            return name;
        }

        String getDescription() {
            return description;
        }

        void setDescription(String description) {
            this.description = description != null ? description.trim() : "";
        }
    }
}