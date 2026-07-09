package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.item.CommentRequestDto;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.ItemDto;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ItemClientTest {

    @Test
    void getItemsByUserId() {
        TestItemClient client = new TestItemClient();

        ResponseEntity<Object> response = client.getItemsByUserId(2L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("", client.path);
        assertEquals(2L, client.userId);
    }

    @Test
    void getItemByUserIdAndItemId() {
        TestItemClient client = new TestItemClient();

        ResponseEntity<Object> response = client.getItemByUserIdAndItemId(2L, 1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("/1", client.path);
        assertEquals(2L, client.userId);
    }

    @Test
    void createItem() {
        TestItemClient client = new TestItemClient();
        ItemDto itemDto = new ItemDto();
        itemDto.setName("drill");
        itemDto.setDescription("powerful");
        itemDto.setAvailable(true);

        ResponseEntity<Object> response = client.createItem(2L, itemDto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("", client.path);
        assertEquals(2L, client.userId);
        assertEquals(itemDto, client.body);
    }

    @Test
    void updateItem() {
        TestItemClient client = new TestItemClient();
        ItemDto itemDto = new ItemDto();
        itemDto.setName("updated");

        ResponseEntity<Object> response = client.updateItem(2L, 1L, itemDto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("/1", client.path);
        assertEquals(2L, client.userId);
        assertEquals(itemDto, client.body);
    }

    @Test
    void addNewComment() {
        TestItemClient client = new TestItemClient();
        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setText("great item");

        ResponseEntity<Object> response = client.addNewComment(1L, 2L, commentRequestDto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("/1/comment", client.path);
        assertEquals(2L, client.userId);
        assertEquals(commentRequestDto, client.body);
    }

    private static class TestItemClient extends ItemClient {
        private String path;
        private Long userId;
        private Map<String, Object> parameters;
        private Object body;

        private TestItemClient() {
            super("http://localhost", new RestTemplateBuilder());
        }

        @Override
        protected ResponseEntity<Object> get(String path, Long userId, Map<String, Object> parameters) {
            this.path = path;
            this.userId = userId;
            this.parameters = parameters;
            return ResponseEntity.ok().build();
        }

        @Override
        protected ResponseEntity<Object> get(String path, long userId) {
            this.path = path;
            this.userId = userId;
            return ResponseEntity.ok().build();
        }

        @Override
        protected <T> ResponseEntity<Object> post(String path, long userId, T body) {
            this.path = path;
            this.userId = userId;
            this.body = body;
            return ResponseEntity.ok().build();
        }

        @Override
        protected <T> ResponseEntity<Object> patch(String path, long userId, T body) {
            this.path = path;
            this.userId = userId;
            this.body = body;
            return ResponseEntity.ok().build();
        }
    }
}
