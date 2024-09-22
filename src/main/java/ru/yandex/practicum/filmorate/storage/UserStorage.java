package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {

    Collection<User> getUsers();

    User add(User user);

    User update(User newUser);

    boolean contains(Long id);

    User getUser(long id);
}
