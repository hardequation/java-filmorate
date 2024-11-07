package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {

    Review add(Review review);

    Review update(Review newReview);

    void remove(int id);

    void removeAll();

    List<Review> findAll();

    Optional<Review> findById(int id);

    List<Review> findByFilmId(int id, int size);

    void addRating(int reviewId, int userId, boolean isLike);

    void removeRating(int id, int userId, boolean isLike);

}
