package ru.practicum.shareit.item.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository, BookingRepository bookingRepository, CommentRepository commentRepository, ItemRequestRepository itemRequestRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
        this.itemRequestRepository = itemRequestRepository;
    }

    @Override
    public List<ItemOwnerDto> getAllByOwner(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }

        LocalDateTime now = LocalDateTime.now();

        List<Item> items = itemRepository.findAllByOwnerId(userId);
        if (items.isEmpty()) {
            return List.of();
        }

        List<Long> itemIds = new ArrayList<>();
        for (Item item : items) {
            itemIds.add(item.getId());
        }

        List<Booking> futureBookings = bookingRepository.findAllByItem_IdInAndStartAfterOrderByStartAsc(itemIds, now);
        List<Booking> pastOrCurrentBookings = bookingRepository.findAllByItem_IdInAndStartLessThanEqualOrderByStartDesc(itemIds, now);

        Map<Long, BookingShortDto> nextByItemId = new HashMap<>();
        for (Booking booking : futureBookings) {
            Long itemId = booking.getItem().getId();
            if (!nextByItemId.containsKey(itemId)) {
                BookingShortDto dto = new BookingShortDto();
                dto.setId(booking.getId());
                dto.setBookerId(booking.getBooker().getId());
                nextByItemId.put(itemId, dto);
            }
        }

        Map<Long, BookingShortDto> lastByItemId = new HashMap<>();
        for (Booking booking : pastOrCurrentBookings) {
            Long itemId = booking.getItem().getId();
            if (!lastByItemId.containsKey(itemId)) {
                BookingShortDto dto = new BookingShortDto();
                dto.setId(booking.getId());
                dto.setBookerId(booking.getBooker().getId());
                lastByItemId.put(itemId, dto);
            }
        }

        Map<Long, List<CommentResponseDto>> commentsByItemId = mapCommentsByItemIds(itemIds);
        List<ItemOwnerDto> ownerDtos = new ArrayList<>();

        for (Item item : items) {
            ItemOwnerDto itemOwnerDto = new ItemOwnerDto();
            Long itemId = item.getId();

            itemOwnerDto.setId(itemId);
            itemOwnerDto.setName(item.getName());
            itemOwnerDto.setDescription(item.getDescription());
            itemOwnerDto.setAvailable(item.getAvailable());
            itemOwnerDto.setOwner(item.getOwner());
            itemOwnerDto.setRequest(item.getRequest());
            itemOwnerDto.setNextBooking(nextByItemId.get(itemId));
            itemOwnerDto.setLastBooking(lastByItemId.get(itemId));
            itemOwnerDto.setComments(commentsByItemId.getOrDefault(itemId, List.of()));

            ownerDtos.add(itemOwnerDto);
        }

        return ownerDtos;
    }

    @Override
    public Item getItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь с id = " + id + " не найдена"));
    }

    @Override
    @Transactional
    public ItemDto createNewItem(Long userId, ItemDto itemDto) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);

        if (itemDto.getRequestId() == null) {
            var saved = itemRepository.save(item);
            return ItemMapper.toItemDto(saved);
        }

        ItemRequest request = itemRequestRepository.findById(itemDto.getRequestId())
                .orElseThrow(() -> new NotFoundException("Запрос с id = " + itemDto.getRequestId() + " не найден"));
        item.setRequest(request);

        var saved = itemRepository.save(item);
        return ItemMapper.toItemDto(saved);
    }

    @Override
    @Transactional
    public Item updateItem(Long userId, Long itemId, Item item) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        Item oldItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id = " + itemId + " не найдена"));

        if (!oldItem.getOwner().getId().equals(userId)) {
            throw new ValidationException("Редактировать вещь может только ее владелец");
        }

        if (item.getName() != null) {
            oldItem.setName(item.getName());
        }

        if (item.getDescription() != null) {
            oldItem.setDescription(item.getDescription());
        }

        if (item.getAvailable() != null) {
            oldItem.setAvailable(item.getAvailable());
        }

        return itemRepository.save(oldItem);
    }

    @Override
    public List<Item> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        return itemRepository.search(text);
    }

    @Override
    public ItemOwnerDto getItemByIdForUser(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id = " + itemId + " не найдена"));

        LocalDateTime now = LocalDateTime.now();
        ItemOwnerDto itemOwnerDto = new ItemOwnerDto();

        if (item.getOwner().getId().equals(userId)) {
            Optional<Booking> nextBooking = bookingRepository.findFirstByItem_IdAndStartAfterOrderByStartAsc(item.getId(), now);
            Optional<Booking> lastBooking = bookingRepository.findFirstByItem_IdAndStartLessThanEqualOrderByStartDesc(item.getId(), now);

            BookingShortDto nextShortDto = null;
            if (nextBooking.isPresent()) {
                nextShortDto = new BookingShortDto();
                nextShortDto.setId(nextBooking.get().getId());
                nextShortDto.setBookerId(nextBooking.get().getBooker().getId());
            }

            BookingShortDto lastShortDto = null;
            if (lastBooking.isPresent()) {
                lastShortDto = new BookingShortDto();
                lastShortDto.setId(lastBooking.get().getId());
                lastShortDto.setBookerId(lastBooking.get().getBooker().getId());
            }

            itemOwnerDto.setId(item.getId());
            itemOwnerDto.setName(item.getName());
            itemOwnerDto.setDescription(item.getDescription());
            itemOwnerDto.setAvailable(item.getAvailable());
            itemOwnerDto.setOwner(item.getOwner());
            itemOwnerDto.setRequest(item.getRequest());
            itemOwnerDto.setNextBooking(nextShortDto);
            itemOwnerDto.setLastBooking(lastShortDto);
            itemOwnerDto.setComments(mapComments(item.getId()));

            return itemOwnerDto;
        } else {
            itemOwnerDto.setId(item.getId());
            itemOwnerDto.setName(item.getName());
            itemOwnerDto.setDescription(item.getDescription());
            itemOwnerDto.setAvailable(item.getAvailable());
            itemOwnerDto.setOwner(item.getOwner());
            itemOwnerDto.setRequest(item.getRequest());
            itemOwnerDto.setComments(mapComments(item.getId()));
            return itemOwnerDto;
        }
    }

    @Override
    @Transactional
    public CommentResponseDto createComment(Long userId, Long itemId, CommentRequestDto commentRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id = " + itemId + " не найдена"));

        boolean canComment = bookingRepository
                .existsByBooker_IdAndItem_IdAndStatusAndEndBefore(
                        userId, itemId, BookingStatus.APPROVED, LocalDateTime.now());

        if (!canComment) {
            throw new ValidationException("Пользователь не может оставить отзыв на эту вещь");
        }

        Comment comment = new Comment();
        comment.setText(commentRequestDto.getText());
        comment.setItem(item);
        comment.setAuthorId(user);
        comment.setCreated(LocalDateTime.now());

        var saved = commentRepository.save(comment);
        CommentResponseDto commentResponseDto = new CommentResponseDto();
        commentResponseDto.setId(saved.getId());
        commentResponseDto.setText(saved.getText());
        commentResponseDto.setAuthorName(saved.getAuthorId().getName());
        commentResponseDto.setCreated(saved.getCreated());

        return commentResponseDto;
    }

    private List<CommentResponseDto> mapComments(Long itemId) {
        List<Comment> comments = commentRepository.findByItem_IdOrderByCreatedAsc(itemId);
        List<CommentResponseDto> result = new ArrayList<>();

        for (Comment comment : comments) {
            CommentResponseDto dto = new CommentResponseDto();
            dto.setId(comment.getId());
            dto.setText(comment.getText());
            dto.setAuthorName(comment.getAuthorId().getName());
            dto.setCreated(comment.getCreated());
            result.add(dto);
        }

        return result;
    }

    private Map<Long, List<CommentResponseDto>> mapCommentsByItemIds(List<Long> itemIds) {
        List<Comment> comments = commentRepository.findByItem_IdInOrderByCreatedAsc(itemIds);
        Map<Long, List<CommentResponseDto>> result = new HashMap<>();

        for (Comment comment : comments) {
            CommentResponseDto dto = new CommentResponseDto();
            dto.setId(comment.getId());
            dto.setText(comment.getText());
            dto.setAuthorName(comment.getAuthorId().getName());
            dto.setCreated(comment.getCreated());

            Long itemId = comment.getItem().getId();
            result.computeIfAbsent(itemId, key -> new ArrayList<>()).add(dto);
        }

        return result;
    }
}
