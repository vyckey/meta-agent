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

package org.metaagent.framework.tools.time;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * GetCurrentTimeOutput
 *
 * @author vyckey
 */
public record GetCurrentTimeOutput(
        @JsonProperty(required = true)
        @JsonPropertyDescription("The current timestamp with milliseconds")
        long timestamp,

        @JsonProperty(required = true)
        @JsonPropertyDescription("The current timezone")
        String timezone,

        @JsonProperty(required = true)
        @JsonPropertyDescription("The current time with format \"yyyy-MM-dd'T'HH:mm:ss\"")
        String dateTime,

        @JsonProperty(required = true)
        @JsonPropertyDescription("The day of week")
        DayOfWeek dayOfWeek) {
    public static GetCurrentTimeOutput fromTime(Instant instant, ZoneId zoneId) {
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(instant, zoneId);
        String dateStr = dateTime.withNano(0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return new GetCurrentTimeOutput(instant.toEpochMilli(), zoneId.getId(), dateStr, dateTime.getDayOfWeek());
    }

    public static GetCurrentTimeOutput fromNow(ZoneId zoneId) {
        return fromTime(Instant.now(), zoneId);
    }
}
