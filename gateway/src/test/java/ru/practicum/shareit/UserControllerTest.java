package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.UserDto;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.ResponseEntity;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserClient userClient;

    @Test
    void getUsers() throws Exception {
        when(userClient.getUsers()).thenReturn(ResponseEntity.ok(List.of(
                createUser(1L, "Yaroslav", "email@yandex.ru"),
                createUser(2L, "Yanis", "yanis@yandex.ru")
        )));

        mvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Yaroslav")))
                .andExpect(jsonPath("$[0].email", is("email@yandex.ru")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Yanis")))
                .andExpect(jsonPath("$[1].email", is("yanis@yandex.ru")));

        verify(userClient).getUsers();
    }

    @Test
    void getUserById() throws Exception {
        when(userClient.getUserById(1L)).thenReturn(ResponseEntity.ok(createUser(1L, "Yaroslav", "email@yandex.ru")));

        mvc.perform(get("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Yaroslav")))
                .andExpect(jsonPath("$.email", is("email@yandex.ru")));

        verify(userClient).getUserById(1L);
    }

    @Test
    void getUserByIdInvalidId() throws Exception {
        mvc.perform(get("/users/{id}", 0L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userClient);
    }

    @Test
    void createUserInvalidEmail() throws Exception {
        UserDto requestDto = new UserDto();
        requestDto.setName("Yaroslav");
        requestDto.setEmail("invalid-email");

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userClient);
    }

    @Test
    void createUser() throws Exception {
        UserDto requestDto = new UserDto();
        requestDto.setName("Yaroslav");
        requestDto.setEmail("email@yandex.ru");

        when(userClient.createUser(any(UserDto.class)))
                .thenReturn(ResponseEntity.status(201).body(createUser(1L, "Yaroslav", "email@yandex.ru")));

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Yaroslav")))
                .andExpect(jsonPath("$.email", is("email@yandex.ru")));

        verify(userClient).createUser(any(UserDto.class));
    }

    @Test
    void updateUserInvalidEmail() throws Exception {
        UserDto requestDto = new UserDto();
        requestDto.setEmail("invalid-email");

        mvc.perform(patch("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userClient);
    }

    @Test
    void updateUser() throws Exception {
        UserDto requestDto = new UserDto();
        requestDto.setName("Updated");
        requestDto.setEmail("updated@yandex.ru");

        when(userClient.updateUser(eq(1L), any(UserDto.class)))
                .thenReturn(ResponseEntity.ok(createUser(1L, "Updated", "updated@yandex.ru")));

        mvc.perform(patch("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Updated")))
                .andExpect(jsonPath("$.email", is("updated@yandex.ru")));

        verify(userClient).updateUser(eq(1L), any(UserDto.class));
    }

    @Test
    void updateUserInvalidId() throws Exception {
        UserDto requestDto = new UserDto();
        requestDto.setName("Updated");

        mvc.perform(patch("/users/{id}", -1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userClient);
    }

    @Test
    void deleteUserInvalidId() throws Exception {
        mvc.perform(delete("/users/{id}", 0L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userClient);
    }

    @Test
    void deleteUser() throws Exception {
        when(userClient.deleteUserById(1L)).thenReturn(ResponseEntity.noContent().build());

        mvc.perform(delete("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(userClient).deleteUserById(1L);
    }

    private UserDto createUser(Long id, String name, String email) {
        UserDto userDto = new UserDto();
        userDto.setId(id);
        userDto.setName(name);
        userDto.setEmail(email);
        return userDto;
    }
}
