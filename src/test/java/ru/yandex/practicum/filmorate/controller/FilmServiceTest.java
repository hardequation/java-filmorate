package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.impl.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.impl.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilmServiceTest {

    private Validator validator;
    private FilmService service;

    private FilmStorage filmStorage;

    private UserStorage userStorage;
    
    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
        filmStorage = new InMemoryFilmStorage();
        service = new FilmService(filmStorage, userStorage);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testCreateFilmSuccess() {
        String name = "Nick Name";
        String description = "Some description";
        LocalDate releaseDate = LocalDate.of(2015, 8, 20);
        Long duration = 100L;
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(releaseDate);
        film.setDuration(duration);

        Film createdFilm = service.create(film);

        assertNotNull(createdFilm.getId(), "Film ID should be generated");
        assertEquals(name, createdFilm.getName());
        assertEquals(description, createdFilm.getDescription());
        assertEquals(releaseDate, createdFilm.getReleaseDate());
        assertEquals(duration, createdFilm.getDuration());
    }

    @Test
    void testCreateFilmWithEmptyName() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Some description");
        film.setReleaseDate(LocalDate.of(2017, 8, 20));
        film.setDuration(120L);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());

        ConstraintViolation<Film> exception = violations.stream().findFirst().get();
        assertEquals("Film name can't be blank", exception.getMessage());
    }

    @Test
    void testCreateFilmWithTooLongDescription() {
        Film film = new Film();
        film.setName("dolore ullamco");
        film.setDescription(("aaaaaaaaaa").repeat(21));
        film.setReleaseDate(LocalDate.of(1990, 8, 20));
        film.setDuration(100L);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());

        ConstraintViolation<Film> exception = violations.stream().findFirst().get();
        assertEquals("Description is too long", exception.getMessage());
    }

    @Test
    void testCreateFilmEarlierThanFirstFilm() {
        Film film = new Film();
        film.setName("dolore ullamco");
        film.setDescription("Some description");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(100L);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());

        ConstraintViolation<Film> exception = violations.stream().findFirst().get();
        assertEquals("Release date should after 1st film birthday", exception.getMessage());
    }

    @Test
    void testCreateFilmWithZeroDuration() {
        Film film = new Film();
        film.setName("dolore ullamco");
        film.setDescription("Some description");
        film.setReleaseDate(LocalDate.of(2023, 12, 27));
        film.setDuration(0L);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());

        ConstraintViolation<Film> exception = violations.stream().findFirst().get();
        assertEquals("Film duration should be positive", exception.getMessage());
    }


    @Test
    void testUpdateFilmSuccess() {
        Film film = new Film();
        film.setName("dolore ullamco");
        film.setDescription("Some description");
        film.setReleaseDate(LocalDate.of(2023, 12, 27));
        film.setDuration(150L);
        service.create(film);

        Film updatedFilm = new Film();
        updatedFilm.setId(film.getId());
        updatedFilm.setName("new dolore ullamco");
        updatedFilm.setDescription("New Some description");
        updatedFilm.setReleaseDate(LocalDate.of(2022, 12, 27));
        updatedFilm.setDuration(170L);

        Film result = service.updateFilm(updatedFilm);

        assertEquals("new dolore ullamco", result.getName());
        assertEquals("New Some description", result.getDescription());
        assertEquals(LocalDate.of(2022, 12, 27), result.getReleaseDate());
        assertEquals(170L, result.getDuration());
    }

    @Test
    void testUpdateFilmNotFound() {
        Film film = new Film();
        film.setId(999L); // Non-existent ID
        film.setName("dolore ullamco");
        film.setDescription("Some description");
        film.setReleaseDate(LocalDate.of(2023, 12, 27));
        film.setDuration(150L);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> service.updateFilm(film));

        assertEquals("Film with id = 999 isn't found", exception.getMessage());
    }

    @Test
    void testFindAllFilms() {
        Film film1 = new Film();
        film1.setName("Test Name 1");
        film1.setDescription("Description1");
        film1.setReleaseDate(LocalDate.of(1990, 1, 1));
        film1.setDuration(100L);
        service.create(film1);

        Film film2 = new Film();
        film2.setName("Test Name 2");
        film2.setDescription("Description2");
        film2.setReleaseDate(LocalDate.of(1995, 1, 1));
        film2.setDuration(150L);
        service.create(film2);

        Collection<Film> films = service.getFilms();

        assertEquals(2, films.size(), "There should be 2 films in the collection");
        assertTrue(films.contains(film1), "Collection should contain film1");
        assertTrue(films.contains(film2), "Collection should contain film2");
    }
}