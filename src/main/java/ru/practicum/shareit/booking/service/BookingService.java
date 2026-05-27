package ru.practicum.shareit.booking.service;


import ru.practicum.shareit.booking.dto.BookingCreateRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.List;

public interface BookingService {
    Booking create(Long userId, BookingCreateRequest request);

    Booking change(Long userId, Long bookingId, boolean approved);

    Booking getBooking(Long userId, Long bookingId);

    List<Booking> getBookings(Long userId, BookingState state);

    List<Booking> getOwnerBookings(Long userId, BookingState state);
}
