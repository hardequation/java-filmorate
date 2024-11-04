package ru.yandex.practicum.filmorate.controller.mappers;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dto.create.CreateDirectorDto;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DirectorMapper {
    public DirectorDto map(Director director) {
        return DirectorDto.builder()
                .id(director.getId())
                .name(director.getName())
                .build();
    }

    public Director map(DirectorDto directorDto) {
        return Director.builder()
                .id(directorDto.getId())
                .name(directorDto.getName())
                .build();
    }

    public Director map(CreateDirectorDto directorDto) {
        return Director.builder()
                .name(directorDto.getName())
                .build();
    }

    public LinkedHashSet<Director> mapToDirectorList(Set<DirectorDto> directorDtos) {
        if (directorDtos == null) {
            return new LinkedHashSet<>();
        }
        return directorDtos.stream()
                .map(this::map)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public LinkedHashSet<DirectorDto> mapToDirectorDtoList(Set<Director> directors) {
        if (directors == null) {
            return new LinkedHashSet<>();
        }
        return directors.stream()
                .map(this::map)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
