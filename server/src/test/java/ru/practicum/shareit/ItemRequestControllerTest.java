package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
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
    private ItemRequestService service;

    @Test
    void createItemRequest() throws Exception {
        ItemRequestCreateDto requestDto = new ItemRequestCreateDto();
        requestDto.setDescription("newItemRequest");

        ItemRequestResponseDto responseDto = new ItemRequestResponseDto();
        responseDto.setId(1L);
        responseDto.setDescription("newItemRequest");
        responseDto.setCreated(LocalDateTime.now());
        responseDto.setItems(List.of());

        when(service.createNewItemRequest(any(), any())).thenReturn(responseDto);

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("newItemRequest")))
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    void getItemRequestUser() throws Exception {

        ItemRequestDto firstDto = new ItemRequestDto();
        firstDto.setId(1L);
        firstDto.setName("firstItemRequest");
        firstDto.setOwnerId(2L);

        ItemRequestDto secondDto = new ItemRequestDto();
        secondDto.setId(2L);
        secondDto.setName("secondItemRequest");
        secondDto.setOwnerId(3L);

        ItemRequestResponseDto responseDto = new ItemRequestResponseDto();
        responseDto.setId(1L);
        responseDto.setDescription("newItemRequest");
        responseDto.setCreated(LocalDateTime.now());
        responseDto.setItems(List.of(firstDto, secondDto));

        when(service.getUserItemRequests(any()))
                .thenReturn(List.of(responseDto));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].description", is("newItemRequest")))
                .andExpect(jsonPath("$[0].items", hasSize(2)))
                .andExpect(jsonPath("$[0].items[0].id", is(1)))
                .andExpect(jsonPath("$[0].items[0].name", is("firstItemRequest")))
                .andExpect(jsonPath("$[0].items[0].ownerId", is(2)))
                .andExpect(jsonPath("$[0].items[1].id", is(2)))
                .andExpect(jsonPath("$[0].items[1].name", is("secondItemRequest")))
                .andExpect(jsonPath("$[0].items[1].ownerId", is(3)));

    }

    @Test
    void getUserItemRequests_shouldReturnRequestWithEmptyItems() throws Exception {
        ItemRequestResponseDto responseDto = new ItemRequestResponseDto();
        responseDto.setId(1L);
        responseDto.setDescription("newItemRequest");
        responseDto.setCreated(LocalDateTime.now());
        responseDto.setItems(List.of());

        when(service.getUserItemRequests(any()))
                .thenReturn(List.of(responseDto));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].items", hasSize(0)));
    }

    @Test
    void getEmptyListItemRequests() throws Exception {
        when(service.getUserItemRequests(any()))
                .thenReturn(List.of());

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getItemRequestByRequestIdAndUserId() throws Exception {
        ItemRequestResponseDto responseDto = new ItemRequestResponseDto();
        responseDto.setId(1L);
        responseDto.setDescription("saw");
        responseDto.setCreated(LocalDateTime.now());
        responseDto.setItems(List.of());

        when(service.getItemRequestByRequestIdAndUserId(1L, 1L))
                .thenReturn(responseDto);

        mvc.perform(get("/requests/{requestId}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("saw")))
                .andExpect(jsonPath("$.items", hasSize(0)));

        verify(service, times(1))
                .getItemRequestByRequestIdAndUserId(1L, 1L);

    }

    @Test
    void getAllRequestsSuccess() throws Exception {
        ItemRequestDto firstDto = new ItemRequestDto();
        firstDto.setId(1L);
        firstDto.setName("firstItemRequest");
        firstDto.setOwnerId(2L);

        ItemRequestDto secondDto = new ItemRequestDto();
        secondDto.setId(2L);
        secondDto.setName("secondItemRequest");
        secondDto.setOwnerId(3L);

        ItemRequestResponseDto responseDto = new ItemRequestResponseDto();
        responseDto.setId(1L);
        responseDto.setDescription("newItemRequest");
        responseDto.setCreated(LocalDateTime.now());
        responseDto.setItems(List.of(firstDto, secondDto));

        ItemRequestResponseDto responseDto2 = new ItemRequestResponseDto();
        responseDto2.setId(3L);
        responseDto2.setDescription("newItemRequest2");
        responseDto2.setCreated(LocalDateTime.now());
        responseDto2.setItems(List.of(firstDto, secondDto));

        when(service.getAllItemRequests(1L))
                .thenReturn(List.of(responseDto, responseDto2));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(3)))
                .andExpect(jsonPath("$[0].description", is("newItemRequest")))
                .andExpect(jsonPath("$[1].description", is("newItemRequest2")))
                .andExpect(jsonPath("$[0].items", hasSize(2)));

        verify(service, times(1)).getAllItemRequests(1L);
    }

    @Test
    void getAllRequestsEmptyList() throws Exception {
        when(service.getAllItemRequests(1L))
                .thenReturn(List.of());

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(service, times(1)).getAllItemRequests(1L);
    }

}
