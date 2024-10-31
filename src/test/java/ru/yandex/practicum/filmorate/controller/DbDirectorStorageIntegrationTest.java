package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.impl.DbDirectorStorage;
import ru.yandex.practicum.filmorate.dal.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.model.Director;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@JdbcTest
@Import({DbDirectorStorage.class, DirectorRowMapper.class})
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DbDirectorStorageIntegrationTest {
    @Autowired
    private final JdbcTemplate template;

    private DbDirectorStorage directorStorage;

    private Director director = null;

    @BeforeEach
    void setUp() {
        DirectorRowMapper directorRowMapper = new DirectorRowMapper();
        directorStorage = new DbDirectorStorage(template, directorRowMapper);

        director = Director.builder()
                .name("Director")
                .build();
    }

    @Test
    @DisplayName("Добавление режиссёра и получение его по ID")
    void testCreateAndFindDirectorById() {
        Director addedDirector = directorStorage.createDirector(director);

        assertEquals(1, directorStorage.findAllDirectors().size());
        assertEquals(addedDirector, directorStorage.findDirectorById(addedDirector.getId()).get());
    }

    @Test
    @DisplayName("Получение списка режиссёров")
    void testGetAllDirectors() {
        directorStorage.createDirector(director);
        directorStorage.createDirector(director);
        directorStorage.createDirector(director);
        assertEquals(3, directorStorage.findAllDirectors().size());
    }

    @Test
    @DisplayName("Обновление режиссёра")
    void testUpdateDirector() {
        Director addedDirector = directorStorage.createDirector(director);
        directorStorage.updateDirector(Director.builder()
                .id(addedDirector.getId()).name("NewDirector").build());
        assertEquals("NewDirector", directorStorage.findDirectorById(addedDirector.getId()).get().getName());
    }

    @Test
    @DisplayName("Удаление режиссёра")
    void testDeleteDirector() {
        directorStorage.createDirector(director);
        directorStorage.deleteDirector(director.getId());
        assertEquals(0, directorStorage.findAllDirectors().size());
    }

}
