package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.controller.mappers.DirectorMapper;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dto.create.CreateDirectorDto;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.film.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {

    private final FilmService service;

    private final DirectorMapper mapper;

    @GetMapping
    public List<DirectorDto> findAllDirectors() {
        List<Director> directors = service.getDirectors();
        log.info("Returning list of directors");
        return directors.stream().map(mapper::map).toList();
    }

    @GetMapping("/{id}")
    public DirectorDto findDirectorById(@PathVariable int id) {
        Director director = service.findDirectorById(id);
        log.info("Director {} is found", id);
        return mapper.map(director);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DirectorDto createDirector(@RequestBody @Valid CreateDirectorDto directorDto) {
        Director directorSaved = service.createDirector(mapper.map(directorDto));
        log.info("Director {} is created", directorSaved.getId());
        return mapper.map(directorSaved);
    }

    @PutMapping
    public DirectorDto updateDirector(@RequestBody @Valid DirectorDto directorDto) {
        Director directorSaved = service.updateDirector(mapper.map(directorDto));
        log.info("Director {} is updated", directorSaved.getId());
        return mapper.map(directorSaved);
    }

    @DeleteMapping("/{id}")
    public void deleteDirector(@PathVariable int id) {
        service.deleteDirector(id);
        log.info("Director {} is removed", id);
    }
}
