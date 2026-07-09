package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.ItemRequestClient;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.ItemRequestCreateDto;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
public class ItemRequestControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemRequestClient itemRequestClient;

    @Test
    void createItemRequest() throws Exception {
        ItemRequestCreateDto requestDto = new ItemRequestCreateDto();
        requestDto.setDescription("newItemRequest");

        ItemRequestResponse response = new ItemRequestResponse();
        response.setId(1L);
        response.setDescription("newItemRequest");

        when(itemRequestClient.createNewItemRequest(eq(1L), any(ItemRequestCreateDto.class)))
                .thenReturn(ResponseEntity.status(201).body(response));

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("newItemRequest")));

        verify(itemRequestClient).createNewItemRequest(eq(1L), any(ItemRequestCreateDto.class));
    }

    @Test
    void createInvalidItemRequest() throws Exception {
        ItemRequestCreateDto requestDto = new ItemRequestCreateDto();
        requestDto.setDescription(" ");

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemRequestClient);
    }

    @Test
    void invalidHeader() throws Exception {
        ItemRequestCreateDto requestDto = new ItemRequestCreateDto();
        requestDto.setDescription("newItemRequest");

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemRequestClient);
    }

    @Test
    void getItemRequestInvalidHeader() throws Exception {

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", -1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemRequestClient);
    }

    @Test
    void getItemRequestUser() throws Exception {
        ItemRequestResponse response = new ItemRequestResponse();
        response.setId(1L);
        response.setDescription("newItemRequest");

        when(itemRequestClient.getUserItemRequests(1L))
                .thenReturn(ResponseEntity.ok(List.of(response)));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].description", is("newItemRequest")));

        verify(itemRequestClient).getUserItemRequests(1L);
    }

    @Test
    void getItemRequestByIdInvalidRequestId() throws Exception {
        mvc.perform(get("/requests/{requestId}", 0L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemRequestClient);
    }

    @Test
    void getItemRequestByIdAndUserIdInvalidRequestId() throws Exception {
        mvc.perform(get("/requests/{requestId}", 1L)
                        .header("X-Sharer-User-Id", -10L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemRequestClient);
    }

    @Test
    void getItemRequestByIdAndUserId() throws Exception {
        ItemRequestResponse response = new ItemRequestResponse();
        response.setId(1L);
        response.setDescription("saw");

        when(itemRequestClient.getItemRequestByRequestIdAndUserId(1L, 1L))
                .thenReturn(ResponseEntity.ok(response));

        mvc.perform(get("/requests/{requestId}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("saw")));

        verify(itemRequestClient).getItemRequestByRequestIdAndUserId(1L, 1L);
    }

    @Test
    void getAllRequestsInvalidHeader() throws Exception {
        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 0L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemRequestClient);
    }

    @Test
    void getAllRequests() throws Exception {
        ItemRequestResponse first = new ItemRequestResponse();
        first.setId(1L);
        first.setDescription("newItemRequest");

        ItemRequestResponse second = new ItemRequestResponse();
        second.setId(2L);
        second.setDescription("newItemRequest2");

        when(itemRequestClient.getAllItemRequests(1L))
                .thenReturn(ResponseEntity.ok(List.of(first, second)));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[0].description", is("newItemRequest")))
                .andExpect(jsonPath("$[1].description", is("newItemRequest2")));

        verify(itemRequestClient).getAllItemRequests(1L);
    }

    private static class ItemRequestResponse {
        private Long id;
        private String description;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
