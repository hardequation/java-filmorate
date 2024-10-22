package ru.yandex.practicum.filmorate.controller.mappers;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.MpaRatingDto;
import ru.yandex.practicum.filmorate.model.MpaRating;

@Service
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

    public Integer mapToId(MpaRatingDto mpa) {
        if (mpa == null) {
            return null;
        }
        return mpa.getId();
    }
}
