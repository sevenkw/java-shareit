package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBooker_IdOrderByStartDesc(Long userId); //all

    List<Booking> findAllByBooker_IdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(Long userId, LocalDateTime now1, LocalDateTime now2); // current

    List<Booking> findAllByBooker_IdAndEndBeforeOrderByStartDesc(Long userId, LocalDateTime now); //past

    List<Booking> findAllByBooker_IdAndStartAfterOrderByStartDesc(Long userId, LocalDateTime now); // future

    List<Booking> findAllByBooker_IdAndStatusOrderByStartDesc(Long userId, BookingStatus status);

    @Query("""
            select b
            from Booking b
            join b.item i
            where i.owner.id = :userId
            order by b.start desc
            """)
    List<Booking> findByOwner(Long userId);

    @Query("""
            select b
            from Booking b
            join b.item i
            where i.owner.id = :userId
            and
            (b.start <= :now and b.end >= :now)
            order by b.start desc
            """)
    List<Booking> getCurrent(Long userId, LocalDateTime now);

    @Query("""
            select b
            from Booking b
            join b.item i
            where i.owner.id = :userId
            and
            b.end < :now
            order by b.start desc
            """)
    List<Booking> getPast(Long userId, LocalDateTime now);

    List<Booking> findAllByItem_Owner_IdAndStartAfterOrderByStartDesc(Long userId, LocalDateTime now);

    List<Booking> findAllByItem_Owner_IdAndStatusOrderByStartDesc(Long userId, BookingStatus status);

    Optional<Booking> findFirstByItem_IdAndStartAfterOrderByStartAsc(Long itemId, LocalDateTime now);

    Optional<Booking> findFirstByItem_IdAndStartLessThanEqualOrderByStartDesc(Long itemId, LocalDateTime now);

    boolean existsByBooker_IdAndItem_IdAndStatusAndEndBefore(
            Long bookerId,
            Long itemId,
            BookingStatus status,
            LocalDateTime now
    );

    List<Booking> findAllByItem_IdInAndStartAfterOrderByStartAsc(List<Long> itemIds, LocalDateTime now); // future

    List<Booking> findAllByItem_IdInAndStartLessThanEqualOrderByStartDesc(List<Long> itemIds, LocalDateTime now); //last
}
