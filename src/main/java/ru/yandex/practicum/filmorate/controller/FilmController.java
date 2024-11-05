package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.create.CreateFilmDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.yandex.practicum.filmorate.model.FilmSortParam.FILMS_BY_RELEASE_DATE;
import static ru.yandex.practicum.filmorate.model.FilmSortParam.POPULAR_FILMS_BY_LIKES;

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
        for (Film film : films) {
            film.setGenres(service.findGenresForFilm(film.getId()));
            film.setDirectors(service.findDirectorsForFilm(film.getId()));
        }
        return films.stream().map(filmMapper::map).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilmDto create(@Valid @RequestBody CreateFilmDto filmDto) {
        Film toCreate = filmMapper.map(filmDto);
        Film createdFilm = service.create(toCreate);
        service.updateGenresForFilm(createdFilm);
        service.updateDirectorsForFilm(createdFilm);
        createdFilm.setGenres(toCreate.getGenres());
        createdFilm.setDirectors(toCreate.getDirectors());
        return filmMapper.map(createdFilm);
    }

    @DeleteMapping("/{filmId}")
    public void removeFilm(@PathVariable Integer filmId) {
        service.removeFilm(filmId);
    }

    @PutMapping
    public FilmDto updateFilm(@Valid @RequestBody FilmDto filmDto) {
        Film film = filmMapper.map(filmDto);
        Film updatedFilm = service.updateFilm(film);
        service.updateGenresForFilm(updatedFilm);
        service.updateDirectorsForFilm(updatedFilm);
        updatedFilm.setGenres(film.getGenres());
        updatedFilm.setDirectors(film.getDirectors());
        return filmMapper.map(updatedFilm);
    }

    @GetMapping("/{id}")
    public FilmDto getFilm(@PathVariable int id) {
        Film film = service.findFilmById(id);
        film.setGenres(service.findGenresForFilm(id));
        film.setDirectors(service.findDirectorsForFilm(id));
        return filmMapper.map(film);
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
    public List<FilmDto> getPopularFilms(@RequestParam(defaultValue = "10") @Positive Integer count,
                                         @Positive @RequestParam(required = false) Integer genreId,
                                         @Positive @RequestParam(required = false) Integer year) {
        List<Film> films;
        if (genreId == null && year == null) {
            films = service.getMostPopularFilms(count);
        } else if (genreId != null && year == null) {
            films = service.getPopularFilmsSortedByGenre(count, genreId);
        } else if (genreId != null) {
            films = service.getPopularFilmsSortedByGenreAndYear(count, genreId, year);
        } else {
            films = service.getPopularFilmsSortedByYear(count, year);
        }
        for (Film film : films) {
            film.setGenres(service.findGenresForFilm(film.getId()));
            film.setDirectors(service.findDirectorsForFilm(film.getId()));
        }
        return films.stream()
                .map(filmMapper::map)
                .toList();
    }

    @GetMapping("/director/{directorId}")
    public ResponseEntity<Object> getFilmsByDirector(@PathVariable Integer directorId,
                                                     @RequestParam(name = "sortBy", required = false) String sortBy) {
        try {
            List<Film> films;
            switch (sortBy.toLowerCase()) {
                case "year":
                    films = service.getFilmsByDirectorSorted(directorId, FILMS_BY_RELEASE_DATE);
                    break;
                case "likes":
                    films = service.getFilmsByDirectorSorted(directorId, POPULAR_FILMS_BY_LIKES);
                    break;
                default:
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Invalid sortBy parameter: '" + sortBy +
                            "'. Allowed values - year, likes");
                    return ResponseEntity.badRequest().body(errorResponse);
            }
            for (Film film : films) {
                film.setGenres(service.findGenresForFilm(film.getId()));
                film.setDirectors(service.findDirectorsForFilm(film.getId()));
            }
            if (films.isEmpty()) {
                return ResponseEntity.notFound().build();
            } else {
                return ResponseEntity.ok(films.stream()
                        .map(filmMapper::map)
                        .toList());
            }
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body("Internal Server Error");
        }
    }

    @GetMapping("/common")
    public ResponseEntity<Object> getCommonFilms(@RequestParam("userId") int userId,
                                                 @RequestParam("friendId") int friendId) {
        try {
            List<Film> films = service.getCommonFilms(userId, friendId);

            for (Film film : films) {
                film.setGenres(service.findGenresForFilm(film.getId()));
                film.setDirectors(service.findDirectorsForFilm(film.getId()));
            }

            return ResponseEntity.ok(films.stream()
                    .map(filmMapper::map)
                    .toList());
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body("Internal Server Error");
        }
    }

    @GetMapping("/search")
    public List<Film> searchFilms(@RequestParam() String query, @RequestParam() List<String> by) {
        return service.searchFilms(query, by);
    }
}