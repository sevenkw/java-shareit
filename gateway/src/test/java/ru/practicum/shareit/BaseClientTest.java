package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BaseClientTest {

    @Test
    void getWithoutUserId() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        TestBaseClient client = new TestBaseClient(restTemplate);

        when(restTemplate.exchange(eq("/items"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1L)));

        ResponseEntity<Object> response = client.getWithoutUserId("/items");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(restTemplate).exchange(eq("/items"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void getWithUserId() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        TestBaseClient client = new TestBaseClient(restTemplate);
        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);

        when(restTemplate.exchange(eq("/items"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1L)));

        ResponseEntity<Object> response = client.getWithUserId("/items", 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(restTemplate).exchange(eq("/items"), eq(HttpMethod.GET), captor.capture(), eq(Object.class));
        assertEquals("2", getHeaders(captor).getFirst("X-Sharer-User-Id"));
        assertEquals(MediaType.APPLICATION_JSON, getHeaders(captor).getContentType());
    }

    @Test
    void getWithParameters() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        TestBaseClient client = new TestBaseClient(restTemplate);

        when(restTemplate.exchange(eq("/items?text={text}"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class), eq(Map.of("text", "drill"))))
                .thenReturn(ResponseEntity.ok(Map.of()));

        ResponseEntity<Object> response = client.getWithParameters("/items?text={text}", 2L, Map.of("text", "drill"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(restTemplate).exchange(eq("/items?text={text}"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class), eq(Map.of("text", "drill")));
    }

    @Test
    void postWithoutUserId() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        TestBaseClient client = new TestBaseClient(restTemplate);
        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        Map<String, Object> body = Map.of("name", "drill");

        when(restTemplate.exchange(eq("/items"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", 1L)));

        ResponseEntity<Object> response = client.postWithoutUserId("/items", body);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(restTemplate).exchange(eq("/items"), eq(HttpMethod.POST), captor.capture(), eq(Object.class));
        assertEquals(body, captor.getValue().getBody());
        assertNull(getHeaders(captor).getFirst("X-Sharer-User-Id"));
    }

    @Test
    void postWithUserId() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        TestBaseClient client = new TestBaseClient(restTemplate);
        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        Map<String, Object> body = Map.of("name", "drill");

        when(restTemplate.exchange(eq("/items"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", 1L)));

        ResponseEntity<Object> response = client.postWithUserId("/items", 2L, body);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(restTemplate).exchange(eq("/items"), eq(HttpMethod.POST), captor.capture(), eq(Object.class));
        assertEquals(body, captor.getValue().getBody());
        assertEquals("2", getHeaders(captor).getFirst("X-Sharer-User-Id"));
    }

    @Test
    void postWithParameters() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        TestBaseClient client = new TestBaseClient(restTemplate);

        when(restTemplate.exchange(eq("/items?available={available}"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class), eq(Map.of("available", true))))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", 1L)));

        ResponseEntity<Object> response = client.postWithParameters("/items?available={available}", 2L, Map.of("available", true), Map.of("name", "drill"));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(restTemplate).exchange(eq("/items?available={available}"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class), eq(Map.of("available", true)));
    }

    @Test
    void putWithUserId() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        TestBaseClient client = new TestBaseClient(restTemplate);

        when(restTemplate.exchange(eq("/items/1"), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1L)));

        ResponseEntity<Object> response = client.putWithUserId("/items/1", 2L, Map.of("name", "updated"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(restTemplate).exchange(eq("/items/1"), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void putWithParameters() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        TestBaseClient client = new TestBaseClient(restTemplate);

        when(restTemplate.exchange(eq("/items/{id}"), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Object.class), eq(Map.of("id", 1L))))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1L)));

        ResponseEntity<Object> response = client.putWithParameters("/items/{id}", 2L, Map.of("id", 1L), Map.of("name", "updated"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(restTemplate).exchange(eq("/items/{id}"), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Object.class), eq(Map.of("id", 1L)));
    }

    @Test
    void patchWithoutUserId() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        TestBaseClient client = new TestBaseClient(restTemplate);

        when(restTemplate.exchange(eq("/items/1"), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1L)));

        ResponseEntity<Object> response = client.patchWithoutUserId("/items/1", Map.of("name", "updated"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(restTemplate).exchange(eq("/items/1"), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void patchWithUserIdWithoutBody() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        TestBaseClient client = new TestBaseClient(restTemplate);
        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);

        when(restTemplate.exchange(eq("/items/1"), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1L)));

        ResponseEntity<Object> response = client.patchWithUserId("/items/1", 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(restTemplate).exchange(eq("/items/1"), eq(HttpMethod.PATCH), captor.capture(), eq(Object.class));
        assertNull(captor.getValue().getBody());
        assertEquals("2", getHeaders(captor).getFirst("X-Sharer-User-Id"));
    }

    @Test
    void patchWithUserIdAndBody() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        TestBaseClient client = new TestBaseClient(restTemplate);

        when(restTemplate.exchange(eq("/items/1"), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1L)));

        ResponseEntity<Object> response = client.patchWithUserIdAndBody("/items/1", 2L, Map.of("name", "updated"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(restTemplate).exchange(eq("/items/1"), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void patchWithParameters() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        TestBaseClient client = new TestBaseClient(restTemplate);

        when(restTemplate.exchange(eq("/bookings/1?approved={approved}"), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class), eq(Map.of("approved", true))))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1L)));

        ResponseEntity<Object> response = client.patchWithParameters("/bookings/1?approved={approved}", 2L, Map.of("approved", true), null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(restTemplate).exchange(eq("/bookings/1?approved={approved}"), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class), eq(Map.of("approved", true)));
    }

    @Test
    void deleteWithoutUserId() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        TestBaseClient client = new TestBaseClient(restTemplate);

        when(restTemplate.exchange(eq("/users/1"), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.noContent().build());

        ResponseEntity<Object> response = client.deleteWithoutUserId("/users/1");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void deleteWithUserId() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        TestBaseClient client = new TestBaseClient(restTemplate);
        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);

        when(restTemplate.exchange(eq("/items/1"), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.noContent().build());

        ResponseEntity<Object> response = client.deleteWithUserId("/items/1", 2L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(restTemplate).exchange(eq("/items/1"), eq(HttpMethod.DELETE), captor.capture(), eq(Object.class));
        assertEquals("2", getHeaders(captor).getFirst("X-Sharer-User-Id"));
    }

    @Test
    void deleteWithParameters() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        TestBaseClient client = new TestBaseClient(restTemplate);

        when(restTemplate.exchange(eq("/items/{id}"), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Object.class), eq(Map.of("id", 1L))))
                .thenReturn(ResponseEntity.noContent().build());

        ResponseEntity<Object> response = client.deleteWithParameters("/items/{id}", 2L, Map.of("id", 1L));

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(restTemplate).exchange(eq("/items/{id}"), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Object.class), eq(Map.of("id", 1L)));
    }

    @Test
    void shouldReturnResponseBodyWhenServerReturnsError() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        TestBaseClient client = new TestBaseClient(restTemplate);

        when(restTemplate.exchange(eq("/items"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.BAD_REQUEST,
                        "Bad Request",
                        HttpHeaders.EMPTY,
                        "error".getBytes(StandardCharsets.UTF_8),
                        StandardCharsets.UTF_8
                ));

        ResponseEntity<Object> response = client.getWithoutUserId("/items");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertArrayEquals("error".getBytes(StandardCharsets.UTF_8), (byte[]) response.getBody());
    }

    @Test
    void shouldReturnResponseWithBodyWhenStatusIsNotSuccessful() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        TestBaseClient client = new TestBaseClient(restTemplate);

        when(restTemplate.exchange(eq("/items"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "not found")));

        ResponseEntity<Object> response = client.getWithoutUserId("/items");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(Map.of("error", "not found"), response.getBody());
    }

    @Test
    void shouldReturnResponseWithoutBodyWhenStatusIsNotSuccessful() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        TestBaseClient client = new TestBaseClient(restTemplate);

        when(restTemplate.exchange(eq("/items"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());

        ResponseEntity<Object> response = client.getWithoutUserId("/items");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.hasBody());
    }

    private HttpHeaders getHeaders(ArgumentCaptor<HttpEntity<?>> captor) {
        return captor.getValue().getHeaders();
    }

    private static class TestBaseClient extends BaseClient {
        private TestBaseClient(RestTemplate rest) {
            super(rest);
        }

        private ResponseEntity<Object> getWithoutUserId(String path) {
            return get(path);
        }

        private ResponseEntity<Object> getWithUserId(String path, long userId) {
            return get(path, userId);
        }

        private ResponseEntity<Object> getWithParameters(String path, Long userId, Map<String, Object> parameters) {
            return get(path, userId, parameters);
        }

        private <T> ResponseEntity<Object> postWithoutUserId(String path, T body) {
            return post(path, body);
        }

        private <T> ResponseEntity<Object> postWithUserId(String path, long userId, T body) {
            return post(path, userId, body);
        }

        private <T> ResponseEntity<Object> postWithParameters(String path, Long userId, Map<String, Object> parameters, T body) {
            return post(path, userId, parameters, body);
        }

        private <T> ResponseEntity<Object> putWithUserId(String path, long userId, T body) {
            return put(path, userId, body);
        }

        private <T> ResponseEntity<Object> putWithParameters(String path, long userId, Map<String, Object> parameters, T body) {
            return put(path, userId, parameters, body);
        }

        private <T> ResponseEntity<Object> patchWithoutUserId(String path, T body) {
            return patch(path, body);
        }

        private ResponseEntity<Object> patchWithUserId(String path, long userId) {
            return patch(path, userId);
        }

        private <T> ResponseEntity<Object> patchWithUserIdAndBody(String path, long userId, T body) {
            return patch(path, userId, body);
        }

        private <T> ResponseEntity<Object> patchWithParameters(String path, Long userId, Map<String, Object> parameters, T body) {
            return patch(path, userId, parameters, body);
        }

        private ResponseEntity<Object> deleteWithoutUserId(String path) {
            return delete(path);
        }

        private ResponseEntity<Object> deleteWithUserId(String path, long userId) {
            return delete(path, userId);
        }

        private ResponseEntity<Object> deleteWithParameters(String path, Long userId, Map<String, Object> parameters) {
            return delete(path, userId, parameters);
        }
    }
}
