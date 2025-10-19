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

package org.metaagent.framework.common.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class IOUtils extends org.apache.commons.io.IOUtils {

    public static String fileToString(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + filePath);
        }
        return toString(file.toURI().toURL(), StandardCharsets.UTF_8);
    }

    public static String resourceToString(String resourceLocation) throws IOException {
        if (resourceLocation.startsWith("classpath:")) {
            resourceLocation = resourceLocation.substring("classpath:".length());
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(resourceLocation);
        if (inputStream == null) {
            throw new IOException("Resource not found: " + resourceLocation);
        }
        try {
            return toString(inputStream, StandardCharsets.UTF_8);
        } finally {
            closeQuietly(inputStream);
        }
    }

    public static String readToString(String resourceLocation) throws IOException {
        try {
            return resourceToString(resourceLocation);
        } catch (IOException e) {
            if (resourceLocation.startsWith("classpath:")) {
                throw e;
            }
        }

        try {
            return fileToString(resourceLocation);
        } catch (Exception e) {
            if (resourceLocation.startsWith("file:")) {
                throw e;
            }
        }

        try {
            // Try to read as a URL
            URL url = new URL(resourceLocation);
            return toString(url, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Ignore
        }
        throw new IOException("Not found resource: " + resourceLocation);
    }
}
