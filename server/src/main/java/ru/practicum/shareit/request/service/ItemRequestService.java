package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestResponseDto createNewItemRequest(Long userId, ItemRequestCreateDto itemRequestCreateDto);

    List<ItemRequestResponseDto> getUserItemRequests(Long userId);

    ItemRequestResponseDto getItemRequestByRequestIdAndUserId(Long userId, Long requestId);

    List<ItemRequestResponseDto> getAllItemRequests(Long userId);
}
