package ru.yandex.practicum.filmorate.controller.mappers;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.MpaRatingDto;
import ru.yandex.practicum.filmorate.model.MpaRating;

@Component
public class MpaRatingMapper {

    public MpaRatingDto map(MpaRating mpa) {
        return MpaRatingDto.builder()
                .id(mpa.getId())
                .name(mpa.getName())
                .build();
    }

    public MpaRating map(MpaRatingDto mpa) {
        return MpaRating.builder()
                .id(mpa.getId())
                .name(mpa.getName())
                .build();
    }
}
