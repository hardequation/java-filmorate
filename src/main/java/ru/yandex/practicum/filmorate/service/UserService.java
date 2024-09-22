package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class UserService {

    UserStorage storage;

    private long nextId = 1;

    @Autowired
    public UserService(UserStorage service) {
        this.storage = service;
    }
    public Collection<User> getUsers() {
        return storage.getUsers();
    }

    public User getUser(long id) {
        return storage.getUser(id);
    }

    public User create(User user) {
        user.setId(getNextId());

        String name = user.getName();
        if (name == null || name.isBlank()) {
            user.setName(user.getLogin());
        }
        storage.add(user);
        return user;
    }

    public User updateUser(User newUser) {
        if (!storage.contains(newUser.getId())) {
            throw new NotFoundException("User with id = " + newUser.getId() + " isn't found");
        }

        String name = newUser.getName();
        if (name == null || name.isBlank()) {
            newUser.setName(newUser.getLogin());
        }

        storage.update(newUser);
        return newUser;
    }

    private long getNextId() {
        return nextId++;
    }

    public void addFriend(Long userId, Long newFriendId) {
        if (!storage.contains(userId)) {
            throw new NotFoundException("Unable to add friend to non-existing user");
        }
        if (!storage.contains(newFriendId)) {
            throw new NotFoundException("Unable to add non-existing friend");
        }

        Set<Long> userFriends = storage.getUser(userId).getFriends();
        Set<Long> friends = storage.getUser(newFriendId).getFriends();
        if (userFriends.contains(newFriendId) || friends.contains(userId)) {
            throw new ValidationException("Users are already friends");
        }

        userFriends.add(newFriendId);
        friends.add(userId);
    }

    public void removeFriend(Long userId, Long newFriendId) {
        if (!storage.contains(userId)) {
            throw new NotFoundException("Unable to remove friend of non-existing user");
        }
        if (!storage.contains(newFriendId)) {
            throw new NotFoundException("Unable to remove non-existing friend");
        }

        Set<Long> userFriends = storage.getUser(userId).getFriends();
        Set<Long> friends = storage.getUser(newFriendId).getFriends();
        if (!userFriends.contains(newFriendId) || !friends.contains(userId)) {
            return;
        }

        userFriends.remove(newFriendId);
        friends.remove(userId);
    }

    public List<User> getUserFriends(Long id) {
        if (!storage.contains(id)) {
            throw new NotFoundException("Unable to find user with id: " + id);
        }
        return storage.getUser(id).getFriends().stream()
                .map(friendId -> storage.getUser(friendId))
                .toList();
    }

}
