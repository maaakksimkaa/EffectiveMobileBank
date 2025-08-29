package bank.effectivemobilebank.mapper;

import bank.effectivemobilebank.dto.CardDto;
import bank.effectivemobilebank.model.Card;
import bank.effectivemobilebank.model.CardStatus;
import bank.effectivemobilebank.model.User;
import bank.effectivemobilebank.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CardMapperTest {

    private CardMapper cardMapper;
    private User testUser;
    private Card testCard;

    @BeforeEach
    void setUp() {
        cardMapper = new CardMapper();
        
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setPasswordHash("encoded_password");
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.USER);
        testUser.setRoles(roles);
        
        testCard = new Card();
        testCard.setId(UUID.randomUUID());
        testCard.setOwner(testUser);
        testCard.setStatus(CardStatus.ACTIVE);
        testCard.setBalance(BigDecimal.valueOf(1000.50));
        testCard.setExpiry(LocalDate.of(2025, 12, 31));
        testCard.setPanLast4("1234");
        testCard.setPanEncrypted("encrypted_pan");
    }

    @Test
    void testToDto_Success() {
        CardDto result = cardMapper.toDto(testCard);

        assertNotNull(result);
        assertEquals(testCard.getId(), result.getId());
        assertEquals("**** **** **** 1234", result.getMaskedNumber());
        assertEquals(testCard.getExpiry(), result.getExpiry());
        assertEquals(testCard.getStatus(), result.getStatus());
        assertEquals(testCard.getBalance(), result.getBalance());
        assertEquals(testCard.getOwner().getId(), result.getOwnerId());
    }

    @Test
    void testToDto_WithDifferentPanLast4() {
        testCard.setPanLast4("5678");

        CardDto result = cardMapper.toDto(testCard);

        assertEquals("**** **** **** 5678", result.getMaskedNumber());
    }

    @Test
    void testToDto_WithDifferentStatus() {
        testCard.setStatus(CardStatus.BLOCKED);

        CardDto result = cardMapper.toDto(testCard);

        assertEquals(CardStatus.BLOCKED, result.getStatus());
    }

    @Test
    void testToDto_WithZeroBalance() {
        testCard.setBalance(BigDecimal.ZERO);

        CardDto result = cardMapper.toDto(testCard);

        assertEquals(BigDecimal.ZERO, result.getBalance());
    }

    @Test
    void testToDto_WithLargeBalance() {
        testCard.setBalance(BigDecimal.valueOf(999999.99));

        CardDto result = cardMapper.toDto(testCard);

        assertEquals(BigDecimal.valueOf(999999.99), result.getBalance());
    }

    @Test
    void testToDto_WithExpiredCard() {
        testCard.setExpiry(LocalDate.now().minusDays(1));
        testCard.setStatus(CardStatus.EXPIRED);

        CardDto result = cardMapper.toDto(testCard);

        assertEquals(LocalDate.now().minusDays(1), result.getExpiry());
        assertEquals(CardStatus.EXPIRED, result.getStatus());
    }

    @Test
    void testToDto_WithFutureExpiry() {
        testCard.setExpiry(LocalDate.now().plusYears(5));

        CardDto result = cardMapper.toDto(testCard);

        assertEquals(LocalDate.now().plusYears(5), result.getExpiry());
    }

    @Test
    void testToDto_WithNullCard() {
        assertThrows(NullPointerException.class, () -> {
            cardMapper.toDto(null);
        });
    }

    @Test
    void testToDto_WithNullOwner() {
        testCard.setOwner(null);

        assertThrows(NullPointerException.class, () -> {
            cardMapper.toDto(testCard);
        });
    }

    @Test
    void testToDto_WithNullOwnerId() {
        testUser.setId(null);
        testCard.setOwner(testUser);

        assertThrows(NullPointerException.class, () -> {
            cardMapper.toDto(testCard);
        });
    }

    @Test
    void testToDto_WithNullPanLast4() {
        testCard.setPanLast4(null);
        assertThrows(NullPointerException.class, () -> {
            cardMapper.toDto(testCard);
        });
    }

    @Test
    void testToDto_WithEmptyPanLast4() {
        testCard.setPanLast4("");
        CardDto result = cardMapper.toDto(testCard);
        assertEquals("**** **** **** ", result.getMaskedNumber());
    }

    @Test
    void testToDto_WithShortPanLast4() {
        testCard.setPanLast4("12");
        CardDto result = cardMapper.toDto(testCard);
        assertEquals("**** **** **** 12", result.getMaskedNumber());
    }

    @Test
    void testToDto_WithLongPanLast4() {
        testCard.setPanLast4("12345");
        CardDto result = cardMapper.toDto(testCard);
        assertEquals("**** **** **** 12345", result.getMaskedNumber());
    }

    @Test
    void testToDto_WithSpecialCharactersInPanLast4() {
        testCard.setPanLast4("12-3");
        CardDto result = cardMapper.toDto(testCard);
        assertEquals("**** **** **** 12-3", result.getMaskedNumber());
    }

    @Test
    void testToDto_WithUnicodeCharactersInPanLast4() {
        testCard.setPanLast4("12те");
        CardDto result = cardMapper.toDto(testCard);
        assertEquals("**** **** **** 12те", result.getMaskedNumber());
    }

    @Test
    void testToDto_VerifyAllFields() {
        UUID cardId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDate expiry = LocalDate.of(2026, 6, 15);
        BigDecimal balance = BigDecimal.valueOf(2500.75);
        
        testCard.setId(cardId);
        testUser.setId(userId);
        testCard.setExpiry(expiry);
        testCard.setBalance(balance);
        testCard.setPanLast4("9876");

        CardDto result = cardMapper.toDto(testCard);

        assertEquals(cardId, result.getId());
        assertEquals("**** **** **** 9876", result.getMaskedNumber());
        assertEquals(expiry, result.getExpiry());
        assertEquals(CardStatus.ACTIVE, result.getStatus());
        assertEquals(balance, result.getBalance());
        assertEquals(userId, result.getOwnerId());
    }

    @Test
    void testToDto_WithNegativeBalance() {
        testCard.setBalance(BigDecimal.valueOf(-100.25));
        CardDto result = cardMapper.toDto(testCard);
        assertEquals(BigDecimal.valueOf(-100.25), result.getBalance());
    }

    @Test
    void testToDto_WithPreciseBalance() {
        testCard.setBalance(new BigDecimal("1234.5678"));
        CardDto result = cardMapper.toDto(testCard);
        assertEquals(new BigDecimal("1234.5678"), result.getBalance());
    }
}
