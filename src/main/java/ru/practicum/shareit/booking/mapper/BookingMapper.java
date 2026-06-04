package ru.practicum.shareit.booking.mapper;

import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.model.Booking;

@NoArgsConstructor
public class BookingMapper {

    public static BookingResponse toBookingResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setStart(booking.getStart());
        response.setEnd(booking.getEnd());
        response.setItem(booking.getItem());
        response.setBooker(booking.getBooker());
        response.setStatus(booking.getStatus());
        return response;
    }
}
