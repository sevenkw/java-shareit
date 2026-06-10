package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.BookingState;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BookingStateTest {

    @Test
    void fromShouldReturnStateIgnoringCase() {
        assertTrue(BookingState.from("current").isPresent());
        assertEquals(BookingState.CURRENT, BookingState.from("current").orElseThrow());
    }

    @Test
    void fromShouldReturnEmptyForUnknownState() {
        assertTrue(BookingState.from("unknown").isEmpty());
    }
}
