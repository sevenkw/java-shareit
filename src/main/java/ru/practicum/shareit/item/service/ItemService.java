package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    List<Item> getAllByOwner(Long userId);

    Item getItemById(Long id);

    Item createNewItem(Long userId, Item item);

    Item updateItem(Long userId, Long itemId, Item item);

    List<Item> searchItems(String text);
}
