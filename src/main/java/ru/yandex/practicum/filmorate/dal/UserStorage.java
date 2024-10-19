package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {

    Collection<User> findAll();

    User add(User user);

    User update(User newUser);

    boolean contains(Integer id);

    Optional<User> findById(int id);

    void addFriendship(Integer userId, Integer friendId, boolean confirmed);

    void removeFriendship(Integer userId, Integer friendId);

    void removeAll();
}
