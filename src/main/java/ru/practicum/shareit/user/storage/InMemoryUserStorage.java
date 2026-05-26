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
    private final HashSet<String> usersEmails = new HashSet<>();

    @Override
    public User create(User user) {

        String email = user.getEmail();
        if (email == null) {
            throw new ValidationException("Почта не должна быть null");
        }

        if (!usersEmails.add(email)) {
            throw new DuplicateException("Пользователь с данной почтой уже существует");
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

        User updatedUser = users.get(id);

        if (user.getName() != null) {
            updatedUser.setName(user.getName());
        }

        if (user.getEmail() != null) {
            String newEmail = user.getEmail();
            String oldEmail = updatedUser.getEmail();

            if (!Objects.equals(oldEmail, newEmail) && usersEmails.contains(newEmail)) {
                throw new DuplicateException("Пользователь с данной почтой уже существует");
            }

            usersEmails.remove(oldEmail);
            usersEmails.add(newEmail);
            updatedUser.setEmail(newEmail);
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

        usersEmails.remove(users.get(id).getEmail());

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
        return new ArrayList<>(users.values());
    }

}
