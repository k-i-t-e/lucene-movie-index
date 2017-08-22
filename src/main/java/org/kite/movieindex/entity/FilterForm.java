package org.kite.movieindex.entity;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortedSetSortField;
import org.kite.movieindex.dao.IndexField;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Mikhail_Miroliubov on 8/2/2017.
 */
public class FilterForm {
    private String searchString; // Can be film name or director or some one of the cast
    private String director;    // Additionally filter by director
    private String cast;  // Additionally filter by cast
    private Pair<Date, Date> releaseDateBetween;
    private Pair<Float, Float> ratingBetween;
    private FilterSection<Genre> genres;
    private int page = 1;
    private int pageSize = 10;
    private List<OrderBy> orderBy;

    public Query buildQuery() {
        if (isEmpty()) {
            return new MatchAllDocsQuery();
        }

        BooleanQuery.Builder builder = new BooleanQuery.Builder();

        addSearchStringQuery(builder);
        addDirectorQuery(builder);
        addCastQuery(builder);

        return builder.build();
    }

    public Sort buildSort() {
        if (CollectionUtils.isEmpty(orderBy)) {
            return null;
        }

        List<SortField> sortFields = orderBy.stream().map(o -> {
            if (o.getField().getType() == SortField.Type.STRING) {
                return new SortedSetSortField(o.getField().getFieldName(), o.isDesc());
            } else {
                return new SortField(o.getField().getFieldName(), o.getField().getType(), o.isDesc());
            }
        }).collect(Collectors.toList());

        return new Sort(sortFields.toArray(new SortField[sortFields.size()]));
    }

    public boolean isEmpty() {
        return StringUtils.isBlank(searchString) && StringUtils.isBlank(director) && StringUtils.isBlank(cast)
                && releaseDateBetween == null && ratingBetween == null && genres == null;
    }

    private void addSearchStringQuery(BooleanQuery.Builder builder) {
        if (StringUtils.isNotBlank(searchString)) {
            BooleanQuery.Builder searchStringBuilder = new BooleanQuery.Builder();
            searchStringBuilder.add(new FuzzyQuery(new Term(IndexField.NAME.getFieldName(), searchString)),
                    BooleanClause.Occur.SHOULD);
            searchStringBuilder.add(new FuzzyQuery(new Term(IndexField.DIRECTOR.getFieldName(), searchString)),
                    BooleanClause.Occur.SHOULD);
            searchStringBuilder.add(new FuzzyQuery(new Term(IndexField.CAST.getFieldName(), searchString)),
                    BooleanClause.Occur.SHOULD);

            builder.add(searchStringBuilder.build(), BooleanClause.Occur.MUST);
        }
    }

    private void addDirectorQuery(BooleanQuery.Builder builder) {
        if (StringUtils.isNotBlank(director)) {
            builder.add(new FuzzyQuery(new Term(IndexField.DIRECTOR.getFieldName(), director)),
                    BooleanClause.Occur.MUST);
        }
    }

    private void addCastQuery(BooleanQuery.Builder builder) {
        if (StringUtils.isNotBlank(cast)) {
            builder.add(new FuzzyQuery(new Term(IndexField.CAST.getFieldName(), cast)),
                    BooleanClause.Occur.MUST);
        }
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getCast() {
        return cast;
    }

    public void setCast(String cast) {
        this.cast = cast;
    }

    public Pair<Date, Date> getReleaseDateBetween() {
        return releaseDateBetween;
    }

    public void setReleaseDateBetween(Pair<Date, Date> releaseDateBetween) {
        this.releaseDateBetween = releaseDateBetween;
    }

    public Pair<Float, Float> getRatingBetween() {
        return ratingBetween;
    }

    public void setRatingBetween(Pair<Float, Float> ratingBetween) {
        this.ratingBetween = ratingBetween;
    }

    public FilterSection<Genre> getGenres() {
        return genres;
    }

    public void setGenres(FilterSection<Genre> genres) {
        this.genres = genres;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public List<OrderBy> getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(List<OrderBy> orderBy) {
        this.orderBy = orderBy;
    }

    public static class FilterSection<T> {
        private List<T> field;
        private boolean conjunction = false;

        public FilterSection(List<T> field, boolean conjunction) {
            this.field = field;
            this.conjunction = conjunction;
        }

        public FilterSection(List<T> field) {
            this.field = field;
        }

        public FilterSection() {
            // no-op
        }

        public List<T> getField() {
            return field;
        }

        public boolean isConjunction() {
            return conjunction;
        }

        public void setField(List<T> field) {
            this.field = field;
        }

        public void setConjunction(boolean conjunction) {
            this.conjunction = conjunction;
        }
    }

    public static class OrderBy {
        private IndexField field;
        private boolean desc = false;

        public OrderBy() {
            // no-op
        }

        public OrderBy(IndexField field, boolean desc) {
            this.field = field;
            this.desc = desc;
        }

        public IndexField getField() {
            return field;
        }

        public void setField(IndexField field) {
            this.field = field;
        }

        public boolean isDesc() {
            return desc;
        }

        public void setDesc(boolean desc) {
            this.desc = desc;
        }
    }
}
