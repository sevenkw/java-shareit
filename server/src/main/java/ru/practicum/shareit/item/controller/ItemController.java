package ru.practicum.shareit.item.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOwnerDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public List<ItemOwnerDto> getItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getAllByOwner(userId);
    }

    @GetMapping("/{id}")
    public ItemOwnerDto getItemById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                    @PathVariable("id") Long itemId) {
        return itemService.getItemByIdForUser(userId, itemId);
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
    public ItemDto createItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @RequestBody ItemDto itemDto) {
        return itemService.createNewItem(userId, itemDto);
    }

    @PatchMapping("/{id}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @PathVariable("id") Long itemId,
                              @RequestBody ItemDto item) {
        Item useItem = ItemMapper.toItem(item);
        Item updated = itemService.updateItem(userId, itemId, useItem);
        return ItemMapper.toItemDto(updated);
    }

    @PostMapping("/{itemId}/comment")
    public CommentResponseDto addNewComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                            @PathVariable Long itemId,
                                            @RequestBody CommentRequestDto commentRequestDto) {

        return itemService.createComment(userId, itemId, commentRequestDto);
    }

}
