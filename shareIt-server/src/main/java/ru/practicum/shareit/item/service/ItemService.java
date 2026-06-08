package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOwnerDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    List<ItemOwnerDto> getAllByOwner(Long userId);

    Item getItemById(Long id);

    ItemDto createNewItem(Long userId, ItemDto itemDto);

    Item updateItem(Long userId, Long itemId, Item item);

    List<Item> searchItems(String text);

    ItemOwnerDto getItemByIdForUser(Long userId, Long itemId);

    CommentResponseDto createComment(Long userId, Long itemId, CommentRequestDto commentRequestDto);


}
