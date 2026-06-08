package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOwnerDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest(properties =
        "spring.datasource.url=jdbc:h2:mem:shareit-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ItemRequestRepository itemRequestRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private CommentRepository commentRepository;

    @Test
    void createNewItemWithoutRequestId() {
        User owner = createUser("Yaroslav", "email@yandex.ru");

        ItemDto dto = new ItemDto();
        dto.setName("drill");
        dto.setDescription("powerful");
        dto.setAvailable(true);

        ItemDto result = itemService.createNewItem(owner.getId(), dto);

        assertThat(result, notNullValue());
        assertThat(result.getId(), notNullValue());
        assertThat(result.getName(), equalTo("drill"));
        assertThat(result.getDescription(), equalTo("powerful"));
        assertThat(result.getAvailable(), equalTo(true));
        assertThat(result.getOwner().getId(), equalTo(owner.getId()));
        assertNull(result.getRequestId());
        assertNull(result.getRequest());
    }

    @Test
    void createNewItemWithRequestId() {
        User owner = createUser("Yaroslav", "email@yandex.ru");
        ItemRequest request = createRequest(owner, "нужен шуруповерт");

        ItemDto dto = new ItemDto();
        dto.setName("drill");
        dto.setDescription("powerful");
        dto.setAvailable(true);
        dto.setRequestId(request.getId());

        ItemDto result = itemService.createNewItem(owner.getId(), dto);

        assertThat(result, notNullValue());
        assertThat(result.getId(), notNullValue());
        assertThat(result.getName(), equalTo("drill"));
        assertThat(result.getRequestId(), equalTo(request.getId()));
        assertThat(result.getRequest(), notNullValue());
        assertThat(result.getRequest().getId(), equalTo(request.getId()));
    }

    @Test
    void createNewItemInvalidUser() {
        ItemDto dto = new ItemDto();
        dto.setName("drill");
        dto.setDescription("powerful");
        dto.setAvailable(true);

        assertThrows(NotFoundException.class, () -> itemService.createNewItem(999L, dto));
    }

    @Test
    void createNewItemInvalidRequestId() {
        User owner = createUser("Yaroslav", "email@yandex.ru");

        ItemDto dto = new ItemDto();
        dto.setName("drill");
        dto.setDescription("powerful");
        dto.setAvailable(true);
        dto.setRequestId(999L);

        assertThrows(NotFoundException.class, () -> itemService.createNewItem(owner.getId(), dto));
    }

    @Test
    void updateItemShouldUpdateFields() {
        User owner = createUser("Yaroslav", "email@yandex.ru");
        Item item = createItem(owner, "old", "old desc", true);

        Item update = new Item();
        update.setName("new");
        update.setDescription("new desc");
        update.setAvailable(false);

        Item result = itemService.updateItem(owner.getId(), item.getId(), update);

        assertThat(result.getId(), equalTo(item.getId()));
        assertThat(result.getName(), equalTo("new"));
        assertThat(result.getDescription(), equalTo("new desc"));
        assertThat(result.getAvailable(), equalTo(false));
    }

    @Test
    void updateItemInvalidUser() {
        User owner = createUser("Yaroslav", "email@yandex.ru");
        Item item = createItem(owner, "old", "old desc", true);

        Item update = new Item();
        update.setName("new");

        assertThrows(NotFoundException.class, () -> itemService.updateItem(999L, item.getId(), update));
    }

    @Test
    void updateItemInvalidItemId() {
        User owner = createUser("Yaroslav", "email@yandex.ru");

        Item update = new Item();
        update.setName("new");

        assertThrows(NotFoundException.class, () -> itemService.updateItem(owner.getId(), 999L, update));
    }

    @Test
    void updateItemByNonOwnerShouldThrowValidationException() {
        User owner = createUser("Yaroslav", "email@yandex.ru");
        User otherUser = createUser("yanis", "yanis@yandex.ru");
        Item item = createItem(owner, "old", "old desc", true);

        Item update = new Item();
        update.setName("new");

        assertThrows(ValidationException.class, () -> itemService.updateItem(otherUser.getId(), item.getId(), update));
    }

    @Test
    void searchItemsShouldReturnEmptyListForBlankText() {
        List<Item> result = itemService.searchItems(" ");

        assertThat(result, empty());
    }

    @Test
    void searchItemsShouldReturnAvailableItems() {
        User owner = createUser("Yaroslav", "email@yandex.ru");
        createItem(owner, "Drill", "powerful", true);
        createItem(owner, "Saw", "sharp", true);

        List<Item> result = itemService.searchItems("drill");

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getName(), equalTo("Drill"));
    }

    @Test
    void getAllByOwnerShouldReturnItemsWithBookingsAndComments() {
        User owner = createUser("Yaroslav", "email@yandex.ru");
        User booker = createUser("yanis", "yanis@yandex.ru");
        Item item = createItem(owner, "Drill", "powerful", true);

        createBooking(item, booker, LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(1), BookingStatus.APPROVED);
        createBooking(item, booker, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), BookingStatus.APPROVED);
        createComment(item, booker, "great");

        List<ItemOwnerDto> result = itemService.getAllByOwner(owner.getId());

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), equalTo(item.getId()));
        assertThat(result.get(0).getName(), equalTo("Drill"));
        assertThat(result.get(0).getComments(), hasSize(1));
        assertThat(result.get(0).getComments().get(0).getText(), equalTo("great"));
        assertThat(result.get(0).getLastBooking(), notNullValue());
        assertThat(result.get(0).getNextBooking(), notNullValue());
    }

    @Test
    void getAllByOwnerShouldReturnEmptyListWhenNoItems() {
        User owner = createUser("Yaroslav", "email@yandex.ru");

        List<ItemOwnerDto> result = itemService.getAllByOwner(owner.getId());

        assertThat(result, empty());
    }

    @Test
    void getAllByOwnerInvalidUser() {
        assertThrows(NotFoundException.class, () -> itemService.getAllByOwner(999L));
    }

    @Test
    void getItemByIdShouldReturnItem() {
        User owner = createUser("Yaroslav", "email@yandex.ru");
        Item item = createItem(owner, "Drill", "powerful", true);

        Item result = itemService.getItemById(item.getId());

        assertThat(result.getId(), equalTo(item.getId()));
        assertThat(result.getName(), equalTo("Drill"));
    }

    @Test
    void getItemByIdInvalidId() {
        assertThrows(NotFoundException.class, () -> itemService.getItemById(999L));
    }

    @Test
    void getItemByIdForOwnerShouldReturnBookingsAndComments() {
        User owner = createUser("Yaroslav", "email@yandex.ru");
        User booker = createUser("yanis", "yanis@yandex.ru");
        Item item = createItem(owner, "Drill", "powerful", true);

        createBooking(item, booker, LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(1), BookingStatus.APPROVED);
        createBooking(item, booker, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), BookingStatus.APPROVED);
        createComment(item, booker, "great");

        ItemOwnerDto result = itemService.getItemByIdForUser(owner.getId(), item.getId());

        assertThat(result.getId(), equalTo(item.getId()));
        assertThat(result.getComments(), hasSize(1));
        assertThat(result.getLastBooking(), notNullValue());
        assertThat(result.getNextBooking(), notNullValue());
    }

    @Test
    void getItemByIdForUserInvalidItemId() {
        User owner = createUser("Yaroslav", "email@yandex.ru");

        assertThrows(NotFoundException.class, () -> itemService.getItemByIdForUser(owner.getId(), 999L));
    }

    @Test
    void createCommentShouldSaveComment() {
        User owner = createUser("Yaroslav", "email@yandex.ru");
        User booker = createUser("yanis", "yanis@yandex.ru");
        Item item = createItem(owner, "Drill", "powerful", true);

        createBooking(item, booker, LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(1), BookingStatus.APPROVED);

        CommentRequestDto dto = new CommentRequestDto();
        dto.setText("great");

        CommentResponseDto result = itemService.createComment(booker.getId(), item.getId(), dto);

        assertThat(result, notNullValue());
        assertThat(result.getId(), notNullValue());
        assertThat(result.getText(), equalTo("great"));
        assertThat(result.getAuthorName(), equalTo(booker.getName()));
        assertThat(result.getCreated(), notNullValue());
    }

    @Test
    void createCommentInvalidUser() {
        User owner = createUser("Yaroslav", "email@yandex.ru");
        Item item = createItem(owner, "Drill", "powerful", true);

        CommentRequestDto dto = new CommentRequestDto();
        dto.setText("great");

        assertThrows(NotFoundException.class, () -> itemService.createComment(999L, item.getId(), dto));
    }

    @Test
    void createCommentInvalidItem() {
        User user = createUser("Yaroslav", "email@yandex.ru");

        CommentRequestDto dto = new CommentRequestDto();
        dto.setText("great");

        assertThrows(NotFoundException.class, () -> itemService.createComment(user.getId(), 999L, dto));
    }

    @Test
    void createCommentWithoutApprovedPastBookingShouldThrowValidationException() {
        User owner = createUser("Yaroslav", "email@yandex.ru");
        User booker = createUser("yanis", "yanis@yandex.ru");
        Item item = createItem(owner, "Drill", "powerful", true);

        CommentRequestDto dto = new CommentRequestDto();
        dto.setText("great");

        assertThrows(ValidationException.class, () -> itemService.createComment(booker.getId(), item.getId(), dto));
    }

    @Test
    void searchItems_shouldIgnoreUnavailableItems() {
        User owner = createUser("Yaroslav", "email@yandex.ru");

        createItem(owner, "Drill", "powerful", true);
        createItem(owner, "Drill hidden", "powerful", false);

        List<Item> result = itemService.searchItems("drill");

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getName(), equalTo("Drill"));
        assertThat(result.get(0).getAvailable(), equalTo(true));
    }

    @Test
    void getItemByIdForUser_shouldNotReturnBookingsForNonOwner() {
        User owner = createUser("Yaroslav", "email@yandex.ru");
        User booker = createUser("yanis", "yanis@yandex.ru");
        User otherUser = createUser("guest", "guest@yandex.ru");

        Item item = createItem(owner, "Drill", "powerful", true);

        createBooking(
                item,
                booker,
                LocalDateTime.now().minusDays(3),
                LocalDateTime.now().minusDays(1),
                BookingStatus.APPROVED
        );

        ItemOwnerDto result = itemService.getItemByIdForUser(otherUser.getId(), item.getId());

        assertThat(result, notNullValue());
        assertThat(result.getId(), equalTo(item.getId()));
        assertThat(result.getName(), equalTo("Drill"));
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
    }

    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return userService.createNewUser(user);
    }

    private ItemRequest createRequest(User owner, String description) {
        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequestor(owner);
        request.setCreated(LocalDateTime.now());
        return itemRequestRepository.save(request);
    }

    private Item createItem(User owner, String name, String description, boolean available) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setOwner(owner);
        return itemRepository.save(item);
    }

    private Booking createBooking(Item item, User booker, LocalDateTime start, LocalDateTime end, BookingStatus status) {
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

    private Comment createComment(Item item, User author, String text) {
        Comment comment = new Comment();
        comment.setItem(item);
        comment.setAuthorId(author);
        comment.setText(text);
        comment.setCreated(LocalDateTime.now());
        return commentRepository.save(comment);
    }
}
