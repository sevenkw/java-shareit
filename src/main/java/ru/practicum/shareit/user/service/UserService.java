package ru.practicum.shareit.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicateException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));
    }


    @Transactional
    public User createNewUser(User user) {
        var exist = userRepository.existsByEmail(user.getEmail());

        if (exist) {
            throw new DuplicateException("Пользователь с данной почтой уже зарегестирован");
        }

        return userRepository.save(user);
    }


    @Transactional
    public User updateUser(Long id, User user) {
        if (id == null) {
            throw new ValidationException("id не должен быть null");
        }

        User existing = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));


        if (user.getName() != null) {
            existing.setName(user.getName());
        }

        if (user.getEmail() != null) {
            if (userRepository.existsByEmailAndIdNot(user.getEmail(), id)) {
                throw new DuplicateException("Пользователь с данной почтой уже зарегестрирован");
            }
            existing.setEmail(user.getEmail());
        }

        return userRepository.save(existing);

    }

    @Transactional
    public void deleteUserById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        userRepository.deleteById(id);
    }
}
