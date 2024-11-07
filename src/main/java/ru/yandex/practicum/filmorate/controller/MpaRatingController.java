package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.controller.mappers.MpaRatingMapper;
import ru.yandex.practicum.filmorate.dto.MpaRatingDto;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.film.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaRatingController {

    private final FilmService service;

    private final MpaRatingMapper mapper;

    @GetMapping
    public List<MpaRatingDto> findAllMpa() {
        List<MpaRating> ratings = service.getRatings();
        return ratings.stream().map(mapper::map).toList();
    }

    @GetMapping("/{id}")
    public MpaRatingDto findMpa(@PathVariable int id) {
        MpaRating rating = service.findMpaRatingById(id);
        return mapper.map(rating);
    }
}
