package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.DuplicateException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.model.User;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new LinkedHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public User create(User user) {

        String email = user.getEmail();
        if (email == null) {
            throw new ValidationException("Почта не должна быть null");
        }

        for (User u : users.values()) {
            if (Objects.equals(u.getEmail(), email)) {
                throw new DuplicateException("Пользователь с данной почтой уже существует");
            }
        }

        User savedUser = new User();
        savedUser.setId(nextId.getAndIncrement());
        savedUser.setName(user.getName());
        savedUser.setEmail(user.getEmail());

        var key = savedUser.getId();
        users.put(key, savedUser);
        return savedUser;
    }

    @Override
    public User update(Long id, User user) {
        if (id == null) {
            throw new ValidationException("Id не должна быть null");
        }

        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }

        for (User u : users.values()) {
            if (Objects.equals(u.getEmail(), user.getEmail()) && !Objects.equals(u.getId(), id)) {
                throw new DuplicateException("Пользователь с данной почтой уже существует");
            }
        }

        User updatedUser = users.get(id);

        if (user.getName() != null) {
            updatedUser.setName(user.getName());
        }

        if (user.getEmail() != null) {
            updatedUser.setEmail(user.getEmail());
        }

        users.put(id, updatedUser);
        return updatedUser;
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new ValidationException("id не должен быть null");
        }

        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }

        users.remove(id);

    }

    @Override
    public User getById(Long id) {
        if (id == null) {
            throw new ValidationException("id не должен быть null");
        }

        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }

        return users.get(id);
    }

    @Override
    public List<User> getAll() {
        List<User> allUsers = new ArrayList<>();

        for (var user : users.values()) {
            allUsers.add(user);
        }

        return allUsers;
    }

}
