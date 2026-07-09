package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestResponseDto createNewItemRequest(Long userId, ItemRequestCreateDto itemRequestCreateDto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(itemRequestCreateDto.getDescription());
        itemRequest.setRequestor(user);
        itemRequest.setCreated(LocalDateTime.now());

        var result = itemRequestRepository.save(itemRequest);

        return ItemRequestMapper.toItemRequestResponse(result);
    }

    @Override
    public List<ItemRequestResponseDto> getUserItemRequests(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        List<ItemRequest> requests = itemRequestRepository.getAllByRequestor(user.getId());

        if (requests.isEmpty()) {
            return List.of();
        }

        List<Long> requestsId =  new ArrayList<>();
        for (ItemRequest request : requests) {
            requestsId.add(request.getId());
        }

        List<Item> allItemsByIds = itemRepository.findAllByRequestIdIn(requestsId);
        //он вернул список абсолютно всех вещей по айдишникам

        Map<Long, List<ItemRequestDto>>  itemRequestsByRequestId = new HashMap<>();

        for (Item item : allItemsByIds) {
            var key = item.getRequest().getId(); // здесь получаю из каждой вещи id этого запроса

            ItemRequestDto itemRequestDto = new ItemRequestDto();
            itemRequestDto.setId(item.getId());
            itemRequestDto.setName(item.getName());
            itemRequestDto.setOwnerId(item.getOwner().getId());

            List<ItemRequestDto> list = itemRequestsByRequestId.get(key);

            if (list == null) {
                list = new ArrayList<>();
                itemRequestsByRequestId.put(key, list);
            }

            list.add(itemRequestDto);
        }

        List<ItemRequestResponseDto> itemRequestResponseDtos = new ArrayList<>();

        for (var request : requests) {
            var key = request.getId();
            ItemRequestResponseDto itemRequestResponseDto = new ItemRequestResponseDto();

            List<ItemRequestDto> itemRequestDto = itemRequestsByRequestId.get(key);
            if (itemRequestDto == null) {
                itemRequestDto = List.of();
            }

            itemRequestResponseDto.setId(request.getId());
            itemRequestResponseDto.setDescription(request.getDescription());
            itemRequestResponseDto.setCreated(request.getCreated());
            itemRequestResponseDto.setItems(itemRequestDto);

            itemRequestResponseDtos.add(itemRequestResponseDto);
        }

        return itemRequestResponseDtos;
    }

    @Override
    public ItemRequestResponseDto getItemRequestByRequestIdAndUserId(Long userId, Long requestId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос вещи с id " + requestId + " не найден"));

        // вернет все вещи у которых id запроса равен requestId
        List<Item> listItemByRequest = itemRepository.findItemByRequest(requestId);

        List<ItemRequestDto> itemRequestDtos = new ArrayList<>();
        for (Item item : listItemByRequest) {
            ItemRequestDto itemRequestDto = new ItemRequestDto();
            itemRequestDto.setId(item.getId());
            itemRequestDto.setName(item.getName());
            itemRequestDto.setOwnerId(item.getOwner().getId());

            itemRequestDtos.add(itemRequestDto);
        }

        ItemRequestResponseDto result = new ItemRequestResponseDto();
        result.setId(itemRequest.getId());
        result.setDescription(itemRequest.getDescription());
        result.setCreated(itemRequest.getCreated());
        result.setItems(itemRequestDtos);

        return result;
    }

    @Override
    public List<ItemRequestResponseDto> getAllItemRequests(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        // все запросы на вещи всех пользователй кроме userId
        List<ItemRequest> allItemRequests = itemRequestRepository.getAllRequests(userId);

        if (allItemRequests.isEmpty()) {
            return List.of();
        }

        List<Long> requestsId =  new ArrayList<>(); // лист для всех id-ков запросов
        for (ItemRequest request : allItemRequests) {
            requestsId.add(request.getId());
        }

        List<Item> allItemsByIds = itemRepository.findAllByRequestIdIn(requestsId); // все ВЕЩИ

        Map<Long, List<ItemRequestDto>>  itemRequestsByRequestId = new HashMap<>();

        for (Item item : allItemsByIds) {
            var key = item.getRequest().getId(); // у каждой вещи получаю ее запросный id

            ItemRequestDto itemRequestDto = new ItemRequestDto();
            itemRequestDto.setId(item.getId());
            itemRequestDto.setName(item.getName());
            itemRequestDto.setOwnerId(item.getOwner().getId());

            List<ItemRequestDto> listForEveryItemRequestDto = itemRequestsByRequestId.get(key);
            if (listForEveryItemRequestDto == null) {
                listForEveryItemRequestDto = new ArrayList<>();
                itemRequestsByRequestId.put(key, listForEveryItemRequestDto);
            }
            listForEveryItemRequestDto.add(itemRequestDto);
        }

        List<ItemRequestResponseDto> result = new ArrayList<>();

        for (var request : allItemRequests) {
            var key = request.getId();

            ItemRequestResponseDto itemRequestResponseDto = new ItemRequestResponseDto();

            List<ItemRequestDto> list = itemRequestsByRequestId.get(key);
            if (list == null) {
                list = List.of();
            }

            itemRequestResponseDto.setId(request.getId());
            itemRequestResponseDto.setDescription(request.getDescription());
            itemRequestResponseDto.setCreated(request.getCreated());
            itemRequestResponseDto.setItems(list);

            result.add(itemRequestResponseDto);
        }
        return result;
    }
}
