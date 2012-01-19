package com.elasticsearch.omg.support.model.result;

import java.util.Collection;

/**
 * Model for holding a collection of {@link ElasticSearchResult}s and other elastic search result information and metadata.
 */
public class ElasticSearchResults<T> {
    private Long                               totalResults;
    private Collection<ElasticSearchResult<T>> results;
    private String                             paginationKey;

    /**
     * The total number of hits from a search
     *
     * @return the total hits
     */
    public Long getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(Long totalResults) {
        this.totalResults = totalResults;
    }

    /**
     * The {@link ElasticSearchResult}s from a search
     *
     * @return the results
     */
    public Collection<ElasticSearchResult<T>> getResults() {
        return results;
    }

    public void setResults(Collection<ElasticSearchResult<T>> results) {
        this.results = results;
    }

    /**
     * Key used for pagination by elastic search when scrolling a search result
     *
     * @return the key
     */
    public String getPaginationKey() {
        return paginationKey;
    }

    public void setPaginationKey(String paginationKey) {
        this.paginationKey = paginationKey;
    }
}
