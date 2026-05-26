package ru.practicum.shareit.item.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Autowired
    public ItemServiceImpl(ItemStorage itemStorage, UserStorage userStorage) {
        this.itemStorage = itemStorage;
        this.userStorage = userStorage;
    }

    @Override
    public List<Item> getAllByOwner(Long userId) {
        if (userStorage.getById(userId) == null) {
            throw new NotFoundException("Пользователь не найден");
        }

        return itemStorage.getAllByOwner(userId);
    }

    @Override
    public Item getItemById(Long id) {
        return itemStorage.getById(id);
    }

    @Override
    public Item createNewItem(Long userId, Item item) {

        if (userStorage.getById(userId) == null) {
            throw new NotFoundException("Пользователь не найден");
        }

        User owner = userStorage.getById(userId);

        return itemStorage.create(owner, item);
    }

    @Override
    public Item updateItem(Long userId, Long itemId, Item item) {
        if (userStorage.getById(userId) == null) {
            throw new NotFoundException("Пользователь не найден");
        }

        return itemStorage.update(userId, itemId, item);
    }

    @Override
    public List<Item> searchItems(String text) {
        return itemStorage.search(text);
    }

}
