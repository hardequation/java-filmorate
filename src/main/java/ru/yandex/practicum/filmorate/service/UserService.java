package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static ru.yandex.practicum.filmorate.utils.ErrorMessages.USER_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage storage;

    public Collection<User> getUsers() {
        return new ArrayList<>(storage.getUsers());
    }

    public User getUser(long id) {
        return storage.getUser(id).orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + id));
    }

    public User create(User user) {
        setName(user);
        storage.add(user);
        return user;
    }

    public User updateUser(User newUser) {
        if (!storage.contains(newUser.getId())) {
            throw new NotFoundException(USER_NOT_FOUND + newUser.getId());
        }

        setName(newUser);

        storage.update(newUser);
        return newUser;
    }

    public void addFriend(Long userId, Long newFriendId) {
        User user = storage.getUser(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + userId));
        User friend = storage.getUser(newFriendId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + newFriendId));
        Set<Long> userFriends = user.getFriends();
        Set<Long> friends = friend.getFriends();
        if (userFriends.contains(newFriendId) || friends.contains(userId)) {
            throw new ValidationException("Users with ids " + userId + " and " + newFriendId + " are already friends");
        }

        userFriends.add(newFriendId);
        friends.add(userId);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = storage.getUser(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + userId));
        User friend = storage.getUser(friendId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + friendId));
        Set<Long> userFriends = user.getFriends();
        Set<Long> friends = friend.getFriends();
        if (!userFriends.contains(friendId) || !friends.contains(userId)) {
            return;
        }

        userFriends.remove(friendId);
        friends.remove(userId);
    }

    public List<User> getUserFriends(Long id) {
        User user = storage.getUser(id).orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + id));
        return user.getFriends().stream()
                .map(friendId -> storage.getUser(friendId)
                        .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + friendId)))
                .toList();
    }

    private void setName(User user) {
        String name = user.getName();
        if (name == null || name.isBlank()) {
            user.setName(user.getLogin());
        }
    }

}
