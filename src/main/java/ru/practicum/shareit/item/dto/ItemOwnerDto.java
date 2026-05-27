package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Data
public class ItemOwnerDto {
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotNull
    private Boolean available;
    private User owner;
    private ItemRequest request;
    private BookingShortDto lastBooking;
    private BookingShortDto nextBooking;
    private List<CommentResponseDto> comments;
}
