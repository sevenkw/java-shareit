package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface ItemStorage {

    Item create(User userId, Item item);

    Item update(Long userId, Long itemId, Item item);

    Item getById(Long itemId);

    List<Item> getAllByOwner(Long userId);

    List<Item> search(String name);

}
