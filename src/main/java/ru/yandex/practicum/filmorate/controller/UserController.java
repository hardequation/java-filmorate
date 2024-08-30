package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RestControllerAdvice
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        validateUser(user);
        user.setId(getNextId());

        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User newUser) {
        validateUser(newUser);
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());

            String newLogin = newUser.getLogin();
            oldUser.setLogin(newLogin);

            String newName = newUser.getName();
            oldUser.setName(newName == null || newName.isEmpty() ? newLogin : newName);

            oldUser.setEmail(newUser.getEmail());

            oldUser.setBirthday(newUser.getBirthday());
            return oldUser;
        }
        throw new NotFoundException("User with id = " + newUser.getId() + " isn't found");
    }

    private void validateUser(User user) {

        if (user == null) {
            throw new ValidationException("User is null");
        }

        String email = user.getEmail();
        if (email == null || !email.contains("@")) {
            throw new ValidationException("Wrong email");
        }

        String login = user.getLogin();
        if (login == null || login.isEmpty() || login.contains(" ")) {
            throw new ValidationException("Login is not valid");
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Birthday is in the future");
        }
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
