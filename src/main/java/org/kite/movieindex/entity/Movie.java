package org.kite.movieindex.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Movie {
    private String name;
    private String director;
    private List<String> cast;
    private Date releaseDate;
    private Float rating;
    private List<Genre> genres;

    public Movie() {
    }

    public Movie(Movie other) {
        name = other.name;
        director = other.director;

        if (other.getCast() != null) {
            cast = new ArrayList<>(other.cast);
        }

        releaseDate = other.releaseDate;
        rating = other.rating;

        if (other.getGenres() != null) {
            genres = new ArrayList<>(other.genres);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public List<String> getCast() {
        return cast;
    }

    public void setCast(List<String> cast) {
        this.cast = cast;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }
}
