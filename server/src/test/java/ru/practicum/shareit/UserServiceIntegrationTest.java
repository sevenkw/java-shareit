package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.Matchers.containsString;

import java.util.List;

@Transactional
@SpringBootTest(properties =
        "spring.datasource.url=jdbc:h2:mem:shareit-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class UserServiceIntegrationTest {
    @Autowired
    private UserService userService;

    @Test
    void getAllUsersPositive() {
        User user = new User();
        user.setName("test");
        user.setEmail("test@email");

        var savedUser1 = userService.createNewUser(user);

        User user2 = new User();
        user2.setName("test2");
        user2.setEmail("test2@email");

        var savedUser2 = userService.createNewUser(user2);

        List<User> result = userService.getAllUsers();

        assertThat(result.size(), is(2));
        assertThat(result.get(0).getId(), is(savedUser1.getId()));
        assertThat(result.get(1).getId(), is(savedUser2.getId()));
    }

    @Test
    void getAllUsersShouldEmpty() {
        List<User> result = userService.getAllUsers();
        assertThat(result.size(), is(0));
    }

    @Test
    void getUserByIdPositive() {
        User user = new User();
        user.setName("test");
        user.setEmail("test@email");
        var savedUser1 = userService.createNewUser(user);

        User result = userService.getUserById(savedUser1.getId());

        assertThat(result.getId(), is(savedUser1.getId()));
        assertThat(result.getName(), is("test"));
        assertThat(result.getEmail(), is("test@email"));
    }

    @Test
    void getUserByIdNegative() {
        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.getUserById(2L));

        assertThat(exception.getMessage(), containsString("Пользователь"));

    }

    @Test
    void createNewUserPositive() {
        User user = new User();
        user.setName("test");
        user.setEmail("test@email");

        User result = userService.createNewUser(user);

        assertThat(result.getId(), notNullValue());
        assertThat(result.getName(), is("test"));
        assertThat(result.getEmail(), is("test@email"));
    }

    @Test
    void createNewUserDuplicateEmail() {
        User user = new User();
        user.setName("test");
        user.setEmail("test@email");
        userService.createNewUser(user);

        User duplicateUser = new User();
        duplicateUser.setName("test2");
        duplicateUser.setEmail("test@email");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.createNewUser(duplicateUser));

        assertThat(exception.getMessage(), containsString("почт"));
    }

    @Test
    void updateUserPositive() {
        User user = new User();
        user.setName("test");
        user.setEmail("test@email");
        User savedUser = userService.createNewUser(user);

        User updateUser = new User();
        updateUser.setName("updated");
        updateUser.setEmail("updated@email");

        User result = userService.updateUser(savedUser.getId(), updateUser);

        assertThat(result.getId(), is(savedUser.getId()));
        assertThat(result.getName(), is("updated"));
        assertThat(result.getEmail(), is("updated@email"));
    }

    @Test
    void updateUserOnlyNamePositive() {
        User user = new User();
        user.setName("test");
        user.setEmail("test@email");
        User savedUser = userService.createNewUser(user);

        User updateUser = new User();
        updateUser.setName("updated");

        User result = userService.updateUser(savedUser.getId(), updateUser);

        assertThat(result.getId(), is(savedUser.getId()));
        assertThat(result.getName(), is("updated"));
        assertThat(result.getEmail(), is("test@email"));
    }

    @Test
    void updateUserOnlyEmailPositive() {
        User user = new User();
        user.setName("test");
        user.setEmail("test@email");
        User savedUser = userService.createNewUser(user);

        User updateUser = new User();
        updateUser.setEmail("updated@email");

        User result = userService.updateUser(savedUser.getId(), updateUser);

        assertThat(result.getId(), is(savedUser.getId()));
        assertThat(result.getName(), is("test"));
        assertThat(result.getEmail(), is("updated@email"));
    }

    @Test
    void updateUserNegativeNotFound() {
        User updateUser = new User();
        updateUser.setName("updated");
        updateUser.setEmail("updated@email");

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.updateUser(99L, updateUser));

        assertThat(exception.getMessage(), containsString("Пользователь"));
    }

    @Test
    void updateUserDuplicateEmailNegative() {
        User user1 = new User();
        user1.setName("test1");
        user1.setEmail("test1@email");
        User savedUser1 = userService.createNewUser(user1);

        User user2 = new User();
        user2.setName("test2");
        user2.setEmail("test2@email");
        userService.createNewUser(user2);

        User updateUser = new User();
        updateUser.setEmail("test2@email");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.updateUser(savedUser1.getId(), updateUser));

        assertThat(exception.getMessage(), containsString("почт"));
    }

    @Test
    void deleteUserPositive() {
        User user = new User();
        user.setName("test");
        user.setEmail("test@email");
        User savedUser = userService.createNewUser(user);

        userService.deleteUserById(savedUser.getId());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.getUserById(savedUser.getId()));

        assertThat(exception.getMessage(), containsString("Пользователь"));
    }

    @Test
    void deleteUserNegative() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.deleteUserById(99L));

        assertThat(exception.getMessage(), containsString("Пользователь"));
    }
}
