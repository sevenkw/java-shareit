package ru.practicum.shareit.request.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestResponseDto createNewItemRequest(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                                       @Valid @RequestBody ItemRequestCreateDto itemRequestCreateDto) {

        return itemRequestService.createNewItemRequest(userId, itemRequestCreateDto);
    }

    @GetMapping
    public List<ItemRequestResponseDto> getUserItemRequests(@RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        return itemRequestService.getUserItemRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestResponseDto getItemRequest(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                                 @PathVariable("requestId") @Positive Long requestId) {
        return itemRequestService.getItemRequestByRequestIdAndUserId(userId, requestId);
    }

    @GetMapping("/all")
    public List<ItemRequestResponseDto> getAllItemRequests(@RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        return itemRequestService.getAllItemRequests(userId);
    }

}
