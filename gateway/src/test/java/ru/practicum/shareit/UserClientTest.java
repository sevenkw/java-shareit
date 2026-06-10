package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.UserDto;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserClientTest {

    @Test
    void getUsers() {
        TestUserClient client = new TestUserClient();

        ResponseEntity<Object> response = client.getUsers();

        assertEquals(200, response.getStatusCode().value());
        assertEquals("", client.path);
    }

    @Test
    void getUserById() {
        TestUserClient client = new TestUserClient();

        ResponseEntity<Object> response = client.getUserById(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("/1", client.path);
    }

    @Test
    void createUser() {
        TestUserClient client = new TestUserClient();
        UserDto userDto = new UserDto();
        userDto.setName("Yaroslav");
        userDto.setEmail("email@yandex.ru");

        ResponseEntity<Object> response = client.createUser(userDto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("", client.path);
        assertEquals(userDto, client.body);
    }

    @Test
    void updateUser() {
        TestUserClient client = new TestUserClient();
        UserDto userDto = new UserDto();
        userDto.setName("Updated");

        ResponseEntity<Object> response = client.updateUser(1L, userDto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("/1", client.path);
        assertEquals(userDto, client.body);
    }

    @Test
    void deleteUserById() {
        TestUserClient client = new TestUserClient();

        ResponseEntity<Object> response = client.deleteUserById(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("/1", client.path);
    }

    private static class TestUserClient extends UserClient {
        private String path;
        private Object body;

        private TestUserClient() {
            super("http://localhost", new RestTemplateBuilder());
        }

        @Override
        protected ResponseEntity<Object> get(String path) {
            this.path = path;
            return ResponseEntity.ok().build();
        }

        @Override
        protected <T> ResponseEntity<Object> post(String path, T body) {
            this.path = path;
            this.body = body;
            return ResponseEntity.ok().build();
        }

        @Override
        protected <T> ResponseEntity<Object> patch(String path, T body) {
            this.path = path;
            this.body = body;
            return ResponseEntity.ok().build();
        }

        @Override
        protected ResponseEntity<Object> delete(String path) {
            this.path = path;
            return ResponseEntity.ok().build();
        }
    }
}
