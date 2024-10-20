package ru.yandex.practicum.filmorate.controller.integration;

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
import ru.yandex.practicum.filmorate.dal.impl.DbUserStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@Import(DbFilmStorage.class)
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DbFilmStorageIntegrationTest {

    @Autowired
    private final JdbcTemplate template;

    private DbFilmStorage storage;

    private Film film = null;

    @BeforeEach
    void setUp() {
        storage = new DbFilmStorage(template);
        film = Film.builder()
                .name("Name")
                .description("Login")
                .duration(150L)
                .mpaId(2)
                .releaseDate(LocalDate.of(1980, 10, 1))
                .genres(new ArrayList<>())
                .build();
    }

    @AfterEach
    void finish() {
        storage.removeAll();
    }

    @Test
    void testAddFilm() {
        assertTrue(storage.findAllFilms().isEmpty());

        Film addedFilm = storage.add(film);

        assertEquals(1, storage.findAllFilms().size());
        assertEquals(film.getName(), addedFilm.getName());
    }

    @Test
    void testUpdateFilm() {
        storage.add(film);

        String newName = "NewName";
        film.setName(newName);
        storage.update(film);

        assertEquals(newName, film.getName());
    }

    @Test
    void testContains() {
        Film addedFilm = storage.add(film);

        assertTrue(storage.containsFilm(addedFilm.getId()));
        assertFalse(storage.containsFilm(addedFilm.getId() + 1));
    }

    @Test
    void testLikes() {
        Film addedFilm = storage.add(film);
        DbUserStorage userStorage = new DbUserStorage(template);

        User user = new User();
        user.setName("Name");
        user.setLogin("Login");
        user.setEmail("a@abc.com");
        user.setBirthday(LocalDate.of(1990, 12, 14));
        User addedUser = userStorage.add(user);
        storage.addLike(addedFilm.getId(), addedUser.getId());

        List<Integer> likes = storage.getLikesByFilmId(addedFilm.getId());

        assertEquals(1, likes.size());
        assertEquals(addedUser.getId(), likes.getFirst());
    }

    @Test
    void testFindFilmById() {
        Film addedFilm = storage.add(film);

        Optional<Film> filmOptional = storage.findFilmById(addedFilm.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f).hasFieldOrPropertyWithValue("id", addedFilm.getId())
                );
    }

    @Test
    void testRemoveAll() {
        storage.add(film);
        assertEquals(1, storage.findAllFilms().size());

        storage.removeAll();

        assertTrue(storage.findAllFilms().isEmpty());
    }
}
