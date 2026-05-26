package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/items")
@Validated
public class ItemController {
    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public List<ItemDto> getItems(@RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        List<Item> itemList = itemService.getAllByOwner(userId);
        List<ItemDto> itemDtoList = new ArrayList<>();

        for (Item item : itemList) {
            itemDtoList.add(ItemMapper.toItemDto(item));
        }

        return itemDtoList;
    }

    @GetMapping("/{id}")
    public ItemDto getItemById(@PathVariable @Positive Long id) {
        Item item = itemService.getItemById(id);
        return ItemMapper.toItemDto(item);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItem(@RequestParam String text) {
        List<Item> itemList = itemService.searchItems(text);
        List<ItemDto> itemDtoList = new ArrayList<>();

        for (Item i : itemList) {
            itemDtoList.add(ItemMapper.toItemDto(i));
        }

        return itemDtoList;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto createItem(@RequestHeader("X-Sharer-User-Id") @Positive Long userId, @Valid @RequestBody ItemDto item) {
        Item useItem = ItemMapper.toItem(item);
        Item created = itemService.createNewItem(userId, useItem);
        return ItemMapper.toItemDto(created);
    }

    @PatchMapping("/{id}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                              @Positive @PathVariable("id") Long itemId,
                              @RequestBody ItemDto item) {
        Item useItem = ItemMapper.toItem(item);
        Item updated = itemService.updateItem(userId, itemId, useItem);
        return ItemMapper.toItemDto(updated);
    }

}
