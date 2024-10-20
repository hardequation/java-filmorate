package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Slf4j
@RestController
public class UtilController {

    private final FilmService service;

    @Autowired
    public UtilController(FilmService service) {
        this.service = service;
    }

    @GetMapping("/mpa")
    public List<MpaRating> findAllMpa() {
        return service.getRatings();
    }

    @GetMapping("/mpa/{id}")
    public ResponseEntity<MpaRating> findMpa(@PathVariable int id) {
        return new ResponseEntity<>(service.findMpaRatingById(id), HttpStatus.OK);
    }

    @GetMapping("/genres")
    public List<Genre> findAllGenres() {
        return service.getGenres();
    }

    @GetMapping("/genres/{id}")
    public ResponseEntity<Genre> findGenre(@PathVariable int id) {
        return new ResponseEntity<>(service.findGenreById(id), HttpStatus.OK);
    }
}
