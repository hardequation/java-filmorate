package ru.yandex.practicum.filmorate.dal.impl;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.dal.UserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();

    private int nextId = 1;

    @Override
    public Collection<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User add(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User newUser) {
        users.replace(newUser.getId(), newUser);
        return newUser;
    }

    @Override
    public boolean contains(Integer id) {
        return users.containsKey(id);
    }

    @Override
    public Optional<User> findById(int id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public void addFriendship(Integer userId, Integer friendId, boolean confirmed) {
        users.get(userId).getFriends().add(friendId);
    }

    @Override
    public void removeFriendship(Integer userId, Integer friendId) {
        users.get(userId).getFriends().remove(friendId);
    }

    @Override
    public void removeAll() {
        users.clear();
    }

    private int getNextId() {
        return nextId++;
    }
}
