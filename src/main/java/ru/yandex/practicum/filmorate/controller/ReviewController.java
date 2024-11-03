package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.controller.mappers.ReviewMapper;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.create.CreateReviewDto;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService service;

    private final ReviewMapper reviewMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewDto create(@Valid @RequestBody CreateReviewDto dto) {
        Review toCreate = reviewMapper.map(dto);
        Review createdFilm = service.create(toCreate);
        return reviewMapper.map(createdFilm);
    }

    @PutMapping
    public ReviewDto update(@Valid @RequestBody ReviewDto dto) {
        Review review = reviewMapper.map(dto);
        Review updatedReview = service.update(review);
        return reviewMapper.map(updatedReview);
    }

    @DeleteMapping("/{id}")
    public void remove(@PathVariable Integer id) {
        service.remove(id);
    }

    @GetMapping("/{id}")
    public ReviewDto getReview(@PathVariable Integer id) {
        Review review = service.findById(id);
        return reviewMapper.map(review);
    }

    @GetMapping
    public List<ReviewDto> getReview(@RequestParam int filmId, @RequestParam(defaultValue = "10") @Positive int count) {
        List<Review> reviews = service.findByFilmId(filmId, count);
        return reviews.stream()
                .map(reviewMapper::map)
                .toList();
    }

    @PutMapping("/{id}/like/{userId}")
    public void like(@PathVariable int id, @PathVariable int userId) {
        service.addRating(id, userId, true);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void dislike(@PathVariable int id, @PathVariable int userId) {
        service.addRating(id, userId, false);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable int id, @PathVariable int userId) {
        service.removeRating(id, userId, true);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislike(@PathVariable int id, @PathVariable int userId) {
        service.removeRating(id, userId, false);
    }

}
