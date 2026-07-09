package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.Matchers.containsString;

@Transactional
@SpringBootTest(properties =
        "spring.datasource.url=jdbc:h2:mem:shareit-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;
    @Autowired
    private UserService userService;

    @Test
    void createNewItemRequest_shouldSaveRequest() {
        ItemRequestCreateDto itemRequestCreateDto = new ItemRequestCreateDto();
        itemRequestCreateDto.setDescription("test");

        User user = new User();
        user.setName("Yaroslav");
        user.setEmail("email@yandex.ru");

        var saveUser = userService.createNewUser(user);

        Long userId = saveUser.getId();

        var itemResponse = itemRequestService.createNewItemRequest(userId, itemRequestCreateDto);

        assertThat(itemResponse, notNullValue());
        assertThat(itemResponse.getId(), notNullValue());
        assertThat(itemResponse.getDescription(), equalTo("test"));
        assertThat(itemResponse.getCreated(), notNullValue());
        assertThat(itemResponse.getItems(), empty());
    }

    @Test
    void getUserItemRequests_shouldReturnRequestsSortedByCreatedDesc() {
        User user = new User();
        user.setName("Yaroslav");
        user.setEmail("email@yandex.ru");

        var saveUser = userService.createNewUser(user);

        Long userId = saveUser.getId();

        ItemRequestCreateDto dto1 = new ItemRequestCreateDto();
        dto1.setDescription("test1");
        ItemRequestCreateDto dto2 = new ItemRequestCreateDto();
        dto2.setDescription("test2");

        var resDto1 = itemRequestService.createNewItemRequest(userId, dto1);
        var resDto2 = itemRequestService.createNewItemRequest(userId, dto2);

        List<ItemRequestResponseDto> result = itemRequestService.getUserItemRequests(userId);

        assertThat(result, hasSize(2));
        assertThat(result.get(0).getId(), equalTo(resDto2.getId()));
        assertThat(result.get(1).getId(), equalTo(resDto1.getId()));
        assertThat(result.get(0).getItems(), empty());
        assertThat(result.get(1).getItems(), empty());

    }

    @Test
    void getUserItemRequests_shouldReturnEmptyListWhenUserHasNoRequests() {
        User user = new User();
        user.setName("Yaroslav");
        user.setEmail("email@yandex.ru");

        var saveUser = userService.createNewUser(user);

        List<ItemRequestResponseDto> result = itemRequestService.getUserItemRequests(saveUser.getId());

        assertThat(result, empty());
    }

    @Test
    void getItemRequestByRequestIdAndUserId() {
        User user = new User();
        user.setName("Yaroslav");
        user.setEmail("email@yandex.ru");

        var saveUser = userService.createNewUser(user);
        Long userId = saveUser.getId();

        ItemRequestCreateDto dto = new ItemRequestCreateDto();
        dto.setDescription("бензопила");

        var saveItemRequest = itemRequestService.createNewItemRequest(userId, dto);
        Long requestId = saveItemRequest.getId();

        ItemRequestResponseDto result = itemRequestService.getItemRequestByRequestIdAndUserId(userId, requestId);

        assertThat(result, notNullValue());
        assertThat(result.getId(), equalTo(requestId));
        assertThat(result.getDescription(), equalTo(dto.getDescription()));
        assertThat(result.getCreated(), notNullValue());
        assertThat(result.getItems(), empty());
    }

    @Test
    void getAllItemRequests_shouldReturnOtherUsersRequests() {
        User user = new User();
        user.setName("Yaroslav");
        user.setEmail("email@yandex.ru");

        var saveUser = userService.createNewUser(user);

        User user2 = new User();
        user2.setName("yanis");
        user2.setEmail("yanis@yandex.ru");

        var saveUser2 = userService.createNewUser(user2);

        ItemRequestCreateDto dto = new ItemRequestCreateDto();
        dto.setDescription("запрос на молоток");

        ItemRequestResponseDto saveItemRequest = itemRequestService.createNewItemRequest(saveUser.getId(), dto);

        List<ItemRequestResponseDto> result = itemRequestService.getAllItemRequests(saveUser2.getId());

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getDescription(), equalTo(dto.getDescription()));
        assertThat(result.get(0).getId(), equalTo(saveItemRequest.getId()));
        assertThat(result.get(0).getItems(), empty());
        assertThat(result.get(0).getCreated(), notNullValue());
    }

    @Test
    void getAllItemRequests_shouldReturnEmptyListWhenUserHasOnlyOwnRequests() {
        User user = new User();
        user.setName("Yaroslav");
        user.setEmail("email@yandex.ru");

        var savedUser = userService.createNewUser(user);

        ItemRequestCreateDto dto = new ItemRequestCreateDto();
        dto.setDescription("запрос на молоток");

        itemRequestService.createNewItemRequest(savedUser.getId(), dto);

        List<ItemRequestResponseDto> result = itemRequestService.getAllItemRequests(savedUser.getId());

        assertThat(result, empty());
    }

    @Test
    void createNewItemRequestInvalidUser() {
        ItemRequestCreateDto dto = new ItemRequestCreateDto();
        dto.setDescription("test");

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.createNewItemRequest(1L, dto));

        assertThat(exception.getMessage(), containsString("Пользователь"));
    }

    @Test
    void getUserItemRequestsInvalidUser() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.getUserItemRequests(1L));

        assertThat(exception.getMessage(), containsString("Пользователь"));
    }

    @Test
    void getItemRequestByRequestIdAndInvalidUserId() {

        User user = new User();
        user.setName("Yaroslav");
        user.setEmail("example@email.ru");

        var saveUser = userService.createNewUser(user);
        Long userId = saveUser.getId();

        ItemRequestCreateDto dto = new ItemRequestCreateDto();
        dto.setDescription("test");

        var saveItemRequest = itemRequestService.createNewItemRequest(userId, dto);


        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.getItemRequestByRequestIdAndUserId(99L, saveItemRequest.getId()));

        assertThat(exception.getMessage(), containsString("Пользователь"));
    }

    @Test
    void getItemRequestByRequestIdAndInvalidRequestId() {
        User user = new User();
        user.setName("Yaroslav");
        user.setEmail("example@email.ru");

        var saveUser = userService.createNewUser(user);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.getItemRequestByRequestIdAndUserId(saveUser.getId(), 99L));

        assertThat(exception.getMessage(), containsString("Запрос вещи"));
    }

    @Test
    void getAllItemRequestsInvalidUser() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.getAllItemRequests(2L));

        assertThat(exception.getMessage(), containsString("Пользователь"));
    }
}
