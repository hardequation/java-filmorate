package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.review.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {

    Review add(Review review);

    Review update(Review newReview);

    void remove(int id);

    Optional<Review> findById(int id);

    List<Review> findByFilmId(int id, int size);
}
