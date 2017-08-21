package org.kite.movieindex.dao;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.LabelAndValue;
import org.apache.lucene.facet.range.DoubleRange;
import org.apache.lucene.facet.range.DoubleRangeFacetCounts;
import org.apache.lucene.facet.range.LongRangeFacetCounts;
import org.apache.lucene.facet.sortedset.DefaultSortedSetDocValuesReaderState;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetCounts;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesReaderState;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.kite.movieindex.entity.Group;
import org.kite.movieindex.entity.Movie;
import org.kite.movieindex.entity.SearchResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class MovieIndexer {
    @Value("{$main.index.path}")
    private String mainIndexPath;

    private File mainIndex;

    @PostConstruct
    public void init() {
        mainIndex = new File(mainIndexPath);
    }

    public void index(List<Movie> movies) throws IOException {
        try (
                StandardAnalyzer analyzer = new StandardAnalyzer();
                Directory index = new SimpleFSDirectory(mainIndex.toPath());
                IndexWriter writer = new IndexWriter(index, new IndexWriterConfig(analyzer).setOpenMode(
                        IndexWriterConfig.OpenMode.CREATE_OR_APPEND))
        ) {
            DocumentBuilder builder = new DocumentBuilder();
            FacetsConfig facetsConfig = builder.createFacetsConfig();
            for (Movie movie : movies) {
                writer.addDocument(facetsConfig.build(builder.build(movie)));
            }
        }
    }

    public SearchResult<Movie> search(Query query, Sort sort, int page, int pageSize) throws IOException {
        try (
                SimpleFSDirectory index = new SimpleFSDirectory(mainIndex.toPath());
                IndexReader reader = DirectoryReader.open(index)
        ) {
            if (reader.numDocs() == 0) {
                return new SearchResult<>(Collections.emptyList(), 0, 0);
            }

            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs docs;
            int numDocs = page * pageSize;
            if (sort == null) {
                docs = searcher.search(query, numDocs);
            } else {
                docs = searcher.search(query, numDocs, sort);
            }

            int totalHits = docs.totalHits;
            int pagesCount = (int) Math.ceil(totalHits / (double) pageSize);

            final ScoreDoc[] hits = docs.scoreDocs;

            int from = (page - 1) * pageSize;
            int to = Math.min(from + pageSize, hits.length);

            if (from > hits.length) {
                return new SearchResult<>(Collections.emptyList(), pagesCount, totalHits);
            }

            List<Movie> movies = new ArrayList<>();
            for (int i = from; i < to; i++) {
                Movie movie = MovieBuilder.build(searcher.doc(hits[i].doc));
                movies.add(movie);
            }

            return new SearchResult<>(movies, pagesCount, totalHits);
        }
    }

    public List<Group> groupBy(Query query, IndexField groupBy) throws IOException {
        try (
                SimpleFSDirectory index = new SimpleFSDirectory(mainIndex.toPath());
                IndexReader reader = DirectoryReader.open(index)
        ) {
            List<Group> res = new ArrayList<>();

            if (reader.numDocs() == 0) {
                return res;
            }

            IndexSearcher searcher = new IndexSearcher(reader);

            FacetsCollector collector = new FacetsCollector();
            FacetsCollector.search(searcher, query, 10, collector);

            Facets facets;
            if (groupBy == IndexField.RELEASE_DATE) {
                groupBy = IndexField.YEAR; // No one is going to group by actual date
            }

            switch (groupBy) {
                case RATING:
                    DoubleRange[] ranges = new DoubleRange[11];
                    for (int i = 0; i < 11; i++) {
                        ranges[i] = new DoubleRange(Integer.toString(i), i, true, i + 1, false);
                    }

                    facets = new DoubleRangeFacetCounts(groupBy.getFieldName(), collector, ranges);
                    break;
                default:
                    SortedSetDocValuesReaderState state = new DefaultSortedSetDocValuesReaderState(reader,
                            groupBy.getFacetField());
                    facets = new SortedSetDocValuesFacetCounts(state, collector);
            }

            FacetResult result = facets.getTopChildren(reader.numDocs(), groupBy.getFieldName());
            for (int i = 0; i < result.childCount; i++) {
                LabelAndValue lv = result.labelValues[i];
                res.add(new Group(lv.label, lv.value.intValue()));
            }

            return res;
        }
    }
}
