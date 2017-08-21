package org.kite.movieindex.entity;

import java.util.List;

/**
 * Created by Mikhail_Miroliubov on 8/2/2017.
 */
public class SearchResult<T> {
    private List<T> results;
    private Integer totalPagesCount;
    private Integer totalResultsCount;

    public SearchResult() {
    }

    public SearchResult(List<T> results, Integer totalPagesCount, Integer totalResultsCount) {
        this.results = results;
        this.totalPagesCount = totalPagesCount;
        this.totalResultsCount = totalResultsCount;
    }

    public List<T> getResults() {
        return results;
    }

    public void setResults(List<T> results) {
        this.results = results;
    }

    public Integer getTotalPagesCount() {
        return totalPagesCount;
    }

    public void setTotalPagesCount(Integer totalPagesCount) {
        this.totalPagesCount = totalPagesCount;
    }

    public Integer getTotalResultsCount() {
        return totalResultsCount;
    }

    public void setTotalResultsCount(Integer totalResultsCount) {
        this.totalResultsCount = totalResultsCount;
    }
}
