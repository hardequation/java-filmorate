package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.controller.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.controller.mappers.MpaRatingMapper;
import ru.yandex.practicum.filmorate.dto.CreateFilmDto;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.MpaRatingDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService service;

    private final FilmMapper filmMapper;

    private final MpaRatingMapper ratingMapper;

    @GetMapping
    public List<FilmDto> findAll() {
        List<Film> films = service.getFilms();
        return films.stream().map(film -> filmMapper.map(film,
                ratingMapper.map(service.findMpaRatingById(film.getMpa().getId())))).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilmDto create(@Valid @RequestBody CreateFilmDto filmDto) {
        Film toCreate = filmMapper.map(filmDto);
        Film createdFilm = service.create(toCreate);

        MpaRatingDto ratingDto = ratingMapper.map(service.findMpaRatingById(createdFilm.getMpa().getId()));
        return filmMapper.map(createdFilm, ratingDto);
    }

    @PutMapping
    public FilmDto updateFilm(@Valid @RequestBody FilmDto filmDto) {
        Film film = filmMapper.map(filmDto);
        Film updatedFilm = service.updateFilm(film);
        MpaRatingDto ratingDto = ratingMapper.map(service.findMpaRatingById(updatedFilm.getMpa().getId()));
        return filmMapper.map(updatedFilm, ratingDto);
    }

    @GetMapping("/{id}")
    public FilmDto getFilm(@PathVariable int id) {
        Film film = service.findFilmById(id);
        MpaRatingDto ratingDto = ratingMapper.map(service.findMpaRatingById(film.getMpa().getId()));
        return filmMapper.map(film, ratingDto);
    }

    @PutMapping("/{id}/like/{userId}")
    public void likeFilm(@PathVariable Integer id, @PathVariable Integer userId) {
        service.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void unlikeFilm(@PathVariable Integer id, @PathVariable Integer userId) {
        service.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<FilmDto> getPopularFilms(@RequestParam(defaultValue = "10") @Positive int count) {
        List<Film> films = service.getMostPopularFilms(count);
        return films.stream().map(film -> filmMapper.map(film,
                ratingMapper.map(service.findMpaRatingById(film.getMpa().getId())))).toList();
    }
}
