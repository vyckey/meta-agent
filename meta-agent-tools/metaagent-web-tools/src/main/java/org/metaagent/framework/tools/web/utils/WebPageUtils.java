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

package org.metaagent.framework.tools.web.utils;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.metaagent.framework.common.io.IOUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class WebPageUtils {
    private static final Pattern HTML_TITLE_PATTERN = Pattern.compile(
            "<title[^>]*>([^<]+)</title>",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern DOCTYPE_PATTERN = Pattern.compile(
            "<!doctype[^>]*>",
            Pattern.CASE_INSENSITIVE
    );
    /**
     * Match HTML script, style, and comment tags
     */
    private static final Pattern MEANINGLESS_TAGS_PATTERN = Pattern.compile(
            "(<script[^>]*>[\\s\\S]*?</script>)|(<style[^>]*>[\\s\\S]*?</style>)|(<!--[\\s\\S]*?-->)",
            Pattern.CASE_INSENSITIVE
    );
    /**
     * Match inline style tags
     */
    private static final Pattern INLINE_STYLE_PATTERN = Pattern.compile(
            "\\s+style\\s*=\\s*([\"']).*?[\"']",
            Pattern.CASE_INSENSITIVE
    );
    /**
     * Match event handler
     */
    private static final Pattern EVENT_HANDLER_PATTERN = Pattern.compile(
            "\\s+on\\w+\\s*=\\s*([\"']).*?[\"']",
            Pattern.CASE_INSENSITIVE
    );
    /**
     * Match class, id, style, data-*, rel, target, tabindex, role, aria-* attributes
     */
    private static final Pattern MEANINGLESS_ATTRIBUTES_PATTERN = Pattern.compile(
            "(?:class|id|style|data-[\\w-]+|rel|target|tabindex|role|aria-[\\w-]+)\\s*=\\s*([\"']).*?\\1",
            Pattern.CASE_INSENSITIVE
    );

    private static final List<String> USER_AGENT_LIST = Lists.newArrayList();

    static {
        try {
            String text = IOUtils.readToString("tools/http/user_agents.txt");
            Arrays.stream(text.split("\n")).map(String::trim).filter(s -> !s.isEmpty()).forEach(USER_AGENT_LIST::add);
        } catch (IOException e) {
            log.warn("Failed to load tools/http/user_agents.txt file", e);
        }
    }

    public static String extractHtmlTitle(String htmlText) {
        Matcher matcher = HTML_TITLE_PATTERN.matcher(htmlText);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public static String normalizeHtmlText(String htmlText) {
        String cleanHtml = htmlText;
        for (Pattern pattern : Arrays.asList(
                DOCTYPE_PATTERN,
                MEANINGLESS_TAGS_PATTERN, INLINE_STYLE_PATTERN,
                EVENT_HANDLER_PATTERN, MEANINGLESS_ATTRIBUTES_PATTERN
        )) {
            cleanHtml = pattern.matcher(cleanHtml).replaceAll("");
        }
        return cleanHtml.replaceAll("\\s+", " ")
                .replaceAll("\\n{3,}", "\n\n");
    }

    public static String randomUserAgent() {
        String defaultUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3";
        if (USER_AGENT_LIST.isEmpty()) {
            return defaultUserAgent;
        }
        return USER_AGENT_LIST.get(new Random().nextInt(USER_AGENT_LIST.size()));
    }

}
