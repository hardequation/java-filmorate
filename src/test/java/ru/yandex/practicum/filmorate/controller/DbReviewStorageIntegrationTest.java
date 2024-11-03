package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.impl.DbFilmStorage;
import ru.yandex.practicum.filmorate.dal.impl.DbReviewStorage;
import ru.yandex.practicum.filmorate.dal.impl.DbUserStorage;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.ReviewRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@Import({DbReviewStorage.class, ReviewRowMapper.class})
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DbReviewStorageIntegrationTest {

    @Autowired
    private final JdbcTemplate template;

    private DbReviewStorage reviewStorage;

    private DbUserStorage userStorage;

    private DbFilmStorage filmStorage;

    private Film film;

    private User user;


    @BeforeEach
    void setUp() {
        ReviewRowMapper rowMapper = new ReviewRowMapper();
        FilmRowMapper filmRowMapper = new FilmRowMapper();
        UserRowMapper userRowMapper = new UserRowMapper();
        reviewStorage = new DbReviewStorage(template, rowMapper);
        filmStorage = new DbFilmStorage(template, filmRowMapper);
        userStorage = new DbUserStorage(template, userRowMapper);

        film = Film.builder()
                .name("Name")
                .description("Login")
                .duration(150L)
                .mpa(MpaRating.builder().id(2).build())
                .releaseDate(LocalDate.of(1980, 10, 1))
                .genres(new LinkedHashSet<>())
                .build();
        user = User.builder()
                .name("Name")
                .login("Login")
                .email("a@abc.com")
                .birthday(LocalDate.of(1990, 12, 14))
                .build();
    }

    @AfterEach
    void finish() {
        reviewStorage.removeAll();
        filmStorage.removeAll();
        userStorage.removeAll();
    }

    @Test
    void addRightReview() {
        Film createdFilm = filmStorage.add(film);
        User createdUser = userStorage.create(user);

        Review review = Review.builder()
                .filmId(createdFilm.getId())
                .userId(createdUser.getId())
                .isPositive(true)
                .useful(0)
                .build();

        Review savedReview = reviewStorage.add(review);

        assertNotNull(savedReview.getReviewId(), "Saved review should have an ID");
        Optional<Review> fetchedReview = reviewStorage.findById(savedReview.getReviewId());
        assertTrue(fetchedReview.isPresent(), "Review should be found by ID");
        assertEquals(review.getFilmId(), fetchedReview.get().getFilmId());
    }

    @Test
    @DisplayName("Film id does not exist")
    void addWithWrongFilm() {
        Film createdFilm = filmStorage.add(film);
        User createdUser = userStorage.create(user);

        Review review = Review.builder()
                .filmId(createdFilm.getId() + 1)
                .userId(createdUser.getId())
                .isPositive(true)
                .useful(0)
                .build();

        NotFoundException exception = assertThrows(NotFoundException.class, () -> reviewStorage.add(review));
        assertTrue(exception.getMessage().contains("Referential integrity constraint violation: \"FK_FILM_ID4:"));
    }

    @Test
    @DisplayName("User id does not exist")
    void addWithUserFilm() {
        Film createdFilm = filmStorage.add(film);
        User createdUser = userStorage.create(user);

        Review review = Review.builder()
                .filmId(createdFilm.getId())
                .userId(createdUser.getId() + 1)
                .isPositive(true)
                .useful(0)
                .build();

        NotFoundException exception = assertThrows(NotFoundException.class, () -> reviewStorage.add(review));
        assertTrue(exception.getMessage().contains("Referential integrity constraint violation: \"FK_USER_ID2:"));
    }

    @Test
    @DisplayName("Successful update")
    void updateReview() {
        Film createdFilm = filmStorage.add(film);
        User createdUser = userStorage.create(user);
        Review review = Review.builder()
                .filmId(createdFilm.getId())
                .userId(createdUser.getId())
                .isPositive(true)
                .useful(0)
                .content("First content")
                .build();

        Review createdReview = reviewStorage.add(review);

        createdReview.setContent("Updated content");
        createdReview.setPositive(false);

        Review updatedReview = reviewStorage.update(createdReview);
        assertFalse(updatedReview.isPositive());
        assertEquals("Updated content", updatedReview.getContent());
    }

    @Test
    void removeReviewSuccessfully() {
        Film createdFilm = filmStorage.add(film);
        User createdUser = userStorage.create(user);
        Review review = reviewStorage.add(Review.builder()
                .filmId(createdFilm.getId())
                .userId(createdUser.getId())
                .isPositive(false)
                .useful(0)
                .build());
        Review createdReview = reviewStorage.add(review);

        assertEquals(createdReview, reviewStorage.findById(createdReview.getReviewId()).get());

        reviewStorage.remove(createdReview.getReviewId());

        Optional<Review> fetchedReview = reviewStorage.findById(createdReview.getReviewId());
        assertFalse(fetchedReview.isPresent(), "Review should be deleted");
    }

    @Test
    void removeNonExistingReview() {
        Film createdFilm = filmStorage.add(film);
        User createdUser = userStorage.create(user);
        int nonExistingReviewID = 999;
        reviewStorage.add(Review.builder()
                .reviewId(nonExistingReviewID)
                .filmId(createdFilm.getId())
                .userId(createdUser.getId())
                .isPositive(false)
                .useful(0)
                .build());

        reviewStorage.remove(nonExistingReviewID);
    }

    @Test
    void removeAllReviews() {
        Film createdFilm = filmStorage.add(film);
        User createdUser = userStorage.create(user);
        reviewStorage.add(Review.builder()
                .filmId(createdFilm.getId())
                .userId(createdUser.getId())
                .isPositive(true)
                .useful(0)
                .build());
        reviewStorage.add(Review.builder()
                .filmId(createdFilm.getId())
                .userId(createdUser.getId())
                .isPositive(false)
                .useful(0)
                .content("Some content")
                .build());

        assertEquals(2, reviewStorage.findByFilmId(createdFilm.getId(), 10).size());
        reviewStorage.removeAll();

        List<Review> allReviews = reviewStorage.findByFilmId(1, 10);
        assertTrue(allReviews.isEmpty(), "All reviews should be deleted");
    }

    @Test
    void testfindByIdSuccessful() {
        Film createdFilm = filmStorage.add(film);
        User createdUser = userStorage.create(user);
        Review review = reviewStorage.add(Review.builder()
                .filmId(createdFilm.getId())
                .userId(createdUser.getId())
                .isPositive(true)
                .useful(0)
                .build());

        Review createdReview = reviewStorage.add(review);
        Optional<Review> foundReview = reviewStorage.findById(review.getReviewId());

        assertEquals(createdReview, foundReview.get());
    }

    @Test
    void testfindByNonExistingId() {
        Film createdFilm = filmStorage.add(film);
        User createdUser = userStorage.create(user);
        Review review = reviewStorage.add(Review.builder()
                .filmId(createdFilm.getId())
                .userId(createdUser.getId())
                .isPositive(true)
                .useful(0)
                .build());

        reviewStorage.add(review);
        Optional<Review> foundReview = reviewStorage.findById(review.getReviewId() + 1);

        assertTrue(foundReview.isEmpty());
    }

    @Test
    void testFindByFilmId() {
        Film createdFilm = filmStorage.add(film);
        User createdUser = userStorage.create(user);
        Review review1 = reviewStorage.add(Review.builder()
                .filmId(createdFilm.getId())
                .userId(createdUser.getId())
                .isPositive(true)
                .useful(0)
                .build());

        Review review2 = reviewStorage.add(Review.builder()
                .filmId(createdFilm.getId())
                .userId(createdUser.getId())
                .isPositive(false)
                .useful(0)
                .build());

        List<Review> reviews = reviewStorage.findByFilmId(createdFilm.getId(), 10);

        assertEquals(2, reviews.size(), "Should return both reviews for the film");
        assertTrue(reviews.contains(review1), "Review1 should be in the results");
        assertTrue(reviews.contains(review2), "Review2 should be in the results");
    }

    @Test
    void testFindByNonExistingFilmId() {
        List<Review> reviews = reviewStorage.findByFilmId(999, 10);
        assertEquals(0, reviews.size(), "Should return both reviews for the film");
    }

    @Test
    void testAddLikeAndDislikeAdd() {
        Film createdFilm = filmStorage.add(film);
        User createdUser = userStorage.create(user);

        Review review = reviewStorage.add(Review.builder()
                .filmId(createdFilm.getId())
                .userId(createdUser.getId())
                .isPositive(true)
                .useful(0)
                .build());
        reviewStorage.add(review);

        User createdUser1 = userStorage.create(
                User.builder()
                        .name("Name")
                        .login("Login1")
                        .email("a1@abc.com")
                        .birthday(LocalDate.of(1990, 12, 14))
                        .build());
        User createdUser2 = userStorage.create(
                User.builder()
                        .name("Name")
                        .login("Login2")
                        .email("a2@abc.com")
                        .birthday(LocalDate.of(1990, 12, 14))
                        .build());

        reviewStorage.addRating(review.getReviewId(), createdUser1.getId(), true);
        assertEquals(1, reviewStorage.findById(review.getReviewId()).get().getUseful());

        reviewStorage.addRating(review.getReviewId(), createdUser2.getId(), false);
        assertEquals(0, reviewStorage.findById(review.getReviewId()).get().getUseful());
    }

    @Test
    void testAddLikeNonExistingReview() {
        Film createdFilm = filmStorage.add(film);
        User createdUser = userStorage.create(user);
        Review review = reviewStorage.add(Review.builder()
                .filmId(createdFilm.getId())
                .userId(createdUser.getId())
                .isPositive(true)
                .useful(0)
                .build());

        reviewStorage.add(review);

        Exception e1 = assertThrows(ValidationException.class,
                () -> reviewStorage.addRating(review.getReviewId() + 1, createdUser.getId(), true));
        assertTrue(e1.getMessage().contains("Referential integrity constraint violation: \"FK_REVIEW_ID"));
    }

    @Test
    void testAddLikeReviewWithNonExistingUser() {
        Film createdFilm = filmStorage.add(film);
        User createdUser = userStorage.create(user);
        Review review = reviewStorage.add(Review.builder()
                .filmId(createdFilm.getId())
                .userId(createdUser.getId())
                .isPositive(true)
                .useful(0)
                .build());

        reviewStorage.add(review);

        Exception e2 = assertThrows(ValidationException.class,
                () -> reviewStorage.addRating(review.getReviewId(), createdUser.getId() + 1, true));
        assertTrue(e2.getMessage().contains("Referential integrity constraint violation: \"FK_USER_ID3"));
    }

    @Test
    void testAddDislikeNonExistingReview() {
        Film createdFilm = filmStorage.add(film);
        User createdUser = userStorage.create(user);
        Review review = reviewStorage.add(Review.builder()
                .filmId(createdFilm.getId())
                .userId(createdUser.getId())
                .isPositive(true)
                .useful(0)
                .build());

        reviewStorage.add(review);

        Exception e3 = assertThrows(ValidationException.class,
                () -> reviewStorage.addRating(review.getReviewId() + 1, createdUser.getId(), false));
        assertTrue(e3.getMessage().contains("Referential integrity constraint violation: \"FK_REVIEW_ID"));
    }

    @Test
    void testAddDislikeNonExistingUser() {
        Film createdFilm = filmStorage.add(film);
        User createdUser = userStorage.create(user);
        Review review = reviewStorage.add(Review.builder()
                .filmId(createdFilm.getId())
                .userId(createdUser.getId())
                .isPositive(true)
                .useful(0)
                .build());

        reviewStorage.add(review);

        Exception e4 = assertThrows(ValidationException.class,
                () -> reviewStorage.addRating(review.getReviewId(), createdUser.getId() + 1, false));
        assertTrue(e4.getMessage().contains("Referential integrity constraint violation: \"FK_USER_ID3"));
    }

    @Test
    void testRemoveLikeAndDislike() {
        Film createdFilm = filmStorage.add(film);
        User createdUser = userStorage.create(user);

        Review review = reviewStorage.add(Review.builder()
                .filmId(createdFilm.getId())
                .userId(createdUser.getId())
                .isPositive(true)
                .useful(0)
                .build());

        User createdUser1 = userStorage.create(
                User.builder()
                        .name("Name")
                        .login("Login1")
                        .email("a1@abc.com")
                        .birthday(LocalDate.of(1990, 12, 14))
                        .build());

        User createdUser2 = userStorage.create(
                User.builder()
                        .name("Name")
                        .login("Login2")
                        .email("a2@abc.com")
                        .birthday(LocalDate.of(1990, 12, 14))
                        .build());
        reviewStorage.addRating(review.getReviewId(), createdUser1.getId(), true);
        assertEquals(1, reviewStorage.findById(review.getReviewId()).get().getUseful());
        reviewStorage.addRating(review.getReviewId(), createdUser2.getId(), false);
        assertEquals(0, reviewStorage.findById(review.getReviewId()).get().getUseful());

        reviewStorage.removeRating(review.getReviewId(), createdUser1.getId(), true);
        assertEquals(-1, reviewStorage.findById(review.getReviewId()).get().getUseful());
        reviewStorage.removeRating(review.getReviewId(), createdUser2.getId(), false);
        assertEquals(0, reviewStorage.findById(review.getReviewId()).get().getUseful());
    }

    @Test
    void testWrongRemoveLikeAndDislike() {
        Film createdFilm = filmStorage.add(film);
        User createdUser = userStorage.create(user);
        Review review = reviewStorage.add(Review.builder()
                .filmId(createdFilm.getId())
                .userId(createdUser.getId())
                .isPositive(true)
                .useful(0)
                .build());

        reviewStorage.add(review);

        reviewStorage.removeRating(review.getReviewId() + 1, createdUser.getId(), true);
        reviewStorage.removeRating(review.getReviewId(), createdUser.getId() + 1, true);
        reviewStorage.removeRating(review.getReviewId() + 1, createdUser.getId(), false);
        reviewStorage.removeRating(review.getReviewId(), createdUser.getId() + 1, false);
    }
}
