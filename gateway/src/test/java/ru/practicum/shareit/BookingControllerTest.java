package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.BookingCreateRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.booking.BookingState;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingClient client;

    @Test
    void createBookingShouldReturnCreatedBooking() throws Exception {
        BookingCreateRequest request = new BookingCreateRequest();
        request.setItemId(1L);
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        when(client.createBooking(eq(2L), any(BookingCreateRequest.class)))
                .thenReturn(ResponseEntity.status(201).body(createBookingResponse(1L, 1L, 2L, "WAITING")));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.item.id", is(1)))
                .andExpect(jsonPath("$.booker.id", is(2)))
                .andExpect(jsonPath("$.status", is("WAITING")));

        verify(client).createBooking(eq(2L), any(BookingCreateRequest.class));
    }

    @Test
    void createBookingWithoutItemIdShouldReturnBadRequest() throws Exception {
        BookingCreateRequest request = new BookingCreateRequest();
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(client);
    }

    @Test
    void createBookingWithoutStartShouldReturnBadRequest() throws Exception {
        BookingCreateRequest request = new BookingCreateRequest();
        request.setItemId(1L);
        request.setEnd(LocalDateTime.now().plusDays(2));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(client);
    }

    @Test
    void createBookingWithoutEndShouldReturnBadRequest() throws Exception {
        BookingCreateRequest request = new BookingCreateRequest();
        request.setItemId(1L);
        request.setStart(LocalDateTime.now().plusDays(1));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(client);
    }

    @Test
    void createBookingInvalidHeaderShouldReturnBadRequest() throws Exception {
        BookingCreateRequest request = new BookingCreateRequest();
        request.setItemId(1L);
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(client);
    }

    @Test
    void changeBookingInvalidHeaderShouldReturnBadRequest() throws Exception {
        mvc.perform(patch("/bookings/{bookingId}", 1L)
                        .header("X-Sharer-User-Id", -1L)
                        .param("approved", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(client);
    }

    @Test
    void changeBookingInvalidBookingIdShouldReturnBadRequest() throws Exception {
        mvc.perform(patch("/bookings/{bookingId}", 0L)
                        .header("X-Sharer-User-Id", 2L)
                        .param("approved", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(client);
    }

    @Test
    void changeBookingShouldReturnUpdatedBooking() throws Exception {
        when(client.changeBooking(2L, 1L, true))
                .thenReturn(ResponseEntity.ok(createBookingResponse(1L, 1L, 2L, "APPROVED")));

        mvc.perform(patch("/bookings/{bookingId}", 1L)
                        .header("X-Sharer-User-Id", 2L)
                        .param("approved", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("APPROVED")));

        verify(client).changeBooking(2L, 1L, true);
    }

    @Test
    void getBookingInvalidHeaderShouldReturnBadRequest() throws Exception {
        mvc.perform(get("/bookings/{bookingId}", 1L)
                        .header("X-Sharer-User-Id", 0L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(client);
    }

    @Test
    void getBookingInvalidBookingIdShouldReturnBadRequest() throws Exception {
        mvc.perform(get("/bookings/{bookingId}", -1L)
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(client);
    }

    @Test
    void getBookingShouldReturnBooking() throws Exception {
        when(client.getBooking(2L, 1L)).thenReturn(ResponseEntity.ok(createBookingResponse(1L, 1L, 2L, "WAITING")));

        mvc.perform(get("/bookings/{bookingId}", 1L)
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.item.id", is(1)))
                .andExpect(jsonPath("$.booker.id", is(2)))
                .andExpect(jsonPath("$.status", is("WAITING")));

        verify(client).getBooking(2L, 1L);
    }

    @Test
    void getBookingsInvalidHeaderShouldReturnBadRequest() throws Exception {
        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", -1L)
                        .param("state", "ALL")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(client);
    }

    @Test
    void getBookingsInvalidStateShouldReturnBadRequest() throws Exception {
        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .param("state", "INVALID")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(client);
    }

    @Test
    void getBookingsShouldReturnBookingsList() throws Exception {
        when(client.getBookings(2L, BookingState.ALL))
                .thenReturn(ResponseEntity.ok(List.of(
                        createBookingResponse(1L, 1L, 2L, "WAITING"),
                        createBookingResponse(2L, 3L, 2L, "APPROVED")
                )));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .param("state", "ALL")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].status", is("WAITING")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].status", is("APPROVED")));

        verify(client).getBookings(2L, BookingState.ALL);
    }

    @Test
    void getBookingsWithoutStateShouldUseDefaultAll() throws Exception {
        when(client.getBookings(2L, BookingState.ALL))
                .thenReturn(ResponseEntity.ok(List.of()));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(client).getBookings(2L, BookingState.ALL);
    }

    @Test
    void getOwnerBookingsInvalidHeaderShouldReturnBadRequest() throws Exception {
        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 0L)
                        .param("state", "ALL")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(client);
    }

    @Test
    void getOwnerBookingsInvalidStateShouldReturnBadRequest() throws Exception {
        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 2L)
                        .param("state", "UNKNOWN")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(client);
    }

    @Test
    void getOwnerBookingsShouldReturnBookingsList() throws Exception {
        when(client.getOwnerBookings(2L, BookingState.ALL))
                .thenReturn(ResponseEntity.ok(List.of(
                        createBookingResponse(1L, 1L, 2L, "WAITING"),
                        createBookingResponse(2L, 3L, 4L, "REJECTED")
                )));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 2L)
                        .param("state", "ALL")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].status", is("WAITING")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].status", is("REJECTED")));

        verify(client).getOwnerBookings(2L, BookingState.ALL);
    }

    @Test
    void getOwnerBookingsWithoutStateShouldUseDefaultAll() throws Exception {
        when(client.getOwnerBookings(2L, BookingState.ALL))
                .thenReturn(ResponseEntity.ok(List.of()));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(client).getOwnerBookings(2L, BookingState.ALL);
    }

    private BookingResponse createBookingResponse(Long id, Long itemId, Long bookerId, String statusValue) {
        BookingResponse response = new BookingResponse();
        response.setId(id);
        response.setItem(new BookingItem(itemId));
        response.setBooker(new BookingUser(bookerId));
        response.setStatus(statusValue);
        return response;
    }

    private static class BookingResponse {
        private Long id;
        private BookingItem item;
        private BookingUser booker;
        private String status;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public BookingItem getItem() {
            return item;
        }

        public void setItem(BookingItem item) {
            this.item = item;
        }

        public BookingUser getBooker() {
            return booker;
        }

        public void setBooker(BookingUser booker) {
            this.booker = booker;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    private static class BookingItem {
        private Long id;

        private BookingItem(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }
    }

    private static class BookingUser {
        private Long id;

        private BookingUser(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }
    }
}
