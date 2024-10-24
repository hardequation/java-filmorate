package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.impl.DbFilmStorage;
import ru.yandex.practicum.filmorate.dal.impl.DbGenreStorage;
import ru.yandex.practicum.filmorate.dal.impl.DbRatingStorage;
import ru.yandex.practicum.filmorate.dal.impl.DbUserStorage;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.RatingRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@Import({DbFilmStorage.class, DbGenreStorage.class, DbRatingStorage.class,
        FilmRowMapper.class, GenreRowMapper.class, RatingRowMapper.class})
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DbFilmStorageIntegrationTest {

    @Autowired
    private final JdbcTemplate template;

    private DbFilmStorage filmStorage;

    private Film film = null;

    @BeforeEach
    void setUp() {
        FilmRowMapper filmRowMapper = new FilmRowMapper();
        filmStorage = new DbFilmStorage(template, filmRowMapper);

        film = Film.builder()
                .name("Name")
                .description("Login")
                .duration(150L)
                .mpa(MpaRating.builder().id(2).build())
                .releaseDate(LocalDate.of(1980, 10, 1))
                .genres(new LinkedHashSet<>())
                .build();
    }

    @AfterEach
    void finish() {
        filmStorage.removeAll();
    }

    @Test
    void testAddFilm() {
        assertTrue(filmStorage.findAllFilms().isEmpty());

        Film addedFilm = filmStorage.add(film);

        assertEquals(1, filmStorage.findAllFilms().size());
        assertEquals(film.getName(), addedFilm.getName());
    }

    @Test
    void testUpdateFilm() {
        filmStorage.add(film);

        String newName = "NewName";
        film.setName(newName);
        filmStorage.update(film);

        assertEquals(newName, film.getName());
    }

    @Test
    void testContains() {
        Film addedFilm = filmStorage.add(film);

        assertTrue(filmStorage.containsFilm(addedFilm.getId()));
        assertFalse(filmStorage.containsFilm(addedFilm.getId() + 1));
    }

    @Test
    void testLikes() {
        UserRowMapper userRowMapper = new UserRowMapper();
        Film addedFilm = filmStorage.add(film);
        DbUserStorage userStorage = new DbUserStorage(template, userRowMapper);

        User user = User.builder()
                .name("Name")
                .login("Login")
                .email("a@abc.com")
                .birthday(LocalDate.of(1990, 12, 14))
                .build();
        User addedUser = userStorage.add(user);
        filmStorage.addLike(addedFilm.getId(), addedUser.getId());

        List<Integer> likes = filmStorage.getLikesByFilmId(addedFilm.getId());

        assertEquals(1, likes.size());
        assertEquals(addedUser.getId(), likes.getFirst());
    }

    @Test
    void testFindFilmById() {
        Film addedFilm = filmStorage.add(film);

        Optional<Film> filmOptional = filmStorage.findFilmById(addedFilm.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f).hasFieldOrPropertyWithValue("id", addedFilm.getId())
                );
    }

    @Test
    void testRemoveAll() {
        filmStorage.add(film);
        assertEquals(1, filmStorage.findAllFilms().size());

        filmStorage.removeAll();

        assertTrue(filmStorage.findAllFilms().isEmpty());
    }
}
