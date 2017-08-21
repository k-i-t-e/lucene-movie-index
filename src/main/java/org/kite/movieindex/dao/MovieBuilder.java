package org.kite.movieindex.dao;

import static org.kite.movieindex.dao.IndexField.*;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.kite.movieindex.entity.Genre;
import org.kite.movieindex.entity.Movie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by Mikhail_Miroliubov on 8/3/2017.
 */
public class MovieBuilder {
    public static Movie build(Document document) {
        Movie movie = new Movie();

        movie.setName(document.get(NAME.getFieldName()));
        movie.setDirector(document.get(DIRECTOR.getFieldName()));
        movie.setCast(Arrays.asList(document.getValues(CAST.getFieldName())));

        IndexableField field = document.getField(RATING.getFieldName());
        if (field != null) {
            movie.setRating(field.numericValue().floatValue());
        }

        field = document.getField(RELEASE_DATE.getFieldName());
        if (field != null) {
            movie.setReleaseDate(new Date(field.numericValue().longValue()));
        }

        List<Genre> genres = new ArrayList<>();
        for (String genreValue : document.getValues(GENRE.getFieldName())) {
            genres.add(Genre.valueOf(genreValue.toUpperCase()));
        }

        movie.setGenres(genres);
        return movie;
    }
}
