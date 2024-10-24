package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;
import java.util.Optional;

public interface RatingStorage {

    List<MpaRating> findAllMpaRatings();

    boolean containsRating(Integer ratingId);

    Optional<MpaRating> findMpaRatingById(int id);

}
