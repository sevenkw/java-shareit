package ru.practicum.shareit.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }


    public List<User> getAllUsers() {
        return userStorage.getAll();
    }


    public User getUserById(Long id) {
        return userStorage.getById(id);
    }


    public User createNewUser(User user) {
        return userStorage.create(user);
    }


    public User updateUser(Long id, User user) {

        return userStorage.update(id, user);
    }

    public void deleteUserById(Long id) {
        userStorage.deleteById(id);
    }
}
