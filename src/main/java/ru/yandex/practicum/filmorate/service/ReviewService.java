package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FeedStorage;
import ru.yandex.practicum.filmorate.dal.ReviewStorage;
import ru.yandex.practicum.filmorate.dal.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

import static ru.yandex.practicum.filmorate.model.enums.EventType.REVIEW;
import static ru.yandex.practicum.filmorate.model.enums.Operation.ADD;
import static ru.yandex.practicum.filmorate.model.enums.Operation.REMOVE;
import static ru.yandex.practicum.filmorate.model.enums.Operation.UPDATE;
import static ru.yandex.practicum.filmorate.utils.ErrorMessages.REVIEW_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final FeedStorage feedStorage;
    private final UserStorage userStorage;

    public Review create(Review review) {
        Review createdReview = reviewStorage.add(review);
        feedStorage.addFeed(createdReview.getReviewId(), createdReview.getUserId(), REVIEW, ADD);
        return createdReview;
    }

    public Review update(Review review) {
        Review updatedReview = reviewStorage.update(review);
        feedStorage.addFeed(updatedReview.getReviewId(), updatedReview.getUserId(), REVIEW, UPDATE);
        return updatedReview;
    }

    public void remove(int id) {
        Optional<Review> review = reviewStorage.findById(id);
        if (review.isPresent()) {
            Integer userId = review.get().getUserId();
            reviewStorage.remove(id);
            feedStorage.addFeed(id, userId, REVIEW, REMOVE);
        }
    }

    public List<Review> findAll() {
        return reviewStorage.findAll();
    }

    public Review findById(int id) {
        return reviewStorage.findById(id).orElseThrow(() -> new NotFoundException(REVIEW_NOT_FOUND + id));
    }

    public List<Review> findByFilmId(int filmId, int size) {
        return reviewStorage.findByFilmId(filmId, size);
    }

    public void addRating(int reviewId, int userId, boolean isLike) {
        reviewStorage.addRating(reviewId, userId, isLike);
    }

    public void removeRating(int reviewId, int userId, boolean isLike) {
        reviewStorage.removeRating(reviewId, userId, isLike);
    }
}
