package bank.effectivemobilebank.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CardNumberCipherTest {

    private CardNumberCipher cipher;
    private static final String SECRET_KEY = "0123456789ABCDEF0123456789ABCDEF";

    @BeforeEach
    void setUp() {
        cipher = new CardNumberCipher(SECRET_KEY);
    }

    @Test
    void testEncryptAndDecrypt_Success() {
        String plainCardNumber = "1234567890123456";

        String encrypted = cipher.encrypt(plainCardNumber);
        String decrypted = cipher.decrypt(encrypted);

        assertNotNull(encrypted);
        assertNotEquals(plainCardNumber, encrypted);
        assertEquals(plainCardNumber, decrypted);
    }

    @Test
    void testEncryptAndDecrypt_EmptyString() {
        String plainCardNumber = "";

        String encrypted = cipher.encrypt(plainCardNumber);
        String decrypted = cipher.decrypt(encrypted);

        assertNotNull(encrypted);
        assertNotEquals(plainCardNumber, encrypted);
        assertEquals(plainCardNumber, decrypted);
    }

    @Test
    void testEncryptAndDecrypt_SpecialCharacters() {
        String plainCardNumber = "1234-5678-9012-3456";

        String encrypted = cipher.encrypt(plainCardNumber);
        String decrypted = cipher.decrypt(encrypted);

        assertNotNull(encrypted);
        assertNotEquals(plainCardNumber, encrypted);
        assertEquals(plainCardNumber, decrypted);
    }

    @Test
    void testEncryptAndDecrypt_UnicodeCharacters() {
        String plainCardNumber = "1234567890123456тест";

        String encrypted = cipher.encrypt(plainCardNumber);
        String decrypted = cipher.decrypt(encrypted);

        assertNotNull(encrypted);
        assertNotEquals(plainCardNumber, encrypted);
        assertEquals(plainCardNumber, decrypted);
    }

    @Test
    void testEncryptAndDecrypt_LongString() {
        String plainCardNumber = "1234567890123456789012345678901234567890";

        String encrypted = cipher.encrypt(plainCardNumber);
        String decrypted = cipher.decrypt(encrypted);

        assertNotNull(encrypted);
        assertNotEquals(plainCardNumber, encrypted);
        assertEquals(plainCardNumber, decrypted);
    }

    @Test
    void testEncryptAndDecrypt_ShortString() {
        String plainCardNumber = "123";

        String encrypted = cipher.encrypt(plainCardNumber);
        String decrypted = cipher.decrypt(encrypted);

        assertNotNull(encrypted);
        assertNotEquals(plainCardNumber, encrypted);
        assertEquals(plainCardNumber, decrypted);
    }

    @Test
    void testEncryptAndDecrypt_NullString() {
        CardNumberCipher cipher = new CardNumberCipher("secret");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            cipher.encrypt(null);
        });

        assertInstanceOf(NullPointerException.class, ex.getCause());
    }


    @Test
    void testDecrypt_InvalidEncryptedString() {
        String invalidEncrypted = "invalid-encrypted-string";

        assertThrows(Exception.class, () -> {
            cipher.decrypt(invalidEncrypted);
        });
    }

    @Test
    void testDecrypt_NullEncryptedString() {
        CardNumberCipher cipher = new CardNumberCipher("secret");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            cipher.decrypt(null);
        });

        assertInstanceOf(NullPointerException.class, ex.getCause());
    }


    @Test
    void testDecrypt_EmptyEncryptedString() {
        String invalidEncrypted = "";

        assertThrows(Exception.class, () -> {
            cipher.decrypt(invalidEncrypted);
        });
    }

    @Test
    void testEncrypt_Consistency() {
        String plainCardNumber = "1234567890123456";

        String encrypted1 = cipher.encrypt(plainCardNumber);
        String encrypted2 = cipher.encrypt(plainCardNumber);

        assertNotNull(encrypted1);
        assertNotNull(encrypted2);
        assertNotEquals(encrypted1, encrypted2); // Should be different due to random IV
    }

    @Test
    void testEncrypt_DifferentInstances() {
        String plainCardNumber = "1234567890123456";
        CardNumberCipher cipher2 = new CardNumberCipher(SECRET_KEY);

        String encrypted1 = cipher.encrypt(plainCardNumber);
        String encrypted2 = cipher2.encrypt(plainCardNumber);

        assertNotNull(encrypted1);
        assertNotNull(encrypted2);
        assertNotEquals(encrypted1, encrypted2); // Should be different due to random IV
    }

    @Test
    void testDecrypt_CrossInstance() {
        String plainCardNumber = "1234567890123456";
        CardNumberCipher cipher2 = new CardNumberCipher(SECRET_KEY);

        String encrypted = cipher.encrypt(plainCardNumber);
        String decrypted = cipher2.decrypt(encrypted);

        assertEquals(plainCardNumber, decrypted);
    }

    @Test
    void testEncrypt_DifferentSecrets() {
        String plainCardNumber = "1234567890123456";
        CardNumberCipher cipher2 = new CardNumberCipher("different-secret-key");

        String encrypted1 = cipher.encrypt(plainCardNumber);
        String encrypted2 = cipher2.encrypt(plainCardNumber);

        assertNotNull(encrypted1);
        assertNotNull(encrypted2);
        assertNotEquals(encrypted1, encrypted2);
    }

    @Test
    void testDecrypt_DifferentSecrets() {
        String plainCardNumber = "1234567890123456";
        CardNumberCipher cipher2 = new CardNumberCipher("different-secret-key");

        String encrypted = cipher.encrypt(plainCardNumber);

        assertThrows(Exception.class, () -> {
            cipher2.decrypt(encrypted);
        });
    }

    @Test
    void testEncrypt_RealCardNumbers() {
        String[] cardNumbers = {
            "4111111111111111",
            "5555555555554444",
            "378282246310005",
            "6011111111111117",
            "3566002020360505"
        };

        // When & Then
        for (String cardNumber : cardNumbers) {
            String encrypted = cipher.encrypt(cardNumber);
            String decrypted = cipher.decrypt(encrypted);

            assertNotNull(encrypted);
            assertNotEquals(cardNumber, encrypted);
            assertEquals(cardNumber, decrypted);
        }
    }

    @Test
    void testEncrypt_WithSpaces() {
        String plainCardNumber = "1234 5678 9012 3456";

        String encrypted = cipher.encrypt(plainCardNumber);
        String decrypted = cipher.decrypt(encrypted);

        assertNotNull(encrypted);
        assertNotEquals(plainCardNumber, encrypted);
        assertEquals(plainCardNumber, decrypted);
    }

    @Test
    void testEncrypt_WithDashes() {
        String plainCardNumber = "1234-5678-9012-3456";

        String encrypted = cipher.encrypt(plainCardNumber);
        String decrypted = cipher.decrypt(encrypted);

        assertNotNull(encrypted);
        assertNotEquals(plainCardNumber, encrypted);
        assertEquals(plainCardNumber, decrypted);
    }

    @Test
    void testEncrypt_WithMixedFormatting() {
        String plainCardNumber = "1234 5678-9012 3456";

        String encrypted = cipher.encrypt(plainCardNumber);
        String decrypted = cipher.decrypt(encrypted);

        assertNotNull(encrypted);
        assertNotEquals(plainCardNumber, encrypted);
        assertEquals(plainCardNumber, decrypted);
    }

    @Test
    void testEncrypt_ShortSecretKey() {
        String shortSecret = "short";
        CardNumberCipher shortCipher = new CardNumberCipher(shortSecret);
        String plainCardNumber = "1234567890123456";

        String encrypted = shortCipher.encrypt(plainCardNumber);
        String decrypted = shortCipher.decrypt(encrypted);

        assertNotNull(encrypted);
        assertNotEquals(plainCardNumber, encrypted);
        assertEquals(plainCardNumber, decrypted);
    }

    @Test
    void testEncrypt_LongSecretKey() {
        String longSecret = "very-long-secret-key-that-exceeds-the-minimum-required-length-for-testing-purposes";
        CardNumberCipher longCipher = new CardNumberCipher(longSecret);
        String plainCardNumber = "1234567890123456";

        String encrypted = longCipher.encrypt(plainCardNumber);
        String decrypted = longCipher.decrypt(encrypted);

        assertNotNull(encrypted);
        assertNotEquals(plainCardNumber, encrypted);
        assertEquals(plainCardNumber, decrypted);
    }
}
