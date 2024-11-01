package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.ReviewStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

import static ru.yandex.practicum.filmorate.utils.ErrorMessages.REVIEW_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;

    public Review create(Review review) {
        return reviewStorage.add(review);
    }

    public Review update(Review review) {
        return reviewStorage.update(review);
    }

    public void remove(int id) {
        reviewStorage.remove(id);
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
