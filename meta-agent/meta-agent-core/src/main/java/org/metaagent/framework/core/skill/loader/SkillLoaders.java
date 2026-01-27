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

package org.metaagent.framework.core.skill.loader;

import org.metaagent.framework.core.skill.exception.SkillParseException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SkillLoaders {
    public static URL resolveLocation(URL location, String relativePath) {
        String protocol = location.getProtocol();
        return switch (protocol) {
            case "file" -> resolveFileLocation(location, relativePath);
            case "http", "https" -> resolveRemoteLocation(location, relativePath);
            default -> throw new SkillParseException("Unsupported protocol: " + protocol);
        };
    }

    private static URL resolveFileLocation(URL fileUrl, String relativePath) {
        try {
            URI uri = fileUrl.toURI();
            Path resolvedPath = Paths.get(uri).resolve(relativePath);
            if (!resolvedPath.isAbsolute()) {
                resolvedPath = resolvedPath.toAbsolutePath();
            }
            return resolvedPath.normalize().toUri().toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new SkillParseException("Cannot concat file path '" + fileUrl + "' and '" + relativePath + "'", e);
        }
    }

    private static URL resolveRemoteLocation(URL remoteUrl, String relativePath) {
        try {
            Path relPath = Paths.get(relativePath).normalize();
            if (relPath.isAbsolute()) {
                throw new MalformedURLException("'" + relativePath + "' isn't a relative path.");
            }
            String remotePath = remoteUrl.getPath();
            return new URL(remoteUrl, relPath.toString());
        } catch (MalformedURLException e) {
            throw new SkillParseException("Cannot concat remote path '" + remoteUrl + "' and '" + relativePath + "'", e);
        }
    }

    public static SkillLoader fileBasedSkillLoader() {
        return new FileBasedSkillLoader();
    }

}
