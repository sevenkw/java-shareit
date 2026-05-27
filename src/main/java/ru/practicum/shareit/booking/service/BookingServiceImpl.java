package ru.practicum.shareit.booking.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;

    public BookingServiceImpl(UserRepository userRepository, ItemRepository itemRepository, BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    @Transactional
    public Booking create(Long userId, BookingCreateRequest request) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь должна быть доступна для бронирования");
        }

        if (!request.getStart().isAfter(LocalDateTime.now())) {
            throw new ValidationException("Дата начала бронирования должна быть позже текущего времени");
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new ValidationException("Владелец вещи не может подать заявку на ее бронирование");
        }

        if (!request.getEnd().isAfter(request.getStart())) {
            throw new ValidationException("Конец бронирования не может быть до начала бронирования");
        }

        Booking booking = new Booking();
        booking.setStart(request.getStart());
        booking.setEnd(request.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        return bookingRepository.save(booking);

    }

    @Override
    @Transactional
    public Booking change(Long userId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Статус броинрования должен быть WAITING");
        }

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ValidationException("Статус броинрования может изменить только владелец");
        }

        if (!approved) {
            booking.setStatus(BookingStatus.REJECTED);
        } else {
            booking.setStatus(BookingStatus.APPROVED);
        }

        return bookingRepository.save(booking);
    }

    @Override
    public Booking getBooking(Long userId, Long bookingId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new ValidationException("Доступ к просмотру есть только у создателя вещи или у автора бронирования");
        }

        return booking;
    }

    @Override
    public List<Booking> getBookings(Long userId, BookingState state) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = new ArrayList<>();
        switch (state) {
            case ALL -> bookings = bookingRepository.findAllByBooker_IdOrderByStartDesc(userId);
            case CURRENT -> bookings = bookingRepository
                    .findAllByBooker_IdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(userId, now, now);
            case PAST -> bookings = bookingRepository.findAllByBooker_IdAndEndBeforeOrderByStartDesc(userId, now);
            case FUTURE -> bookings = bookingRepository.findAllByBooker_IdAndStartAfterOrderByStartDesc(userId, now);
            case REJECTED ->
                    bookings = bookingRepository.findAllByBooker_IdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
            case WAITING ->
                    bookings = bookingRepository.findAllByBooker_IdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
        }
        return bookings;
    }

    @Override
    public List<Booking> getOwnerBookings(Long userId, BookingState state) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = new ArrayList<>();
        switch (state) {
            case ALL -> bookings = bookingRepository.findByOwner(userId);
            case CURRENT -> bookings = bookingRepository.getCurrent(userId, now);
            case PAST -> bookings = bookingRepository.getPast(userId, now);
            case FUTURE ->
                    bookings = bookingRepository.findAllByItem_Owner_IdAndStartAfterOrderByStartDesc(userId, now);
            case REJECTED ->
                    bookings = bookingRepository.findAllByItem_Owner_IdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
            case WAITING ->
                    bookings = bookingRepository.findAllByItem_Owner_IdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
        }
        return bookings;

    }


}
