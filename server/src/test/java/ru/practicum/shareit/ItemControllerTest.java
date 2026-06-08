package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOwnerDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemService service;

    @Test
    void createNewItemWithoutRequestId() throws Exception {
        User owner = createUser(2L, "owner");

        ItemDto requestDto = new ItemDto();
        requestDto.setName("test");
        requestDto.setDescription("description");
        requestDto.setAvailable(true);

        ItemDto responseDto = new ItemDto();
        responseDto.setId(1L);
        responseDto.setName("test");
        responseDto.setDescription("description");
        responseDto.setAvailable(true);
        responseDto.setOwner(owner);

        when(service.createNewItem(eq(2L), any(ItemDto.class))).thenReturn(responseDto);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("test")))
                .andExpect(jsonPath("$.description", is("description")))
                .andExpect(jsonPath("$.available", is(true)))
                .andExpect(jsonPath("$.owner.id", is(2)))
                .andExpect(jsonPath("$.owner.name", is("owner")));

        verify(service).createNewItem(eq(2L), any(ItemDto.class));
    }

    @Test
    void createNewItemWithRequestId() throws Exception {
        User owner = createUser(2L, "owner");
        ItemRequest request = createRequest(2L, owner);

        ItemDto requestDto = new ItemDto();
        requestDto.setName("test");
        requestDto.setDescription("description");
        requestDto.setAvailable(true);
        requestDto.setRequestId(2L);

        ItemDto responseDto = new ItemDto();
        responseDto.setId(1L);
        responseDto.setName("test");
        responseDto.setDescription("description");
        responseDto.setAvailable(true);
        responseDto.setOwner(owner);
        responseDto.setRequest(request);
        responseDto.setRequestId(2L);

        when(service.createNewItem(eq(2L), any(ItemDto.class))).thenReturn(responseDto);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("test")))
                .andExpect(jsonPath("$.description", is("description")))
                .andExpect(jsonPath("$.available", is(true)))
                .andExpect(jsonPath("$.requestId", is(2)))
                .andExpect(jsonPath("$.owner.id", is(2)))
                .andExpect(jsonPath("$.request.id", is(2)))
                .andExpect(jsonPath("$.request.description", is("description")));

        verify(service).createNewItem(eq(2L), any(ItemDto.class));
    }

    @Test
    void createNewItemWithUnknownRequestId() throws Exception {
        ItemDto requestDto = new ItemDto();
        requestDto.setName("test");
        requestDto.setDescription("description");
        requestDto.setAvailable(true);
        requestDto.setRequestId(999L);

        when(service.createNewItem(eq(2L), any(ItemDto.class)))
                .thenThrow(new NotFoundException("Запрос не найден"));

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());

        verify(service).createNewItem(eq(2L), any(ItemDto.class));
    }

    @Test
    void createNewItemWithoutName() throws Exception {
        ItemDto requestDto = new ItemDto();
        requestDto.setDescription("description");
        requestDto.setAvailable(true);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void createNewItemWithBlankDescription() throws Exception {
        ItemDto requestDto = new ItemDto();
        requestDto.setName("test");
        requestDto.setDescription(" ");
        requestDto.setAvailable(true);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void createNewItemWithoutAvailable() throws Exception {
        ItemDto requestDto = new ItemDto();
        requestDto.setName("test");
        requestDto.setDescription("description");

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void createNewItemInvalidHeader() throws Exception {
        ItemDto requestDto = new ItemDto();
        requestDto.setName("test");
        requestDto.setDescription("description");
        requestDto.setAvailable(true);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void getItemsSuccess() throws Exception {
        ItemOwnerDto first = createItemOwnerDto(1L, "drill", 2L);
        ItemOwnerDto second = createItemOwnerDto(2L, "saw", 2L);

        when(service.getAllByOwner(2L)).thenReturn(List.of(first, second));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("drill")))
                .andExpect(jsonPath("$[0].comments", hasSize(1)))
                .andExpect(jsonPath("$[0].lastBooking.id", is(10)))
                .andExpect(jsonPath("$[0].nextBooking.id", is(11)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("saw")));

        verify(service).getAllByOwner(2L);
    }

    @Test
    void getItemsEmptyList() throws Exception {
        when(service.getAllByOwner(2L)).thenReturn(List.of());

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(service).getAllByOwner(2L);
    }

    @Test
    void getItemsInvalidHeader() throws Exception {
        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", -1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void getItemByIdSuccess() throws Exception {
        ItemOwnerDto itemOwnerDto = createItemOwnerDto(1L, "drill", 2L);

        when(service.getItemByIdForUser(2L, 1L)).thenReturn(itemOwnerDto);

        mvc.perform(get("/items/{id}", 1L)
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("drill")))
                .andExpect(jsonPath("$.description", is("description")))
                .andExpect(jsonPath("$.available", is(true)))
                .andExpect(jsonPath("$.owner.id", is(2)))
                .andExpect(jsonPath("$.comments", hasSize(1)));

        verify(service).getItemByIdForUser(2L, 1L);
    }

    @Test
    void getItemByIdInvalidItemId() throws Exception {
        mvc.perform(get("/items/{id}", 0L)
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void getItemByIdInvalidHeader() throws Exception {
        mvc.perform(get("/items/{id}", 1L)
                        .header("X-Sharer-User-Id", 0L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void searchItemsSuccess() throws Exception {
        User owner = createUser(2L, "owner");
        ItemRequest request = createRequest(5L, owner);

        Item item = new Item();
        item.setId(1L);
        item.setName("drill");
        item.setDescription("powerful");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(request);

        when(service.searchItems("drill")).thenReturn(List.of(item));

        mvc.perform(get("/items/search")
                        .param("text", "drill")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("drill")))
                .andExpect(jsonPath("$[0].requestId", is(5)))
                .andExpect(jsonPath("$[0].owner.id", is(2)))
                .andExpect(jsonPath("$[0].request.id", is(5)));

        verify(service).searchItems("drill");
    }

    @Test
    void searchItemsEmptyList() throws Exception {
        when(service.searchItems("unknown")).thenReturn(List.of());

        mvc.perform(get("/items/search")
                        .param("text", "unknown")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(service).searchItems("unknown");
    }

    @Test
    void updateItemSuccess() throws Exception {
        ItemDto requestDto = new ItemDto();
        requestDto.setName("updated");
        requestDto.setDescription("updated description");
        requestDto.setAvailable(false);

        Item updated = new Item();
        updated.setId(1L);
        updated.setName("updated");
        updated.setDescription("updated description");
        updated.setAvailable(false);
        updated.setOwner(createUser(2L, "owner"));

        when(service.updateItem(eq(2L), eq(1L), any(Item.class))).thenReturn(updated);

        mvc.perform(patch("/items/{id}", 1L)
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("updated")))
                .andExpect(jsonPath("$.description", is("updated description")))
                .andExpect(jsonPath("$.available", is(false)))
                .andExpect(jsonPath("$.owner.id", is(2)));

        verify(service).updateItem(eq(2L), eq(1L), any(Item.class));
    }

    @Test
    void updateItemInvalidItemId() throws Exception {
        ItemDto requestDto = new ItemDto();
        requestDto.setName("updated");

        mvc.perform(patch("/items/{id}", 0L)
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void updateItemInvalidHeader() throws Exception {
        ItemDto requestDto = new ItemDto();
        requestDto.setName("updated");

        mvc.perform(patch("/items/{id}", 1L)
                        .header("X-Sharer-User-Id", -2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void createCommentSuccess() throws Exception {
        CommentRequestDto requestDto = new CommentRequestDto();
        requestDto.setText("great item");

        CommentResponseDto responseDto = new CommentResponseDto();
        responseDto.setId(1L);
        responseDto.setText("great item");
        responseDto.setAuthorName("booker");
        responseDto.setCreated(LocalDateTime.now());

        when(service.createComment(eq(2L), eq(1L), any(CommentRequestDto.class))).thenReturn(responseDto);

        mvc.perform(post("/items/{itemId}/comment", 1L)
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.text", is("great item")))
                .andExpect(jsonPath("$.authorName", is("booker")));

        verify(service).createComment(eq(2L), eq(1L), any(CommentRequestDto.class));
    }

    @Test
    void createCommentBlankText() throws Exception {
        CommentRequestDto requestDto = new CommentRequestDto();
        requestDto.setText(" ");

        mvc.perform(post("/items/{itemId}/comment", 1L)
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void createCommentInvalidItemId() throws Exception {
        CommentRequestDto requestDto = new CommentRequestDto();
        requestDto.setText("great item");

        mvc.perform(post("/items/{itemId}/comment", 0L)
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void createCommentInvalidHeader() throws Exception {
        CommentRequestDto requestDto = new CommentRequestDto();
        requestDto.setText("great item");

        mvc.perform(post("/items/{itemId}/comment", 1L)
                        .header("X-Sharer-User-Id", 0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    private User createUser(Long id, String name) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(name + "@mail.com");
        return user;
    }

    private ItemRequest createRequest(Long id, User owner) {
        ItemRequest request = new ItemRequest();
        request.setId(id);
        request.setDescription("description");
        request.setRequestor(owner);
        request.setCreated(LocalDateTime.now());
        return request;
    }

    private ItemOwnerDto createItemOwnerDto(Long id, String name, Long ownerId) {
        User owner = createUser(ownerId, "owner");
        ItemRequest request = createRequest(5L, owner);

        BookingShortDto lastBooking = new BookingShortDto();
        lastBooking.setId(10L);
        lastBooking.setBookerId(20L);

        BookingShortDto nextBooking = new BookingShortDto();
        nextBooking.setId(11L);
        nextBooking.setBookerId(21L);

        CommentResponseDto comment = new CommentResponseDto();
        comment.setId(30L);
        comment.setText("comment");
        comment.setAuthorName("author");
        comment.setCreated(LocalDateTime.now());

        ItemOwnerDto dto = new ItemOwnerDto();
        dto.setId(id);
        dto.setName(name);
        dto.setDescription("description");
        dto.setAvailable(true);
        dto.setOwner(owner);
        dto.setRequest(request);
        dto.setLastBooking(lastBooking);
        dto.setNextBooking(nextBooking);
        dto.setComments(List.of(comment));
        return dto;
    }
}
