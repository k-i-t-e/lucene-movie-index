package org.kite.movieindex.dao;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleDocValuesField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FloatDocValuesField;
import org.apache.lucene.document.FloatPoint;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedSetDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetField;
import org.apache.lucene.util.BytesRef;
import org.kite.movieindex.entity.Genre;
import org.kite.movieindex.entity.Movie;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Created by Mikhail_Miroliubov on 8/2/2017.
 */
public class DocumentBuilder {
    public Document build(Movie movie) {
        Document document = new Document();

        document.add(new TextField(IndexField.NAME.getFieldName(), movie.getName(), Field.Store.YES));
        document.add(new SortedSetDocValuesField(IndexField.NAME.getFieldName(), new BytesRef(movie.getName())));

        document.add(new TextField(IndexField.DIRECTOR.getFieldName(), movie.getDirector(), Field.Store.YES));
        document.add(new SortedSetDocValuesField(IndexField.DIRECTOR.getFieldName(), new BytesRef(movie.getDirector())));
        document.add(new SortedSetDocValuesFacetField(IndexField.DIRECTOR.getFieldName(), movie.getDirector()));

        for (String actor : movie.getCast()) {
            document.add(new TextField(IndexField.CAST.getFieldName(), actor, Field.Store.YES));
            document.add(new SortedSetDocValuesField(IndexField.CAST.getFieldName(), new BytesRef(actor)));
            document.add(new SortedSetDocValuesFacetField(IndexField.CAST.getFieldName(), actor));
        }

        LocalDate date = movie.getReleaseDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        document.add(new IntPoint(IndexField.YEAR.getFieldName(), date.getYear())); // we wanna have facets for release year
        document.add(new StoredField(IndexField.YEAR.getFieldName(), date.getYear()));
        document.add(new NumericDocValuesField(IndexField.YEAR.getFieldName(), date.getYear()));
        document.add(new SortedSetDocValuesFacetField(IndexField.YEAR.getFieldName(), Integer.toString(date.getYear())));

        document.add(new LongPoint(IndexField.RELEASE_DATE.getFieldName(), movie.getReleaseDate().getTime()));
        document.add(new StoredField(IndexField.RELEASE_DATE.getFieldName(), movie.getReleaseDate().getTime()));
        document.add(new NumericDocValuesField(IndexField.RELEASE_DATE.getFieldName(), movie.getReleaseDate().getTime()));

        document.add(new FloatPoint(IndexField.RATING.getFieldName(), movie.getRating()));
        document.add(new StoredField(IndexField.RATING.getFieldName(), movie.getRating()));
        document.add(new DoubleDocValuesField(IndexField.RATING.getFieldName(), movie.getRating()));

        for (Genre genre : movie.getGenres()) {
            String genreValue = genre.name().toLowerCase();
            document.add(new StringField(IndexField.GENRE.getFieldName(), genreValue, Field.Store.YES));
            document.add(new SortedSetDocValuesField(IndexField.GENRE.getFieldName(), new BytesRef(genreValue)));
            document.add(new SortedSetDocValuesFacetField(IndexField.GENRE.getFieldName(), genreValue));
        }

        return document;
    }

    public FacetsConfig createFacetsConfig() {
        FacetsConfig config = new FacetsConfig();
        config.setIndexFieldName(IndexField.DIRECTOR.getFieldName(), IndexField.DIRECTOR.getFacetField());

        config.setIndexFieldName(IndexField.CAST.getFieldName(), IndexField.CAST.getFacetField());
        config.setMultiValued(IndexField.CAST.getFieldName(), true);

        config.setIndexFieldName(IndexField.GENRE.getFieldName(), IndexField.GENRE.getFacetField());
        config.setMultiValued(IndexField.GENRE.getFieldName(), true);

        config.setIndexFieldName(IndexField.YEAR.getFieldName(), IndexField.YEAR.getFacetField());
        return config;
    }
}
