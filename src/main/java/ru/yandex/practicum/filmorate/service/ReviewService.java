package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.ReviewStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.review.Review;

import java.util.List;

import static ru.yandex.practicum.filmorate.utils.ErrorMessages.REVIEW_NOT_FOUND;

@Service
public class ReviewService {

    ReviewStorage storage;

    public ReviewService(ReviewStorage storage) {
        this.storage = storage;
    }

    public Review create(Review review) {
        return storage.add(review);
    }

    public Review update(Review review) {
        return storage.update(review);
    }

    public void remove(int id) {
        storage.remove(id);
    }

    public Review findById(int id) {
        return storage.findById(id).orElseThrow(() -> new NotFoundException(REVIEW_NOT_FOUND + id));
    }

    public List<Review> findByFilmId(int filmId, int size) {
        return storage.findByFilmId(filmId, size);
    }
}
