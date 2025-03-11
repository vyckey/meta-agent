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

package org.metaagent.thirdparty.google.api.searchapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SearchApiWebSearchRequest {
    /**
     * The engine that will be used to retrieve real-time data. It must be set to google.
     */
    @Builder.Default
    @JsonProperty(required = true)
    private String engine = "google";

    /**
     * The api_key authenticates your requests. Use it as a query parameter
     * ({@code https://www.searchapi.io/api/v1/search?api_key=YOUR_API_KEY})
     * or in the Authorization header ({@code Bearer YOUR_API_KEY}).
     */
    @JsonProperty(required = true, value = "api_key")
    private String apiKey;

    /**
     * The query you want to search. You can use anything that you would use in a regular Google search.
     * e.g. {@code inurl:}, {@code site:}, {@code intitle:}.
     */
    @JsonProperty(required = true, value = "q")
    private String query;

    /**
     * Includes desktop, mobile, tablet. Default is desktop.
     */
    private String device;

    /**
     * Where you want the search to originate. If several locations match the location requested, we'll pick the most popular one.
     */
    private String location;

    /**
     * The Google encoded location you want to use for the search. SearchApi automatically generated the {@code uule}
     * parameter when you use the {@code location} parameter but we allow you to overwrite it directly.
     * {@code uule} and {@code location} parameters can't be used together.
     */
    private String uule;

    /**
     * The Google domain of the search. Default is google.com.
     */
    private String googleDomain;

    /**
     * The country of the search. Default is us.
     */
    private String gl;

    /**
     * The interface language of the search. Default is en.
     */
    private String hl;

    /**
     * The lr parameter restricts search results to documents written in a particular language or a set of languages.
     */
    private String lr;

    /**
     * The cr parameter restricts search results to documents originating in a particular country.
     */
    private String cr;

    /**
     * This parameter controls whether results from queries that have been auto-corrected for spelling errors are included.
     * To exclude these auto-corrected results, set the value to 1.
     * By default, the value is 0, meaning auto-corrected results are included.
     */
    private Integer nfpr;

    /**
     * This parameter controls whether the "Duplicate Content" and "Host Crowding" filters are enabled.
     * Set the value to 1 to enable these filters, which is the default setting.
     * To disable these filters, set the value to 0.
     */
    private Integer filter;

    /**
     * This parameter toggles the SafeSearch feature for the results.
     * SafeSearch operates by filtering out adult content from your search results.
     */
    private String safe;

    /**
     * This parameter restricts results to URLs based on date. Supported values are:
     * <li>last_hour: data from the past hour.</li>
     * <li>last_day: data from the past 24 hours.</li>
     * <li>last_week: data from the past week.</li>
     * <li>last_month: data from the past month.</li>
     * <li>last_year: data from the past year.</li>
     * Using time_period_min or time_period_max parameters, you can specify a custom time period.
     */
    private String timePeriod;

    /**
     * This parameter specifies the start of the time period. It could be used in combination with the time_period_max parameter.
     * The value should be in the format {@code MM/DD/YYYY}.
     */
    private String timePeriodMin;

    /**
     * This parameter specifies the end of the time period. It could be used in combination with the time_period_min parameter.
     * The value should be in the format {@code MM/DD/YYYY}.
     */
    private String timePeriodMax;

    /**
     * The number of results to display per page.
     * Use in combination with the {@code page} parameter to implement pagination functionality.
     */
    private Integer num;

    /**
     * The page number of the search results. Default is 1.
     * Use in combination with the {@code num} parameter to implement pagination functionality.
     */
    private Integer page;

    /**
     * Controls how the search request is optimized. Available options:
     * <li>performance: Default</li>
     * <li>ads: optimizes for higher ad collection success rate, which may result in longer request processing times.</li>
     */
    private String optimizationStrategy;
}
