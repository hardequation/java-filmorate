package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.FilmStorage;
import ru.yandex.practicum.filmorate.dal.GenreStorage;
import ru.yandex.practicum.filmorate.dal.RatingStorage;
import ru.yandex.practicum.filmorate.dal.UserStorage;
import ru.yandex.practicum.filmorate.dal.impl.DbFilmStorage;
import ru.yandex.practicum.filmorate.dal.impl.DbGenreStorage;
import ru.yandex.practicum.filmorate.dal.impl.DbRatingStorage;
import ru.yandex.practicum.filmorate.dal.impl.DbUserStorage;
import ru.yandex.practicum.filmorate.dto.CreateFilmDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.yandex.practicum.filmorate.utils.ErrorMessages.FILM_NOT_FOUND;

class FilmServiceTest {

    private Validator validator;
    private FilmService service;

    private FilmStorage filmStorage;

    private UserStorage userStorage;

    private RatingStorage ratingStorage;

    private GenreStorage genreStorage;

    @Mock
    private JdbcTemplate template;

    @BeforeEach
    void setUp() {
        userStorage = new DbUserStorage(template);
        filmStorage = new DbFilmStorage(template, genreStorage);
        ratingStorage = new DbRatingStorage(template);
        genreStorage = new DbGenreStorage(template);
        service = new FilmService(
                filmStorage,
                userStorage,
                ratingStorage,
                genreStorage);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testCreateFilmSuccess() {
        String name = "Nick Name";
        String description = "Some description";
        LocalDate releaseDate = LocalDate.of(2015, 8, 20);
        Long duration = 100L;
        Film film = Film.builder()
                .name(name)
                .description(description)
                .releaseDate(releaseDate)
                .duration(duration)
                .build();

        Film createdFilm = service.create(film);

        assertEquals(name, createdFilm.getName());
        assertEquals(description, createdFilm.getDescription());
        assertEquals(releaseDate, createdFilm.getReleaseDate());
        assertEquals(duration, createdFilm.getDuration());
    }

    @Test
    void testCreateFilmWithEmptyName() {
        CreateFilmDto film = new CreateFilmDto();
        film.setName("");
        film.setDescription("Some description");
        film.setReleaseDate(LocalDate.of(2017, 8, 20));
        film.setDuration(120L);

        Set<ConstraintViolation<CreateFilmDto>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());

        ConstraintViolation<CreateFilmDto> exception = violations.stream().findFirst().get();
        assertEquals("Film name can't be blank", exception.getMessage());
    }

    @Test
    void testCreateFilmWithTooLongDescription() {
        CreateFilmDto film = new CreateFilmDto();
        film.setName("dolore ullamco");
        film.setDescription(("aaaaaaaaaa").repeat(21));
        film.setReleaseDate(LocalDate.of(1990, 8, 20));
        film.setDuration(100L);

        Set<ConstraintViolation<CreateFilmDto>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());

        ConstraintViolation<CreateFilmDto> exception = violations.stream().findFirst().get();
        assertEquals("Description is too long", exception.getMessage());
    }

    @Test
    void testCreateFilmEarlierThanFirstFilm() {
        CreateFilmDto film = new CreateFilmDto();
        film.setName("dolore ullamco");
        film.setDescription("Some description");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(100L);

        Set<ConstraintViolation<CreateFilmDto>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());

        ConstraintViolation<CreateFilmDto> exception = violations.stream().findFirst().get();
        assertEquals("Release date should after 1st film birthday", exception.getMessage());
    }

    @Test
    void testCreateFilmWithZeroDuration() {
        CreateFilmDto film = new CreateFilmDto();
        film.setName("dolore ullamco");
        film.setDescription("Some description");
        film.setReleaseDate(LocalDate.of(2023, 12, 27));
        film.setDuration(0L);

        Set<ConstraintViolation<CreateFilmDto>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());

        ConstraintViolation<CreateFilmDto> exception = violations.stream().findFirst().get();
        assertEquals("Film duration should be positive", exception.getMessage());
    }


    @Test
    void testUpdateFilmSuccess() {
        Film film = Film.builder()
                .name("dolore ullamco")
                .description("Some description")
                .releaseDate(LocalDate.of(2023, 12, 27))
                .duration(150L)
                .build();
        service.create(film);

        Film updatedFilm = Film.builder()
                .id(film.getId())
                .name("new dolore ullamco")
                .description("New Some description")
                .releaseDate(LocalDate.of(2022, 12, 27))
                .duration(170L)
                .build();

        Film result = service.updateFilm(updatedFilm);

        assertEquals("new dolore ullamco", result.getName());
        assertEquals("New Some description", result.getDescription());
        assertEquals(LocalDate.of(2022, 12, 27), result.getReleaseDate());
        assertEquals(170L, result.getDuration());
    }

    @Test
    void testUpdateFilmNotFound() {
        Film film = Film.builder()
                .id(999)
                .name("dolore ullamco")
                .description("Some description")
                .releaseDate(LocalDate.of(2023, 12, 27))
                .duration(150L)
                .build();

        NotFoundException exception = assertThrows(NotFoundException.class, () -> service.updateFilm(film));

        assertEquals(FILM_NOT_FOUND + 999, exception.getMessage());
    }

    @Test
    void testFindAllFilms() {
        Film film1 = Film.builder()
                .name("Test Name 1")
                .description("Description1")
                .releaseDate(LocalDate.of(1990, 1, 1))
                .duration(100L)
                .build();
        service.create(film1);

        Film film2 = Film.builder()
                .name("Test Name 2")
                .description("Description2")
                .releaseDate(LocalDate.of(1995, 1, 1))
                .duration(150L)
                .build();
        service.create(film2);

        Collection<Film> films = service.getFilms();

        assertEquals(2, films.size(), "There should be 2 films in the collection");
        assertTrue(films.contains(film1), "Collection should contain film1");
        assertTrue(films.contains(film2), "Collection should contain film2");
    }
}