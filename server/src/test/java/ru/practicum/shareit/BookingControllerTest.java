package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingCreateRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingService service;

    @Test
    void createBookingShouldReturnCreatedBooking() throws Exception {
        BookingCreateRequest request = new BookingCreateRequest();
        request.setItemId(1L);
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        Booking booking = createBooking(1L, 2L, 3L, BookingStatus.WAITING);

        when(service.create(eq(2L), any(BookingCreateRequest.class))).thenReturn(booking);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.item.id", is(2)))
                .andExpect(jsonPath("$.booker.id", is(3)))
                .andExpect(jsonPath("$.status", is("WAITING")));

        verify(service).create(eq(2L), any(BookingCreateRequest.class));
    }

    @Test
    void changeBookingShouldReturnUpdatedBooking() throws Exception {
        Booking booking = createBooking(1L, 2L, 3L, BookingStatus.APPROVED);

        when(service.change(2L, 1L, true)).thenReturn(booking);

        mvc.perform(patch("/bookings/{bookingId}", 1L)
                        .header("X-Sharer-User-Id", 2L)
                        .param("approved", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("APPROVED")));

        verify(service).change(2L, 1L, true);
    }

    @Test
    void getBookingShouldReturnBooking() throws Exception {
        Booking booking = createBooking(1L, 2L, 3L, BookingStatus.WAITING);

        when(service.getBooking(2L, 1L)).thenReturn(booking);

        mvc.perform(get("/bookings/{bookingId}", 1L)
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.item.id", is(2)))
                .andExpect(jsonPath("$.booker.id", is(3)))
                .andExpect(jsonPath("$.status", is("WAITING")));

        verify(service).getBooking(2L, 1L);
    }

    @Test
    void getBookingsShouldReturnBookingsList() throws Exception {
        Booking first = createBooking(1L, 2L, 3L, BookingStatus.WAITING);
        Booking second = createBooking(2L, 4L, 3L, BookingStatus.APPROVED);

        when(service.getBookings(2L, BookingState.ALL)).thenReturn(List.of(first, second));

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

        verify(service).getBookings(2L, BookingState.ALL);
    }

    @Test
    void getBookingsShouldReturnEmptyList() throws Exception {
        when(service.getBookings(2L, BookingState.CURRENT)).thenReturn(List.of());

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .param("state", "CURRENT")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(service).getBookings(2L, BookingState.CURRENT);
    }

    @Test
    void getBookingsWithoutStateShouldUseDefaultAll() throws Exception {
        when(service.getBookings(2L, BookingState.ALL)).thenReturn(List.of());

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(service).getBookings(2L, BookingState.ALL);
    }

    @Test
    void getOwnerBookingsShouldReturnBookingsList() throws Exception {
        Booking first = createBooking(1L, 2L, 3L, BookingStatus.WAITING);
        Booking second = createBooking(2L, 4L, 5L, BookingStatus.REJECTED);

        when(service.getOwnerBookings(2L, BookingState.ALL)).thenReturn(List.of(first, second));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 2L)
                        .param("state", "ALL")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[0].status", is("WAITING")))
                .andExpect(jsonPath("$[1].status", is("REJECTED")));

        verify(service).getOwnerBookings(2L, BookingState.ALL);
    }

    @Test
    void getOwnerBookingsShouldReturnEmptyList() throws Exception {
        when(service.getOwnerBookings(2L, BookingState.PAST)).thenReturn(List.of());

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 2L)
                        .param("state", "PAST")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(service).getOwnerBookings(2L, BookingState.PAST);
    }

    @Test
    void getOwnerBookingsWithoutStateShouldUseDefaultAll() throws Exception {
        when(service.getOwnerBookings(2L, BookingState.ALL)).thenReturn(List.of());

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(service).getOwnerBookings(2L, BookingState.ALL);
    }

    private Booking createBooking(Long id, Long itemId, Long bookerId, BookingStatus status) {
        User owner = new User();
        owner.setId(10L);
        owner.setName("owner");
        owner.setEmail("owner@mail.com");

        Item item = new Item();
        item.setId(itemId);
        item.setName("item");
        item.setDescription("description");
        item.setAvailable(true);
        item.setOwner(owner);

        User booker = new User();
        booker.setId(bookerId);
        booker.setName("booker");
        booker.setEmail("booker@mail.com");

        Booking booking = new Booking();
        booking.setId(id);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(status);
        return booking;
    }
}
