package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.request.ItemRequestClient;
import ru.practicum.shareit.request.ItemRequestCreateDto;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ItemRequestClientTest {

    @Test
    void createNewItemRequest() {
        TestItemRequestClient client = new TestItemRequestClient();
        ItemRequestCreateDto requestDto = new ItemRequestCreateDto();
        requestDto.setDescription("need drill");

        ResponseEntity<Object> response = client.createNewItemRequest(1L, requestDto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("", client.path);
        assertEquals(1L, client.userId);
        assertEquals(requestDto, client.body);
    }

    @Test
    void getUserItemRequests() {
        TestItemRequestClient client = new TestItemRequestClient();

        ResponseEntity<Object> response = client.getUserItemRequests(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("", client.path);
        assertEquals(1L, client.userId);
    }

    @Test
    void getItemRequestByRequestIdAndUserId() {
        TestItemRequestClient client = new TestItemRequestClient();

        ResponseEntity<Object> response = client.getItemRequestByRequestIdAndUserId(1L, 2L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("/2", client.path);
        assertEquals(1L, client.userId);
    }

    @Test
    void getAllItemRequests() {
        TestItemRequestClient client = new TestItemRequestClient();

        ResponseEntity<Object> response = client.getAllItemRequests(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("/all", client.path);
        assertEquals(1L, client.userId);
    }

    private static class TestItemRequestClient extends ItemRequestClient {
        private String path;
        private Long userId;
        private Object body;

        private TestItemRequestClient() {
            super("http://localhost", new RestTemplateBuilder());
        }

        @Override
        protected <T> ResponseEntity<Object> post(String path, long userId, T body) {
            this.path = path;
            this.userId = userId;
            this.body = body;
            return ResponseEntity.ok().build();
        }

        @Override
        protected ResponseEntity<Object> get(String path, long userId) {
            this.path = path;
            this.userId = userId;
            return ResponseEntity.ok().build();
        }
    }
}
