package org.kite.movieindex.dao;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kite.movieindex.entity.FilterForm;
import org.kite.movieindex.entity.Genre;
import org.kite.movieindex.entity.Group;
import org.kite.movieindex.entity.Movie;
import org.kite.movieindex.entity.SearchResult;
import org.kite.movieindex.util.TestUtil;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.internal.util.reflection.Whitebox;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class MovieIndexerTest {
    @Spy
    private MovieIndexer indexer = new MovieIndexer();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(MovieIndexerTest.class);
        Whitebox.setInternalState(indexer, "mainIndex", new File("./test-index.luc"));
    }

    @Test
    public void search() throws Exception {
        Movie movie = new Movie();
        movie.setName("Pulp Fiction");
        movie.setDirector("Quentin Tarantino");
        movie.setCast(Arrays.asList("John Travolta", "Samuel L Jackson", "Uma Thurman", "Bruce Willis"));
        movie.setGenres(Arrays.asList(Genre.ACTION, Genre.COMEDY));
        movie.setRating(10.0F);
        movie.setReleaseDate(new Date());

        indexer.index(Collections.singletonList(movie));

        FilterForm filterForm = new FilterForm();
        filterForm.setDirector("Tarantino");

        SearchResult<Movie> res = indexer.search(filterForm.buildQuery(), null, filterForm.getPage(),
                filterForm.getPageSize());

        Assert.assertFalse(res.getResults().isEmpty());
        for (Movie m : res.getResults()) {
            Assert.assertEquals(movie.getName(), m.getName());
            Assert.assertEquals(movie.getDirector(), m.getDirector());
            TestUtil.assertListEquals(movie.getGenres(), m.getGenres());
            TestUtil.assertListEquals(movie.getCast(), m.getCast());
            Assert.assertEquals(movie.getRating(), m.getRating());
            Assert.assertEquals(movie.getReleaseDate(), m.getReleaseDate());
        }
    }

    @Test
    public void searchComplex() throws IOException
    {
        List<Movie> movies = createMovies();
        indexer.index(movies);

        FilterForm filterForm = new FilterForm();
        filterForm.setSearchString("Tarantino");

        SearchResult<Movie> res = indexer.search(filterForm.buildQuery(), null, filterForm.getPage(),
                filterForm.getPageSize());
        checkContainsString(res, "Tarantino");

        filterForm.setSearchString("willis");
        res = indexer.search(filterForm.buildQuery(), null, filterForm.getPage(),
                filterForm.getPageSize());
        checkContainsString(res, "Willis");
    }

    @Test
    public void testSorting() throws IOException
    {
        List<Movie> movies = createMovies();
        indexer.index(movies);

        FilterForm filterForm = new FilterForm();

        testSorting(filterForm, new FilterForm.OrderBy(IndexField.NAME, false), Comparator.comparing(Movie::getName));
        testSorting(filterForm, new FilterForm.OrderBy(IndexField.DIRECTOR, false), Comparator.comparing(Movie::getDirector));
        testSorting(filterForm, new FilterForm.OrderBy(IndexField.RATING, false), Comparator.comparing(Movie::getRating));
        testSorting(filterForm, new FilterForm.OrderBy(IndexField.RELEASE_DATE, false), Comparator.comparing(Movie::getReleaseDate));
        testSorting(filterForm, new FilterForm.OrderBy(IndexField.CAST, false), (o1, o2) -> {
            Collections.sort(o1.getCast());
            Collections.sort(o2.getCast());
            return o1.getCast().get(0).compareTo(o2.getCast().get(0));
        });

        testSorting(filterForm, new FilterForm.OrderBy(IndexField.GENRE, false), (o1, o2) -> {
            Collections.sort(o1.getGenres());
            Collections.sort(o2.getGenres());
            return o1.getGenres().get(0).compareTo(o2.getGenres().get(0));
        });
    }

    private void testSorting(FilterForm filterForm, FilterForm.OrderBy orderBy, Comparator<Movie> comparator) throws IOException
    {
        filterForm.setOrderBy(Collections.singletonList(orderBy));
        SearchResult<Movie> res = indexer.search(filterForm.buildQuery(), filterForm.buildSort(), filterForm.getPage(),
                filterForm.getPageSize());

        Assert.assertFalse(res.getResults().isEmpty());
        Assert.assertTrue(checkSorting(res.getResults(), comparator));
    }

    private boolean checkSorting(List<Movie> movies, Comparator<Movie> comparator) {
        for (int i = 0; i < movies.size(); i++) {
            if (i > 0) {
                if (comparator.compare(movies.get(i - 1), movies.get(i)) > 0) {
                    return false;
                }
            }
        }

        return true;
    }

    private void checkContainsString(SearchResult<Movie> res, String str)
    {
        Assert.assertFalse(res.getResults().isEmpty());
        for (Movie m : res.getResults()) {
            Assert.assertTrue(m.getName().contains(str) || m.getDirector().contains(str) || m.getCast().stream().anyMatch(c -> c.contains(str)));
        }
    }

    @Test
    public void testGroup() throws IOException
    {
        List<Movie> movies = createMovies();
        indexer.index(movies);

        List<Group> groups = indexer.groupBy(new FilterForm().buildQuery(), IndexField.GENRE);
        Assert.assertFalse(groups.isEmpty());
        for (Group g : groups) {
            Assert.assertEquals(movies.stream().filter(m -> m.getGenres().contains(Genre.valueOf(g.getName().toUpperCase()))).count(),
                    g.getValue().longValue());
        }

        groups = indexer.groupBy(new FilterForm().buildQuery(), IndexField.DIRECTOR);
        Assert.assertFalse(groups.isEmpty());
        Assert.assertTrue(groups.stream().allMatch(g -> g.getValue() == 3));

        groups = indexer.groupBy(new FilterForm().buildQuery(), IndexField.CAST);
        Assert.assertFalse(groups.isEmpty());
        Assert.assertTrue(groups.stream().allMatch(g -> g.getValue() == 3));

        groups = indexer.groupBy(new FilterForm().buildQuery(), IndexField.YEAR);
        Assert.assertFalse(groups.isEmpty());
        Assert.assertTrue(groups.stream().allMatch(g -> g.getValue() == 3));

        groups = indexer.groupBy(new FilterForm().buildQuery(), IndexField.RATING);
        Assert.assertFalse(groups.isEmpty());
        Assert.assertEquals(3, groups.stream().filter(g -> g.getName().equals("7")).findFirst().get().getValue().intValue());
        Assert.assertEquals(3, groups.stream().filter(g -> g.getName().equals("8")).findFirst().get().getValue().intValue());
        Assert.assertEquals(3, groups.stream().filter(g -> g.getName().equals("9")).findFirst().get().getValue().intValue());
    }

    private List<Movie> createMovies()
    {
        List<String> cast1 = Arrays.asList("John Travolta", "Samuel L Jackson", "Uma Thurman");
        List<String> cast2 = Arrays.asList("Bruce Willis", "Will Smith", "Daniel Radckliff");

        List<Movie> movies = new ArrayList<>();

        Movie tarantinoMovie = new Movie();
        tarantinoMovie.setName("Tarantino Movie ");
        tarantinoMovie.setDirector("Quentin Tarantino");
        tarantinoMovie.setGenres(Arrays.asList(Genre.ACTION, Genre.COMEDY));
        tarantinoMovie.setRating(7.0F);
        LocalDate initDate = LocalDate.of(1991, 6, 1);

        makeMovieCopies(cast1, cast2, movies, tarantinoMovie, initDate);

        Movie rodrigesMovie = new Movie();
        rodrigesMovie.setName("Rodriges Movie ");
        rodrigesMovie.setDirector("Robert Rodriges");
        rodrigesMovie.setGenres(Collections.singletonList(Genre.HORROR));
        rodrigesMovie.setRating(7.0F);
        rodrigesMovie.setReleaseDate(Date.from(initDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        makeMovieCopies(cast1, cast2, movies, rodrigesMovie, initDate);

        Movie spilbergMovie = new Movie();
        spilbergMovie.setName("Spielberg Movie ");
        spilbergMovie.setDirector("Stievin Spielberg");
        spilbergMovie.setGenres(Collections.singletonList(Genre.COMEDY));
        spilbergMovie.setRating(7.0F);
        spilbergMovie.setReleaseDate(Date.from(initDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        makeMovieCopies(cast1, cast2, movies, spilbergMovie, initDate);
        return movies;
    }

    private void makeMovieCopies(List<String> cast1, List<String> cast2, List<Movie> movies, Movie origin, LocalDate initDate)
    {
        for (int i = 0; i < 3; i++) {
            Movie m = new Movie(origin);
            m.setReleaseDate(Date.from(initDate.plusYears(i).atStartOfDay(ZoneId.systemDefault()).toInstant()));
            m.setRating(origin.getRating() + i);
            m.setCast(Arrays.asList(cast1.get(i), cast2.get(i)));
            m.setName(origin.getName() + i);
            movies.add(m);
        }
    }

    @After
    public void tearDown() {
        File testIndexDir = new File("./test-index.luc");
        FileUtils.deleteQuietly(testIndexDir);
    }
}