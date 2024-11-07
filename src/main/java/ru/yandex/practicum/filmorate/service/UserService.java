package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FeedStorage;
import ru.yandex.practicum.filmorate.dal.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

import static ru.yandex.practicum.filmorate.model.enums.EventType.FRIEND;
import static ru.yandex.practicum.filmorate.model.enums.Operation.ADD;
import static ru.yandex.practicum.filmorate.model.enums.Operation.REMOVE;
import static ru.yandex.practicum.filmorate.utils.ErrorMessages.USER_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;
    private final FeedStorage feedStorage;

    public List<User> getUsers() {
        return userStorage.findAll();
    }

    public User getUser(int id) {
        return userStorage.findById(id).orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + id));
    }

    public User create(User user) {
        setName(user);
        return userStorage.add(user);
    }

    public void removeUser(Integer id) {
        userStorage.removeUser(id);
    }

    public User updateUser(User newUser) {
        setName(newUser);
        userStorage.update(newUser);
        return newUser;
    }

    public void addFriend(Integer userId, Integer newFriendId) {
        if (!userStorage.contains(userId)) {
            throw new NotFoundException(USER_NOT_FOUND + userId);
        }
        if (!userStorage.contains(newFriendId)) {
            throw new NotFoundException(USER_NOT_FOUND + newFriendId);
        }
        userStorage.addFriendship(userId, newFriendId);
        feedStorage.addFeed(newFriendId, userId, FRIEND, ADD);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        List<Integer> userFriends = getUserFriendIds(userId);
        if (!userStorage.contains(userId)) {
            throw new NotFoundException("Can't remove friend of non-existing user with id " + userId);
        }

        if (!userStorage.contains(friendId)) {
            throw new NotFoundException("Can't remove non-existing friend with id " + friendId);
        }

        if (!userFriends.contains(friendId)) {
            return;
        }
        userStorage.removeFriendship(userId, friendId);
        feedStorage.addFeed(friendId, userId, FRIEND, REMOVE);
    }

    public List<User> getUserFriends(Integer id) {
        return userStorage.getFriendsbyUserId(id);
    }

    public List<Integer> getUserFriendIds(Integer id) {
        return userStorage.getFriendsbyUserId(id).stream()
                .map(User::getId)
                .toList();
    }

    public List<User> getCommonFriends(Integer userId, Integer friendId) {
        return userStorage.getCommonFriends(userId, friendId);
    }

    private void setName(User user) {
        String name = user.getName();
        if (name == null || name.isBlank()) {
            user.setName(user.getLogin());
        }
    }

    public List<Feed> getFeedByUserId(Integer id) {
        getUser(id);
        return feedStorage.getFeedByUserId(id);
    }
}
