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

package org.metaagent.framework.core.model.chat.metadata;

import org.metaagent.framework.common.metadata.ClassMetadataProvider;
import org.metaagent.framework.common.metadata.ImmutableMetadataProvider;

import java.time.Instant;

/**
 * Default implementation of {@link ChatModelMetadata}
 *
 * @author vyckey
 */
public class DefaultChatModelMetadata extends ClassMetadataProvider implements ChatModelMetadata, ImmutableMetadataProvider {
    private final Instant cutOffDay;
    private final int maxWindowSize;

    private DefaultChatModelMetadata(Builder builder) {
        super(builder.getExtendProperties());
        this.cutOffDay = builder.cutOffDay;
        this.maxWindowSize = builder.maxWindowSize;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Instant getCutOffDay() {
        return cutOffDay;
    }

    @Override
    public int getMaxWindowSize() {
        return maxWindowSize;
    }

    public static class Builder extends ClassMetadataProvider.Builder<Builder> {
        private Instant cutOffDay;
        private int maxWindowSize = -1;

        private Builder() {
        }

        @Override
        protected Builder self() {
            return this;
        }

        public Builder cutOffDay(Instant cutOffDay) {
            this.cutOffDay = cutOffDay;
            return this;
        }

        public Builder maxWindowSize(int maxWindowSize) {
            this.maxWindowSize = maxWindowSize;
            return this;
        }

        @Override
        public DefaultChatModelMetadata build() {
            return new DefaultChatModelMetadata(this);
        }
    }
}
