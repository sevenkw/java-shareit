package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByItem_IdOrderByCreatedAsc(Long itemId);

    List<Comment> findByItem_IdInOrderByCreatedAsc(List<Long> itemIds);
}
