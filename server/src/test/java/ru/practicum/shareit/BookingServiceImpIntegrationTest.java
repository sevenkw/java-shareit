package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest(properties =
        "spring.datasource.url=jdbc:h2:mem:shareit-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class BookingServiceImpIntegrationTest {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private UserService userService;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void createBookingPositive() {
        User owner = createUser("owner", "owner@email.ru");
        User booker = createUser("booker", "booker@email.ru");
        Item item = createItem(owner, "drill", "powerful", true);

        BookingCreateRequest request = new BookingCreateRequest();
        request.setItemId(item.getId());
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        Booking result = bookingService.create(booker.getId(), request);

        assertThat(result.getId(), notNullValue());
        assertThat(result.getItem().getId(), equalTo(item.getId()));
        assertThat(result.getBooker().getId(), equalTo(booker.getId()));
        assertThat(result.getStatus(), equalTo(BookingStatus.WAITING));
    }

    @Test
    void createBookingInvalidUser() {
        User owner = createUser("owner", "owner@email.ru");
        Item item = createItem(owner, "drill", "powerful", true);

        BookingCreateRequest request = new BookingCreateRequest();
        request.setItemId(item.getId());
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        assertThrows(NotFoundException.class, () -> bookingService.create(999L, request));
    }

    @Test
    void createBookingInvalidItem() {
        User booker = createUser("booker", "booker@email.ru");

        BookingCreateRequest request = new BookingCreateRequest();
        request.setItemId(999L);
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        assertThrows(NotFoundException.class, () -> bookingService.create(booker.getId(), request));
    }

    @Test
    void createBookingUnavailableItem() {
        User owner = createUser("owner", "owner@email.ru");
        User booker = createUser("booker", "booker@email.ru");
        Item item = createItem(owner, "drill", "powerful", false);

        BookingCreateRequest request = new BookingCreateRequest();
        request.setItemId(item.getId());
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        assertThrows(ValidationException.class, () -> bookingService.create(booker.getId(), request));
    }

    @Test
    void createBookingStartNotAfterNow() {
        User owner = createUser("owner", "owner@email.ru");
        User booker = createUser("booker", "booker@email.ru");
        Item item = createItem(owner, "drill", "powerful", true);

        BookingCreateRequest request = new BookingCreateRequest();
        request.setItemId(item.getId());
        request.setStart(LocalDateTime.now().minusHours(1));
        request.setEnd(LocalDateTime.now().plusDays(1));

        assertThrows(ValidationException.class, () -> bookingService.create(booker.getId(), request));
    }

    @Test
    void createBookingByOwnerShouldThrow() {
        User owner = createUser("owner", "owner@email.ru");
        Item item = createItem(owner, "drill", "powerful", true);

        BookingCreateRequest request = new BookingCreateRequest();
        request.setItemId(item.getId());
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        assertThrows(ValidationException.class, () -> bookingService.create(owner.getId(), request));
    }

    @Test
    void createBookingEndBeforeStart() {
        User owner = createUser("owner", "owner@email.ru");
        User booker = createUser("booker", "booker@email.ru");
        Item item = createItem(owner, "drill", "powerful", true);

        BookingCreateRequest request = new BookingCreateRequest();
        request.setItemId(item.getId());
        request.setStart(LocalDateTime.now().plusDays(2));
        request.setEnd(LocalDateTime.now().plusDays(1));

        assertThrows(ValidationException.class, () -> bookingService.create(booker.getId(), request));
    }

    @Test
    void changeBookingApprovePositive() {
        User owner = createUser("owner", "owner@email.ru");
        User booker = createUser("booker", "booker@email.ru");
        Item item = createItem(owner, "drill", "powerful", true);
        Booking booking = createBooking(item, booker,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING);

        Booking result = bookingService.change(owner.getId(), booking.getId(), true);

        assertThat(result.getStatus(), equalTo(BookingStatus.APPROVED));
    }

    @Test
    void changeBookingRejectPositive() {
        User owner = createUser("owner", "owner@email.ru");
        User booker = createUser("booker", "booker@email.ru");
        Item item = createItem(owner, "drill", "powerful", true);
        Booking booking = createBooking(item, booker,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING);

        Booking result = bookingService.change(owner.getId(), booking.getId(), false);

        assertThat(result.getStatus(), equalTo(BookingStatus.REJECTED));
    }

    @Test
    void changeBookingInvalidBooking() {
        User owner = createUser("owner", "owner@email.ru");

        assertThrows(NotFoundException.class, () -> bookingService.change(owner.getId(), 999L, true));
    }

    @Test
    void changeBookingNonWaitingStatus() {
        User owner = createUser("owner", "owner@email.ru");
        User booker = createUser("booker", "booker@email.ru");
        Item item = createItem(owner, "drill", "powerful", true);
        Booking booking = createBooking(item, booker,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.APPROVED);

        assertThrows(ValidationException.class, () -> bookingService.change(owner.getId(), booking.getId(), true));
    }

    @Test
    void changeBookingByNonOwner() {
        User owner = createUser("owner", "owner@email.ru");
        User booker = createUser("booker", "booker@email.ru");
        Item item = createItem(owner, "drill", "powerful", true);
        Booking booking = createBooking(item, booker,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING);

        assertThrows(ValidationException.class, () -> bookingService.change(booker.getId(), booking.getId(), true));
    }

    @Test
    void getBookingByBookerPositive() {
        User owner = createUser("owner", "owner@email.ru");
        User booker = createUser("booker", "booker@email.ru");
        Item item = createItem(owner, "drill", "powerful", true);
        Booking booking = createBooking(item, booker,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING);

        Booking result = bookingService.getBooking(booker.getId(), booking.getId());

        assertThat(result.getId(), equalTo(booking.getId()));
        assertThat(result.getBooker().getId(), equalTo(booker.getId()));
    }

    @Test
    void getBookingByOwnerPositive() {
        User owner = createUser("owner", "owner@email.ru");
        User booker = createUser("booker", "booker@email.ru");
        Item item = createItem(owner, "drill", "powerful", true);
        Booking booking = createBooking(item, booker,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING);

        Booking result = bookingService.getBooking(owner.getId(), booking.getId());

        assertThat(result.getId(), equalTo(booking.getId()));
        assertThat(result.getItem().getOwner().getId(), equalTo(owner.getId()));
    }

    @Test
    void getBookingInvalidUser() {
        User owner = createUser("owner", "owner@email.ru");
        User booker = createUser("booker", "booker@email.ru");
        Item item = createItem(owner, "drill", "powerful", true);
        Booking booking = createBooking(item, booker,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING);

        assertThrows(NotFoundException.class, () -> bookingService.getBooking(999L, booking.getId()));
    }

    @Test
    void getBookingInvalidBooking() {
        User owner = createUser("owner", "owner@email.ru");

        assertThrows(NotFoundException.class, () -> bookingService.getBooking(owner.getId(), 999L));
    }

    @Test
    void getBookingAccessDenied() {
        User owner = createUser("owner", "owner@email.ru");
        User booker = createUser("booker", "booker@email.ru");
        User stranger = createUser("stranger", "stranger@email.ru");
        Item item = createItem(owner, "drill", "powerful", true);
        Booking booking = createBooking(item, booker,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING);

        assertThrows(ValidationException.class, () -> bookingService.getBooking(stranger.getId(), booking.getId()));
    }

    @Test
    void getBookingsAll() {
        User owner = createUser("owner", "owner@email.ru");
        User booker = createUser("booker", "booker@email.ru");
        Item item1 = createItem(owner, "drill", "powerful", true);
        Item item2 = createItem(owner, "saw", "sharp", true);

        createBooking(item1, booker, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), BookingStatus.WAITING);
        createBooking(item2, booker, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4), BookingStatus.REJECTED);

        List<Booking> result = bookingService.getBookings(booker.getId(), BookingState.ALL);

        assertThat(result, hasSize(2));
    }

    @Test
    void getBookingsPast() {
        User owner = createUser("owner", "owner@email.ru");
        User booker = createUser("booker", "booker@email.ru");
        Item item = createItem(owner, "drill", "powerful", true);

        createBooking(item, booker, LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(1), BookingStatus.APPROVED);

        List<Booking> result = bookingService.getBookings(booker.getId(), BookingState.PAST);

        assertThat(result, hasSize(1));
    }

    @Test
    void getBookingsFuture() {
        User owner = createUser("owner", "owner@email.ru");
        User booker = createUser("booker", "booker@email.ru");
        Item item = createItem(owner, "drill", "powerful", true);

        createBooking(item, booker, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4), BookingStatus.WAITING);

        List<Booking> result = bookingService.getBookings(booker.getId(), BookingState.FUTURE);

        assertThat(result, hasSize(1));
    }

    @Test
    void getBookingsWaiting() {
        User owner = createUser("owner", "owner@email.ru");
        User booker = createUser("booker", "booker@email.ru");
        Item item = createItem(owner, "drill", "powerful", true);

        createBooking(item, booker, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4), BookingStatus.WAITING);

        List<Booking> result = bookingService.getBookings(booker.getId(), BookingState.WAITING);

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getStatus(), equalTo(BookingStatus.WAITING));
    }

    @Test
    void getBookingsRejected() {
        User owner = createUser("owner", "owner@email.ru");
        User booker = createUser("booker", "booker@email.ru");
        Item item = createItem(owner, "drill", "powerful", true);

        createBooking(item, booker, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4), BookingStatus.REJECTED);

        List<Booking> result = bookingService.getBookings(booker.getId(), BookingState.REJECTED);

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getStatus(), equalTo(BookingStatus.REJECTED));
    }

    @Test
    void getBookingsInvalidUser() {
        assertThrows(NotFoundException.class, () -> bookingService.getBookings(999L, BookingState.ALL));
    }

    @Test
    void getOwnerBookingsAll() {
        User owner = createUser("owner", "owner@email.ru");
        User booker = createUser("booker", "booker@email.ru");
        Item item1 = createItem(owner, "drill", "powerful", true);
        Item item2 = createItem(owner, "saw", "sharp", true);

        createBooking(item1, booker, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), BookingStatus.WAITING);
        createBooking(item2, booker, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4), BookingStatus.REJECTED);

        List<Booking> result = bookingService.getOwnerBookings(owner.getId(), BookingState.ALL);

        assertThat(result, hasSize(2));
    }

    @Test
    void getOwnerBookingsCurrent() {
        User owner = createUser("owner", "owner@email.ru");
        User booker = createUser("booker", "booker@email.ru");
        Item item = createItem(owner, "drill", "powerful", true);

        createBooking(item, booker, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1), BookingStatus.APPROVED);

        List<Booking> result = bookingService.getOwnerBookings(owner.getId(), BookingState.CURRENT);

        assertThat(result, hasSize(1));
    }

    @Test
    void getOwnerBookingsPast() {
        User owner = createUser("owner", "owner@email.ru");
        User booker = createUser("booker", "booker@email.ru");
        Item item = createItem(owner, "drill", "powerful", true);

        createBooking(item, booker, LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(1), BookingStatus.APPROVED);

        List<Booking> result = bookingService.getOwnerBookings(owner.getId(), BookingState.PAST);

        assertThat(result, hasSize(1));
    }

    @Test
    void getOwnerBookingsFuture() {
        User owner = createUser("owner", "owner@email.ru");
        User booker = createUser("booker", "booker@email.ru");
        Item item = createItem(owner, "drill", "powerful", true);

        createBooking(item, booker, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4), BookingStatus.WAITING);

        List<Booking> result = bookingService.getOwnerBookings(owner.getId(), BookingState.FUTURE);

        assertThat(result, hasSize(1));
    }

    @Test
    void getOwnerBookingsWaiting() {
        User owner = createUser("owner", "owner@email.ru");
        User booker = createUser("booker", "booker@email.ru");
        Item item = createItem(owner, "drill", "powerful", true);

        createBooking(item, booker, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4), BookingStatus.WAITING);

        List<Booking> result = bookingService.getOwnerBookings(owner.getId(), BookingState.WAITING);

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getStatus(), equalTo(BookingStatus.WAITING));
    }

    @Test
    void getOwnerBookingsRejected() {
        User owner = createUser("owner", "owner@email.ru");
        User booker = createUser("booker", "booker@email.ru");
        Item item = createItem(owner, "drill", "powerful", true);

        createBooking(item, booker, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4), BookingStatus.REJECTED);

        List<Booking> result = bookingService.getOwnerBookings(owner.getId(), BookingState.REJECTED);

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getStatus(), equalTo(BookingStatus.REJECTED));
    }

    @Test
    void getOwnerBookingsInvalidUser() {
        assertThrows(NotFoundException.class, () -> bookingService.getOwnerBookings(999L, BookingState.ALL));
    }

    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return userService.createNewUser(user);
    }

    private Item createItem(User owner, String name, String description, boolean available) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setOwner(owner);
        return itemRepository.save(item);
    }

    private Booking createBooking(Item item, User booker, LocalDateTime start, LocalDateTime end, BookingStatus status) {
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setStatus(status);
        return bookingRepository.save(booking);
    }
}
