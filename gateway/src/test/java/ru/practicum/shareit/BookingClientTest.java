package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.BookingCreateRequest;
import ru.practicum.shareit.booking.BookingState;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BookingClientTest {

    @Test
    void getBookings() {
        TestBookingClient client = new TestBookingClient();

        ResponseEntity<Object> response = client.getBookings(2L, BookingState.ALL);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("?state={state}", client.path);
        assertEquals(2L, client.userId);
        assertEquals(Map.of("state", "ALL"), client.parameters);
    }

    @Test
    void createBooking() {
        TestBookingClient client = new TestBookingClient();
        BookingCreateRequest request = new BookingCreateRequest();
        request.setItemId(1L);
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        ResponseEntity<Object> response = client.createBooking(2L, request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("", client.path);
        assertEquals(2L, client.userId);
        assertEquals(request, client.body);
    }

    @Test
    void changeBooking() {
        TestBookingClient client = new TestBookingClient();

        ResponseEntity<Object> response = client.changeBooking(2L, 1L, true);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("/1?approved={approved}", client.path);
        assertEquals(2L, client.userId);
        assertEquals(Map.of("approved", true), client.parameters);
    }

    @Test
    void getOwnerBookings() {
        TestBookingClient client = new TestBookingClient();

        ResponseEntity<Object> response = client.getOwnerBookings(2L, BookingState.WAITING);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("/owner?state={state}", client.path);
        assertEquals(2L, client.userId);
        assertEquals(Map.of("state", "WAITING"), client.parameters);
    }

    private static class TestBookingClient extends BookingClient {
        private String path;
        private Long userId;
        private Map<String, Object> parameters;
        private Object body;

        private TestBookingClient() {
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
        protected <T> ResponseEntity<Object> patch(String path, Long userId, Map<String, Object> parameters, T body) {
            this.path = path;
            this.userId = userId;
            this.parameters = parameters;
            this.body = body;
            return ResponseEntity.ok().build();
        }
    }
}
