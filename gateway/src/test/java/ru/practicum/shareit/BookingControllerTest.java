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

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingClient client;

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
}
