package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserService service;

    @Test
    void getUsers() throws Exception {
        User firstUser = new User();
        firstUser.setId(1L);
        firstUser.setName("Yaroslav");
        firstUser.setEmail("email@yandex.ru");

        User secondUser = new User();
        secondUser.setId(2L);
        secondUser.setName("Yanis");
        secondUser.setEmail("yanis@yandex.ru");

        when(service.getAllUsers()).thenReturn(List.of(firstUser, secondUser));

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
    }

    @Test
    void getEmptyUsersList() throws Exception {
        when(service.getAllUsers()).thenReturn(List.of());

        mvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getUserById() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName("Yaroslav");
        user.setEmail("email@yandex.ru");

        when(service.getUserById(1L)).thenReturn(user);

        mvc.perform(get("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Yaroslav")))
                .andExpect(jsonPath("$.email", is("email@yandex.ru")));
    }

    @Test
    void createUser() throws Exception {
        UserDto requestDto = new UserDto();
        requestDto.setName("Yaroslav");
        requestDto.setEmail("email@yandex.ru");

        User createdUser = new User();
        createdUser.setId(1L);
        createdUser.setName("Yaroslav");
        createdUser.setEmail("email@yandex.ru");

        when(service.createNewUser(any(User.class))).thenReturn(createdUser);

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Yaroslav")))
                .andExpect(jsonPath("$.email", is("email@yandex.ru")));
    }

    @Test
    void updateUser() throws Exception {
        UserDto requestDto = new UserDto();
        requestDto.setName("Updated");
        requestDto.setEmail("updated@yandex.ru");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setName("Updated");
        updatedUser.setEmail("updated@yandex.ru");

        when(service.updateUser(eq(1L), any(User.class))).thenReturn(updatedUser);

        mvc.perform(patch("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Updated")))
                .andExpect(jsonPath("$.email", is("updated@yandex.ru")));
    }

    @Test
    void updateUserWithOnlyName() throws Exception {
        UserDto requestDto = new UserDto();
        requestDto.setName("Updated");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setName("Updated");
        updatedUser.setEmail("email@yandex.ru");

        when(service.updateUser(eq(1L), any(User.class))).thenReturn(updatedUser);

        mvc.perform(patch("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Updated")))
                .andExpect(jsonPath("$.email", is("email@yandex.ru")));
    }

    @Test
    void deleteUser() throws Exception {
        mvc.perform(delete("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(service).deleteUserById(1L);
    }

}
