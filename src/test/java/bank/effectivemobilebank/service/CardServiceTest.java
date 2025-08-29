package bank.effectivemobilebank.service;

import bank.effectivemobilebank.config.CardNumberCipher;
import bank.effectivemobilebank.model.Card;
import bank.effectivemobilebank.model.CardStatus;
import bank.effectivemobilebank.model.User;
import bank.effectivemobilebank.repository.CardRepository;
import bank.effectivemobilebank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardNumberCipher cipher;

    @InjectMocks
    private CardService cardService;

    private User user;
    private UUID cardId;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("maks");

        cardId = UUID.randomUUID();
    }

    // метод createCard — успешное создание карты
    @Test
    void testCreateCard() {
        String plainPan = "1234567890123456";
        LocalDate expiry = LocalDate.now().plusYears(3);
        when(cipher.encrypt(plainPan)).thenReturn("encryptedPan");
        when(cardRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Card card = cardService.createCard(user, plainPan, expiry);

        assertEquals(user, card.getOwner());
        assertEquals("encryptedPan", card.getPanEncrypted());
        assertEquals("3456", card.getPanLast4());
        assertEquals(CardStatus.ACTIVE, card.getStatus());
        assertEquals(BigDecimal.ZERO, card.getBalance());

        verify(cardRepository).save(card);
    }

    // метод createCardForOwnerId — создание карты по ID владельца
    @Test
    void testCreateCardForOwnerId() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(cipher.encrypt(anyString())).thenReturn("enc");
        when(cardRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Card card = cardService.createCardForOwnerId(user.getId(), "1234567890123456", LocalDate.now());

        assertEquals(user, card.getOwner());
    }

    // метод transfer — успешный перевод между картами одного пользователя
    @Test
    void testTransferSuccessful() {
        Card from = new Card();
        from.setId(cardId);
        from.setOwner(user);
        from.setBalance(new BigDecimal("100"));
        from.setStatus(CardStatus.ACTIVE);

        Card to = new Card();
        to.setId(UUID.randomUUID());
        to.setOwner(user);
        to.setBalance(BigDecimal.ZERO);
        to.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findByIdAndOwner(from.getId(), user)).thenReturn(Optional.of(from));
        when(cardRepository.findByIdAndOwner(to.getId(), user)).thenReturn(Optional.of(to));

        cardService.transfer(user, from.getId(), to.getId(), new BigDecimal("50"));

        assertEquals(new BigDecimal("50"), from.getBalance());
        assertEquals(new BigDecimal("50"), to.getBalance());

        verify(cardRepository).save(from);
        verify(cardRepository).save(to);
    }

    // метод transfer — недостаточно средств для перевода
    @Test
    void testTransferInsufficientFunds() {
        Card from = new Card();
        from.setId(cardId);
        from.setOwner(user);
        from.setBalance(new BigDecimal("10"));
        from.setStatus(CardStatus.ACTIVE);

        Card to = new Card();
        to.setId(UUID.randomUUID());
        to.setOwner(user);
        to.setBalance(BigDecimal.ZERO);
        to.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findByIdAndOwner(from.getId(), user)).thenReturn(Optional.of(from));
        when(cardRepository.findByIdAndOwner(to.getId(), user)).thenReturn(Optional.of(to));

        Exception ex = assertThrows(IllegalStateException.class, () ->
                cardService.transfer(user, from.getId(), to.getId(), new BigDecimal("50"))
        );
        assertEquals("Недостаточно средств", ex.getMessage());
    }

    // метод findUserCards — получение карт пользователя с фильтром по статусу
    @Test
    void testFindUserCardsWithStatus() {
        Card card = new Card();
        Page<Card> page = new PageImpl<>(List.of(card));
        when(cardRepository.findAllByOwnerAndStatus(user, CardStatus.ACTIVE, Pageable.unpaged()))
                .thenReturn(page);

        Page<Card> result = cardService.findUserCards(user, CardStatus.ACTIVE, Pageable.unpaged());

        assertEquals(1, result.getContent().size());
        assertEquals(card, result.getContent().getFirst());
    }

    // метод changeStatus — изменение статуса карты с владельцем
    @Test
    void testChangeStatusWithOwner() {
        Card card = new Card();
        card.setId(cardId);
        card.setOwner(user);
        card.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findByIdAndOwner(cardId, user)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(card);

        cardService.changeStatus(cardId, user, CardStatus.BLOCKED);

        assertEquals(CardStatus.BLOCKED, card.getStatus());
        verify(cardRepository).save(card);
    }

    // метод changeStatus — изменение статуса карты без указания владельца
    @Test
    void testChangeStatusWithoutOwner() {
        Card card = new Card();
        card.setId(cardId);
        card.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(card);

        cardService.changeStatus(cardId, null, CardStatus.BLOCKED);

        assertEquals(CardStatus.BLOCKED, card.getStatus());
        verify(cardRepository).save(card);
    }

    // метод topUp — успешное пополнение активной карты
    @Test
    void testTopUpSuccessful() {
        Card card = new Card();
        card.setId(cardId);
        card.setOwner(user);
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);

        when(cardRepository.findByIdAndOwner(cardId, user)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(card);

        cardService.topUp(user, cardId, new BigDecimal("100"));

        assertEquals(new BigDecimal("100"), card.getBalance());
        verify(cardRepository).save(card);
    }

    // метод topUp — попытка пополнения неактивной карты
    @Test
    void testTopUpInactiveCard() {
        Card card = new Card();
        card.setId(cardId);
        card.setOwner(user);
        card.setStatus(CardStatus.BLOCKED);

        when(cardRepository.findByIdAndOwner(cardId, user)).thenReturn(Optional.of(card));

        Exception ex = assertThrows(IllegalStateException.class, () ->
                cardService.topUp(user, cardId, BigDecimal.TEN)
        );
        assertEquals("Карта должна быть активна", ex.getMessage());
    }

    // метод adminTopUp — успешное пополнение карты администратором
    @Test
    void testAdminTopUpSuccessful() {
        Card card = new Card();
        card.setId(cardId);
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(card);

        cardService.adminTopUp(cardId, new BigDecimal("50"));

        assertEquals(new BigDecimal("50"), card.getBalance());
        verify(cardRepository).save(card);
    }

    // метод deleteById — удаление карты
    @Test
    void testDeleteById() {
        doNothing().when(cardRepository).deleteById(cardId);
        cardService.deleteById(cardId);
        verify(cardRepository).deleteById(cardId);
    }

    // метод findAll — фильтр по username и статусу
    @Test
    void testFindAllWithUsernameAndStatus() {
        Card card = new Card();
        Page<Card> page = new PageImpl<>(List.of(card));
        when(cardRepository.findAllByOwnerUsernameContainingIgnoreCaseAndStatus("maks", CardStatus.ACTIVE, Pageable.unpaged()))
                .thenReturn(page);

        Page<Card> result = cardService.findAll("maks", CardStatus.ACTIVE, Pageable.unpaged());

        assertEquals(1, result.getContent().size());
        assertEquals(card, result.getContent().getFirst());
    }

    // метод findAll — фильтр по username только
    @Test
    void testFindAllWithUsernameOnly() {
        Card card = new Card();
        Page<Card> page = new PageImpl<>(List.of(card));
        when(cardRepository.findAllByOwnerUsernameContainingIgnoreCase("maks", Pageable.unpaged()))
                .thenReturn(page);

        Page<Card> result = cardService.findAll("maks", null, Pageable.unpaged());

        assertEquals(1, result.getContent().size());
    }

    // метод findAll — фильтр по статусу только
    @Test
    void testFindAllWithStatusOnly() {
        Card card = new Card();
        Page<Card> page = new PageImpl<>(List.of(card));
        when(cardRepository.findAllByStatus(CardStatus.ACTIVE, Pageable.unpaged()))
                .thenReturn(page);

        Page<Card> result = cardService.findAll(null, CardStatus.ACTIVE, Pageable.unpaged());

        assertEquals(1, result.getContent().size());
    }

    // метод findAll — без фильтров
    @Test
    void testFindAllWithoutFilters() {
        Card card = new Card();
        Page<Card> page = new PageImpl<>(List.of(card));
        when(cardRepository.findAll(Pageable.unpaged())).thenReturn(page);

        Page<Card> result = cardService.findAll(null, null, Pageable.unpaged());

        assertEquals(1, result.getContent().size());
    }
}


