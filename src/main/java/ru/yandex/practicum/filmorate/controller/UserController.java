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
import ru.yandex.practicum.filmorate.controller.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.controller.mappers.UserMapper;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.dto.create.CreateUserDto;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final FilmService filmService;

    private final UserMapper userMapper;

    private final FilmMapper filmMapper;

    @GetMapping
    public List<UserDto> findAll() {
        List<User> users = userService.getUsers();
        return users.stream().map(userMapper::map).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody CreateUserDto user) {
        User toCreate = userMapper.map(user);
        User createdFilm = userService.create(toCreate);
        return userMapper.map(createdFilm);
    }

    @DeleteMapping("/{userId}")
    public void removeUser(@PathVariable Integer userId) {
        userService.removeUser(userId);
    }

    @PutMapping
    public UserDto updateUser(@Valid @RequestBody UserDto userDto) {
        User user = userMapper.map(userDto);
        User updatedUser = userService.updateUser(user);
        return userMapper.map(updatedUser);
    }

    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable int id) {
        User user = userService.getUser(id);
        return userMapper.map(user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<UserDto> getFriends(@PathVariable Integer id) {
        return userService.getUserFriends(id).stream()
                .map(userMapper::map).toList();
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<UserDto> getCommonFriends(@PathVariable Integer id, @PathVariable Integer otherId) {
        return userService.getCommonFriends(id, otherId).stream()
                .map(userMapper::map).toList();
    }

    @GetMapping("/{id}/recommendations")
    public List<FilmDto> getRecommendations(@PathVariable Integer id) {
        List<Film> recommendations = filmService.getFilmRecommendationsForUser(id);

        return recommendations.stream().map(filmMapper::map).toList();
    }

    @GetMapping("/{id}/feed")
    public List<Feed> getFeed(@PathVariable Integer id) {
        return userService.getFeedByUserId(id);
    }
}
