package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.CommentRequestDto;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemDto;

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
    private ItemClient itemClient;

    @Test
    void createItem() throws Exception {
        ItemDto requestDto = new ItemDto();
        requestDto.setName("test");
        requestDto.setDescription("description");
        requestDto.setAvailable(true);

        ItemDto responseDto = new ItemDto();
        responseDto.setId(1L);
        responseDto.setName("test");
        responseDto.setDescription("description");
        responseDto.setAvailable(true);

        when(itemClient.createItem(eq(2L), any(ItemDto.class)))
                .thenReturn(ResponseEntity.status(201).body(responseDto));

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("test")))
                .andExpect(jsonPath("$.description", is("description")))
                .andExpect(jsonPath("$.available", is(true)));

        verify(itemClient).createItem(eq(2L), any(ItemDto.class));
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

        verifyNoInteractions(itemClient);
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

        verifyNoInteractions(itemClient);
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

        verifyNoInteractions(itemClient);
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

        verifyNoInteractions(itemClient);
    }

    @Test
    void getItemsInvalidHeader() throws Exception {
        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", -1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    void getItems() throws Exception {
        ItemDto first = new ItemDto();
        first.setId(1L);
        first.setName("drill");
        first.setDescription("description");
        first.setAvailable(true);

        ItemDto second = new ItemDto();
        second.setId(2L);
        second.setName("saw");
        second.setDescription("description");
        second.setAvailable(true);

        when(itemClient.getItemsByUserId(2L)).thenReturn(ResponseEntity.ok(List.of(first, second)));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("drill")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("saw")));

        verify(itemClient).getItemsByUserId(2L);
    }

    @Test
    void getItemByIdInvalidItemId() throws Exception {
        mvc.perform(get("/items/{id}", 0L)
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    void getItemByIdInvalidHeader() throws Exception {
        mvc.perform(get("/items/{id}", 1L)
                        .header("X-Sharer-User-Id", 0L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    void getItemById() throws Exception {
        ItemDto responseDto = new ItemDto();
        responseDto.setId(1L);
        responseDto.setName("drill");
        responseDto.setDescription("description");
        responseDto.setAvailable(true);

        when(itemClient.getItemByUserIdAndItemId(2L, 1L)).thenReturn(ResponseEntity.ok(responseDto));

        mvc.perform(get("/items/{id}", 1L)
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("drill")))
                .andExpect(jsonPath("$.description", is("description")))
                .andExpect(jsonPath("$.available", is(true)));

        verify(itemClient).getItemByUserIdAndItemId(2L, 1L);
    }

    @Test
    void searchItems() throws Exception {
        ItemDto responseDto = new ItemDto();
        responseDto.setId(1L);
        responseDto.setName("drill");
        responseDto.setDescription("powerful");
        responseDto.setAvailable(true);

        when(itemClient.searchItems("drill")).thenReturn(ResponseEntity.ok(List.of(responseDto)));

        mvc.perform(get("/items/search")
                        .param("text", "drill")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("drill")));

        verify(itemClient).searchItems("drill");
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

        verifyNoInteractions(itemClient);
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

        verifyNoInteractions(itemClient);
    }

    @Test
    void updateItem() throws Exception {
        ItemDto requestDto = new ItemDto();
        requestDto.setName("updated");

        ItemDto responseDto = new ItemDto();
        responseDto.setId(1L);
        responseDto.setName("updated");
        responseDto.setDescription("description");
        responseDto.setAvailable(true);

        when(itemClient.updateItem(eq(2L), eq(1L), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok(responseDto));

        mvc.perform(patch("/items/{id}", 1L)
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("updated")));

        verify(itemClient).updateItem(eq(2L), eq(1L), any(ItemDto.class));
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

        verifyNoInteractions(itemClient);
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

        verifyNoInteractions(itemClient);
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

        verifyNoInteractions(itemClient);
    }

    @Test
    void createComment() throws Exception {
        CommentRequestDto requestDto = new CommentRequestDto();
        requestDto.setText("great item");

        CommentResponse response = new CommentResponse();
        response.setText("great item");

        when(itemClient.addNewComment(eq(1L), eq(2L), any(CommentRequestDto.class)))
                .thenReturn(ResponseEntity.status(201).body(response));

        mvc.perform(post("/items/{itemId}/comment", 1L)
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text", is("great item")));

        verify(itemClient).addNewComment(eq(1L), eq(2L), any(CommentRequestDto.class));
    }

    private static class CommentResponse {
        private String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
