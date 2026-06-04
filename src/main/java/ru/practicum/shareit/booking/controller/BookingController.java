package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateRequest;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@Validated
public class BookingController {
    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                         @Valid @RequestBody BookingCreateRequest request) {

        Booking created = bookingService.create(userId, request);
        return BookingMapper.toBookingResponse(created);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponse changeBooking(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                         @PathVariable @Positive Long bookingId,
                                         @RequestParam("approved") boolean approved) {

        Booking changed = bookingService.change(userId, bookingId, approved);
        return BookingMapper.toBookingResponse(changed);
    }

    @GetMapping("/{bookingId}")
    public BookingResponse getBooking(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                      @PathVariable("bookingId") @Positive Long bookingId) {

        Booking booking = bookingService.getBooking(userId, bookingId);
        return BookingMapper.toBookingResponse(booking);
    }

    @GetMapping
    public List<BookingResponse> getBookings(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                             @RequestParam(defaultValue = "ALL") BookingState state) {

        List<Booking> bookings = bookingService.getBookings(userId, state);
        List<BookingResponse> bookingResponses = new ArrayList<>();

        for (Booking booking : bookings) {
            bookingResponses.add(BookingMapper.toBookingResponse(booking));
        }
        return bookingResponses;
    }

    @GetMapping("/owner")
    public List<BookingResponse> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                                  @RequestParam(defaultValue = "ALL") BookingState state) {

        List<Booking> bookings = bookingService.getOwnerBookings(userId, state);
        List<BookingResponse> bookingResponses = new ArrayList<>();

        for (Booking booking : bookings) {
            bookingResponses.add(BookingMapper.toBookingResponse(booking));
        }
        return bookingResponses;
    }

}
