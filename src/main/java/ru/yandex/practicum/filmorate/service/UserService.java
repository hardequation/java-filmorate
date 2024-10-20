package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static ru.yandex.practicum.filmorate.utils.ErrorMessages.USER_NOT_FOUND;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("dbUserStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> getUsers() {
        return new ArrayList<>(userStorage.findAll());
    }

    public User getUser(int id) {
        return userStorage.findById(id).orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + id));
    }

    public User create(User user) {
        setName(user);
        userStorage.add(user);
        return user;
    }

    public User updateUser(User newUser) {
        if (!userStorage.contains(newUser.getId())) {
            throw new NotFoundException(USER_NOT_FOUND + newUser.getId());
        }

        setName(newUser);

        userStorage.update(newUser);
        return newUser;
    }

    public User addFriend(Integer userId, Integer newFriendId) {
        if (!userStorage.contains(userId)) {
            throw new NotFoundException(USER_NOT_FOUND + userId);
        }
        if (!userStorage.contains(newFriendId)) {
            throw new NotFoundException(USER_NOT_FOUND + newFriendId);
        }
        User friend = userStorage.findById(newFriendId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + newFriendId));
        List<Integer> userFriends = getUserFriendIds(userId);
        List<Integer> friends = getUserFriendIds(newFriendId);
        if (userFriends.contains(newFriendId) || friends.contains(userId)) {
            throw new ValidationException("Users with ids " + userId + " and " + newFriendId + " are already friends");
        }

        userStorage.addFriendship(userId, newFriendId, true);
        return friend;
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
    }

    public List<User> getUserFriends(Integer id) {
        User user = userStorage.findById(id).orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + id));
        Set<Integer> friendIds = user.getFriendshipStatuses().keySet();
        return friendIds.stream()
                .filter(friendId -> user.getFriendshipStatuses().get(friendId))
                .map(friendId -> userStorage.findById(friendId)
                        .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + friendId)))
                .toList();
    }

    public List<Integer> getUserFriendIds(Integer id) {
        User user = userStorage.findById(id).orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + id));
        Set<Integer> friendIds = user.getFriendshipStatuses().keySet();
        return friendIds.stream()
                .filter(friendId -> user.getFriendshipStatuses().get(friendId))
                .toList();
    }

    private void setName(User user) {
        String name = user.getName();
        if (name == null || name.isBlank()) {
            user.setName(user.getLogin());
        }
    }

}
