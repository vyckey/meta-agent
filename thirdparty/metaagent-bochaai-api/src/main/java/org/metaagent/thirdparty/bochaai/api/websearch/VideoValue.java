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

package org.metaagent.thirdparty.bochaai.api.websearch;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.List;

public record VideoValue(
        String webSearchUrl,
        String name,
        String description,
        String thumbnailUrl,
        List<Publisher> publisher,
        Creator creator,
        String contentUrl,
        String hostPageUrl,
        String encodingFormat,
        String hostPageDisplayUrl,
        Integer width,
        Integer height,
        String duration,
        String motionThumbnailUrl,
        String embedHtml,
        Boolean allowHttpsEmbed,
        Integer viewCount,
        Thumbnail thumbnail,
        Boolean allowMobileEmbed,
        Boolean isSuperfresh,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        String datePublished) {

    public record Creator(String name) {
    }

    record Publisher(String name) {
    }
}
