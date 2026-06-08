package ru.practicum.shareit.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    @Query("""
            select ir from ItemRequest ir
            where ir.requestor.id = :userId
            order by ir.created desc
            """)
    List<ItemRequest> getAllByRequestor(Long userId);

    @Query("""
            select ir from ItemRequest ir
            where ir.requestor.id <> :userId
            order by ir.created desc
            """)
    List<ItemRequest> getAllRequests(Long userId);
}
