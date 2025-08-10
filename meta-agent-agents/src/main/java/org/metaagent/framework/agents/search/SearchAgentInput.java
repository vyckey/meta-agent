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

package org.metaagent.framework.agents.search;

import org.metaagent.framework.core.agent.AgentExecutionContext;
import org.metaagent.framework.core.agent.input.AbstractAgentInput;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.common.metadata.MetadataProvider;

import java.util.Objects;

/**
 * Search agent input schema.
 *
 * @param query          the search query
 * @param detailIncluded whether to include detailed results
 */
public record SearchAgentInput(
        AgentExecutionContext context,
        String query,
        String queryContext,
        boolean forceSearch,
        boolean detailIncluded) implements AgentInput {

    public static SearchAgentInput from(String query) {
        return SearchAgentInput.builder().query(query).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractAgentInput.Builder<Builder> {
        private AgentExecutionContext context;
        private String query;
        private String queryContext;
        private boolean forceSearch = false;
        private boolean detailIncluded = true;

        private Builder() {
        }

        @Override
        protected Builder self() {
            return this;
        }

        public Builder context(AgentExecutionContext context) {
            this.context = context;
            return this;
        }

        public Builder query(String query) {
            this.query = Objects.requireNonNull(query, "query is required");
            return this;
        }

        public Builder queryContext(String queryContext) {
            this.queryContext = queryContext;
            return this;
        }

        public Builder forceSearch(boolean forceSearch) {
            this.forceSearch = forceSearch;
            return this;
        }

        public Builder detailIncluded(boolean detailIncluded) {
            this.detailIncluded = detailIncluded;
            return this;
        }

        public SearchAgentInput build() {
            if (context == null) {
                context = AgentExecutionContext.create();
            }
            if (metadata == null) {
                metadata = MetadataProvider.empty();
            }
            return new SearchAgentInput(context, query, queryContext, forceSearch, detailIncluded);
        }
    }
}
