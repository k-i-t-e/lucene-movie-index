package org.kite.movieindex.controller;

import org.kite.movieindex.entity.Movie;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("search")
public class SearchController {
    @RequestMapping(method = RequestMethod.GET)
    public List<Movie> getMovies() {
        return Collections.singletonList(new Movie());
    }

    @RequestMapping(method = RequestMethod.POST)
    public List<Movie> searchMovies() {
        return Collections.singletonList(new Movie());
    }
}
