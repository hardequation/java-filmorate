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
        film = new Film();
        film.setName("Name");
        film.setDescription("Login");
        film.setDuration(150L);
        film.setMpaRatingId(2);
        film.setReleaseDate(LocalDate.of(1980, 10, 1));
    }

    @AfterEach
    void finish() {
        storage.removeAll();
    }

    @Test
    void testAddFilm() {
        assertTrue(storage.findAll().isEmpty());

        Film addedFilm = storage.add(film);

        assertEquals(1, storage.findAll().size());
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

        assertTrue(storage.contains(addedFilm.getId()));
        assertFalse(storage.contains(addedFilm.getId() + 1));
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

        Optional<Film> filmOptional = storage.findById(addedFilm.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f).hasFieldOrPropertyWithValue("id", addedFilm.getId())
                );
    }

    @Test
    void testRemoveAll() {
        storage.add(film);
        assertEquals(1, storage.findAll().size());

        storage.removeAll();

        assertTrue(storage.findAll().isEmpty());
    }
}
