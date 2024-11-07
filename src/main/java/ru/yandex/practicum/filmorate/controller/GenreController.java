package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.controller.mappers.GenreMapper;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.film.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {

    private final FilmService service;

    private final GenreMapper mapper;

    @GetMapping
    public List<GenreDto> findAllGenres() {
        List<Genre> genres = service.getGenres();
        return genres.stream().map(mapper::map).toList();
    }

    @GetMapping("/{id}")
    public GenreDto findGenreById(@PathVariable int id) {
        Genre genre = service.findGenreById(id);
        return mapper.map(genre);
    }
}
