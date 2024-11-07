package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {

    List<User> findAll();

    User add(User user);

    void removeUser(Integer id);

    User update(User newUser);

    boolean contains(Integer id);

    Optional<User> findById(int id);

    List<User> getFriendsbyUserId(int userId);

    List<User> getCommonFriends(int userId, int friendId);

    void addFriendship(Integer userId, Integer friendId);

    void removeFriendship(Integer userId, Integer friendId);

    void removeAll();
}
