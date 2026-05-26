package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryItemStorage implements ItemStorage {
    private final Map<Long, Item> allItems = new LinkedHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public Item create(User owner, Item item) {
        Item savedItem = new Item();

        savedItem.setId(nextId.getAndIncrement());

        savedItem.setName(item.getName());
        savedItem.setDescription(item.getDescription());
        savedItem.setAvailable(item.getAvailable());
        savedItem.setOwner(owner);

        var key = savedItem.getId();
        allItems.put(key, savedItem);

        return savedItem;
    }

    @Override
    public Item getById(Long itemId) {

        if (itemId == null) {
            throw new ValidationException("Item id не может быть null");
        }

        if (!allItems.containsKey(itemId)) {
            throw new NotFoundException("Вещь с id = " + itemId + " не найдена");
        }

        return allItems.get(itemId);
    }

    @Override
    public List<Item> getAllByOwner(Long userId) {
        List<Item> itemsByOwner = new ArrayList<>();

        for (Item item : allItems.values()) {
            if (item.getOwner().getId().equals(userId)) {
                itemsByOwner.add(item);
            }
        }
        return itemsByOwner;
    }

    @Override
    public List<Item> search(String name) {

        List<Item> itemsByName = new ArrayList<>();

        if (name.isBlank() || name == null) {
            return List.of();
        }

        for (Item item : allItems.values()) {
            if (item.getName().equalsIgnoreCase(name) || item.getDescription().equalsIgnoreCase(name)) {
                if (item.getAvailable() == Boolean.TRUE) {
                    itemsByName.add(item);
                }
            }
        }

        return itemsByName;
    }

    @Override
    public Item update(Long userId, Long itemId, Item item) {
        if (!allItems.containsKey(itemId)) {
            throw new NotFoundException("Вещь не найдена");
        }

        Item existingItem = allItems.get(itemId);

        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new ValidationException("Редактировать вещь может только ее владелец");
        }

        if (item.getName() != null) {
            existingItem.setName(item.getName());
        }

        if (item.getDescription() != null) {
            existingItem.setDescription(item.getDescription());
        }

        if (item.getAvailable() != null) {
            existingItem.setAvailable(item.getAvailable());
        }

        allItems.put(itemId, existingItem);

        return existingItem;
    }
}
