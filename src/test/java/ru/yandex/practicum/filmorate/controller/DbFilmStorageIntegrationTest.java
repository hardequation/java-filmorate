package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.impl.*;
import ru.yandex.practicum.filmorate.dal.mappers.*;
import ru.yandex.practicum.filmorate.model.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@JdbcTest
@Import({DbFilmStorage.class, DbGenreStorage.class, DbRatingStorage.class, DbDirectorStorage.class,
        FilmRowMapper.class, GenreRowMapper.class, RatingRowMapper.class, DirectorRowMapper.class})
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DbFilmStorageIntegrationTest {

    @Autowired
    private final JdbcTemplate template;

    private DbFilmStorage filmStorage;

    private DbUserStorage userStorage;

    private DbDirectorStorage directorStorage;

    private Film film = null;

    @Autowired
    private DirectorRowMapper directorRowMapper;

    @BeforeEach
    void setUp() {
        FilmRowMapper filmRowMapper = new FilmRowMapper();
        UserRowMapper userRowMapper = new UserRowMapper();
        filmStorage = new DbFilmStorage(template, filmRowMapper);
        userStorage = new DbUserStorage(template, userRowMapper);
        directorStorage = new DbDirectorStorage(template, directorRowMapper);

        film = Film.builder()
                .name("Name")
                .description("Login")
                .duration(150L)
                .mpa(MpaRating.builder().id(2).build())
                .releaseDate(LocalDate.of(1980, 10, 1))
                .genres(new LinkedHashSet<>())
                .directors(new LinkedHashSet<>())
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
    void testRemoveFilm() {
        Film addedFilm = filmStorage.add(film);
        int id = addedFilm.getId();
        assertFalse(filmStorage.findFilmById(id).isEmpty());

        filmStorage.removeFilm(id);

        assertEquals(Optional.empty(), filmStorage.findFilmById(id));
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
        Film addedFilm = filmStorage.add(film);

        User user = User.builder()
                .name("Name")
                .login("Login")
                .email("a@abc.com")
                .birthday(LocalDate.of(1990, 12, 14))
                .build();
        User addedUser = userStorage.create(user);
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

    @Test
    void testPopularFilms() {
        Film film1 = filmStorage.add(Film.builder()
                .name("Name1")
                .description("Login1")
                .duration(150L)
                .mpa(MpaRating.builder().id(2).name("PG").build())
                .releaseDate(LocalDate.of(1980, 10, 1))
                .genres(new LinkedHashSet<>())
                .directors(new LinkedHashSet<>())
                .build());
        Film film2 = filmStorage.add(Film.builder()
                .name("Name2")
                .description("Login2")
                .duration(150L)
                .mpa(MpaRating.builder().id(2).name("PG").build())
                .releaseDate(LocalDate.of(1980, 10, 1))
                .genres(new LinkedHashSet<>())
                .directors(new LinkedHashSet<>())
                .build());
        Film film3 = filmStorage.add(Film.builder()
                .name("Name3")
                .description("Login3")
                .duration(150L)
                .mpa(MpaRating.builder().id(2).name("PG").build())
                .releaseDate(LocalDate.of(1980, 10, 1))
                .genres(new LinkedHashSet<>())
                .directors(new LinkedHashSet<>())
                .build());
        User user1 = userStorage.create(
                User.builder()
                        .name("Name1")
                        .login("Loin1")
                        .email("a1@g.com")
                        .birthday(LocalDate.of(1980, 10, 30))
                        .build());
        User user2 = userStorage.create(
                User.builder()
                        .name("Name2")
                        .login("Loin2")
                        .email("a2@g.com")
                        .birthday(LocalDate.of(1980, 10, 29))
                        .build());

        User user3 = userStorage.create(
                User.builder()
                        .name("Name3")
                        .login("Login3")
                        .email("a3@g.com")
                        .birthday(LocalDate.of(1980, 10, 27))
                        .build());

        filmStorage.addLike(film1.getId(), user1.getId());
        filmStorage.addLike(film2.getId(), user1.getId());
        filmStorage.addLike(film2.getId(), user2.getId());
        filmStorage.addLike(film3.getId(), user1.getId());
        filmStorage.addLike(film3.getId(), user2.getId());
        filmStorage.addLike(film3.getId(), user3.getId());

        List<Film> popularFilms = filmStorage.getMostPopularFilms(2);

        assertEquals(2, popularFilms.size());
        assertEquals(film3, popularFilms.get(0));
        assertEquals(film2, popularFilms.get(1));

        filmStorage.removeFilm(film3.getId());

        List<Film> popularFilmsAfterRemove = filmStorage.getMostPopularFilms(1);
        assertEquals(1, popularFilmsAfterRemove.size());
        assertEquals(film2, popularFilmsAfterRemove.get(0));
    }

    @Test
    @DisplayName("Film without likes can be popular also")
    void testPopularFilmsAfterRemoveWithoutLikes() {
        filmStorage.add(Film.builder()
                .name("Name1")
                .description("Login1")
                .duration(150L)
                .mpa(MpaRating.builder().id(2).name("PG").build())
                .releaseDate(LocalDate.of(1980, 10, 1))
                .genres(new LinkedHashSet<>())
                .directors(new LinkedHashSet<>())
                .build());

        List<Film> popularFilms = filmStorage.getMostPopularFilms(1000);

        assertEquals(1, popularFilms.size());
    }

    @Test
    @DisplayName("Получение отсортированных списков фильмов режиссёра по годам/лайкам")
    void testSortedFilmsByDirector() {
        Director director = directorStorage.createDirector(Director.builder()
                .name("Director")
                .build());
        Film film1 = filmStorage.add(Film.builder()
                .name("Name1")
                .description("Login1")
                .duration(150L)
                .mpa(MpaRating.builder().id(2).name("PG").build())
                .releaseDate(LocalDate.of(1980, 10, 1))
                .genres(new LinkedHashSet<>())
                .directors(new LinkedHashSet<>(Collections.singleton(director)))
                .build());
        Film film2 = filmStorage.add(Film.builder()
                .name("Name2")
                .description("Login2")
                .duration(150L)
                .mpa(MpaRating.builder().id(2).name("PG").build())
                .releaseDate(LocalDate.of(1981, 10, 1))
                .genres(new LinkedHashSet<>())
                .directors(new LinkedHashSet<>(Collections.singleton(director)))
                .build());
        Film film3 = filmStorage.add(Film.builder()
                .name("Name3")
                .description("Login3")
                .duration(150L)
                .mpa(MpaRating.builder().id(2).name("PG").build())
                .releaseDate(LocalDate.of(1982, 10, 1))
                .genres(new LinkedHashSet<>())
                .directors(new LinkedHashSet<>(Collections.singleton(director)))
                .build());
        User user1 = userStorage.create(
                User.builder()
                        .name("Name1")
                        .login("Loin1")
                        .email("a1@g.com")
                        .birthday(LocalDate.of(1980, 10, 30))
                        .build());
        User user2 = userStorage.create(
                User.builder()
                        .name("Name2")
                        .login("Loin2")
                        .email("a2@g.com")
                        .birthday(LocalDate.of(1980, 10, 29))
                        .build());

        User user3 = userStorage.create(
                User.builder()
                        .name("Name3")
                        .login("Login3")
                        .email("a3@g.com")
                        .birthday(LocalDate.of(1980, 10, 27))
                        .build());

        directorStorage.addDirectorOfFilm(film1);
        directorStorage.addDirectorOfFilm(film2);
        directorStorage.addDirectorOfFilm(film3);

        filmStorage.addLike(film1.getId(), user1.getId());
        filmStorage.addLike(film2.getId(), user1.getId());
        filmStorage.addLike(film2.getId(), user2.getId());
        filmStorage.addLike(film3.getId(), user1.getId());
        filmStorage.addLike(film3.getId(), user2.getId());
        filmStorage.addLike(film3.getId(), user3.getId());

        List<Film> filmsByDirectorSortedByYear = filmStorage.getFilmsByDirectorSorted(director.getId(), FilmSortParam.FILMS_BY_RELEASE_DATE);
        List<Film> filmsByDirectorSortedByLikes = filmStorage.getFilmsByDirectorSorted(director.getId(), FilmSortParam.POPULAR_FILMS_BY_LIKES);

        assertEquals(film1.getId(), filmsByDirectorSortedByYear.getFirst().getId());
        assertEquals(film3.getId(), filmsByDirectorSortedByLikes.getFirst().getId());
    }

    @Test
    @DisplayName("Получение общих фильмов")
    void testCommonFilms() {
        Film film1 = filmStorage.add(Film.builder()
                .name("Name1")
                .description("Login1")
                .duration(150L)
                .mpa(MpaRating.builder().id(2).name("PG").build())
                .releaseDate(LocalDate.of(1980, 10, 1))
                .genres(new LinkedHashSet<>())
                .directors(new LinkedHashSet<>())
                .build());
        Film film2 = filmStorage.add(Film.builder()
                .name("Name2")
                .description("Login2")
                .duration(150L)
                .mpa(MpaRating.builder().id(2).name("PG").build())
                .releaseDate(LocalDate.of(1980, 10, 1))
                .genres(new LinkedHashSet<>())
                .directors(new LinkedHashSet<>())
                .build());
        Film film3 = filmStorage.add(Film.builder()
                .name("Name3")
                .description("Login3")
                .duration(150L)
                .mpa(MpaRating.builder().id(2).name("PG").build())
                .releaseDate(LocalDate.of(1980, 10, 1))
                .genres(new LinkedHashSet<>())
                .directors(new LinkedHashSet<>())
                .build());
        User user1 = userStorage.create(
                User.builder()
                        .name("Name1")
                        .login("Loin1")
                        .email("a1@g.com")
                        .birthday(LocalDate.of(1980, 10, 30))
                        .build());
        User user2 = userStorage.create(
                User.builder()
                        .name("Name2")
                        .login("Loin2")
                        .email("a2@g.com")
                        .birthday(LocalDate.of(1980, 10, 29))
                        .build());
        User user3 = userStorage.create(
                User.builder()
                        .name("Name3")
                        .login("Login3")
                        .email("a3@g.com")
                        .birthday(LocalDate.of(1980, 10, 27))
                        .build());
        User user4 = userStorage.create(
                User.builder()
                        .name("Name4")
                        .login("Login4")
                        .email("a4@g.com")
                        .birthday(LocalDate.of(1980, 10, 27))
                        .build());

        filmStorage.addLike(film1.getId(), user1.getId());
        filmStorage.addLike(film2.getId(), user1.getId());
        filmStorage.addLike(film3.getId(), user1.getId());
        filmStorage.addLike(film2.getId(), user2.getId());
        filmStorage.addLike(film3.getId(), user2.getId());
        filmStorage.addLike(film3.getId(), user3.getId());
        filmStorage.addLike(film1.getId(), user4.getId());

        List<Film> commonFilms = filmStorage.getCommonFilms(user1.getId(), user2.getId());
        List<Film> commonFilms1 = filmStorage.getCommonFilms(user2.getId(), user4.getId());

        assertEquals(film3.getId(), commonFilms.getFirst().getId());
        assertEquals(2, commonFilms.size());
        assertEquals(0, commonFilms1.size());
    }
}
