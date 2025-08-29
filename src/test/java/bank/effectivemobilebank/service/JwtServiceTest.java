package bank.effectivemobilebank.service;

import bank.effectivemobilebank.security.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;
    private static final String SECRET_KEY = "test-secret-key-for-jwt-service-testing-purposes-only";
    private static final long EXPIRATION_MINUTES = 60;


    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET_KEY, EXPIRATION_MINUTES);
    }

    @Test
    void testGenerate_Success() {
        String subject = "testuser";
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "USER");

        String token = jwtService.generate(subject, claims);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
    }

    @Test
    void testGenerate_WithoutClaims() {
        String subject = "testuser";
        Map<String, Object> claims = new HashMap<>();

        String token = jwtService.generate(subject, claims);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testGenerate_WithEmptySubject() {
        String subject = "";
        Map<String, Object> claims = new HashMap<>();

        String token = jwtService.generate(subject, claims);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testParse_Success() {
        String subject = "testuser";
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "USER");
        claims.put("department", "IT");
        
        String token = jwtService.generate(subject, claims);

        Claims parsedClaims = jwtService.parse(token);

        assertNotNull(parsedClaims);
        assertEquals(subject, parsedClaims.getSubject());
        assertEquals("USER", parsedClaims.get("role"));
        assertEquals("IT", parsedClaims.get("department"));
    }

    @Test
    void testParse_InvalidToken() {
        String invalidToken = "invalid.token.here";

        assertThrows(Exception.class, () -> {
            jwtService.parse(invalidToken);
        });
    }

    @Test
    void testParse_NullToken() {
        assertThrows(Exception.class, () -> {
            jwtService.parse(null);
        });
    }

    @Test
    void testParse_EmptyToken() {
        assertThrows(Exception.class, () -> {
            jwtService.parse("");
        });
    }

    @Test
    void testTokenExpirationTime() {
        String subject = "testuser";
        Map<String, Object> claims = new HashMap<>();
        String token = jwtService.generate(subject, claims);

        Claims parsedClaims = jwtService.parse(token);
        Date expiration = parsedClaims.getExpiration();
        Date now = new Date();
        long diffInMinutes = (expiration.getTime() - now.getTime()) / (1000 * 60);


        assertTrue(diffInMinutes >= EXPIRATION_MINUTES - 1);
        assertTrue(diffInMinutes <= EXPIRATION_MINUTES + 1);
    }

    @Test
    void testTokenWithSpecialCharacters() {
        String subject = "test.user@domain.com";
        Map<String, Object> claims = new HashMap<>();
        String token = jwtService.generate(subject, claims);

        Claims parsedClaims = jwtService.parse(token);

        assertEquals(subject, parsedClaims.getSubject());
    }

    @Test
    void testTokenWithUnicodeCharacters() {
        String subject = "тестпользователь";
        Map<String, Object> claims = new HashMap<>();
        String token = jwtService.generate(subject, claims);

        Claims parsedClaims = jwtService.parse(token);

        assertEquals(subject, parsedClaims.getSubject());
    }

    @Test
    void testMultipleTokensForSameUser() {
        String subject = "testuser";
        Map<String, Object> claims = new HashMap<>();

        String token1 = jwtService.generate(subject, claims);
        String token2 = jwtService.generate(subject, claims);

        assertEquals(subject, jwtService.parse(token1).getSubject());
        assertEquals(subject, jwtService.parse(token2).getSubject());

    }

    @Test
    void testTokenWithComplexClaims() {
        String subject = "testuser";
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ADMIN");
        claims.put("permissions", "read,write,delete");
        claims.put("userId", 12345);
        claims.put("active", true);
        
        String token = jwtService.generate(subject, claims);

        Claims parsedClaims = jwtService.parse(token);

        assertEquals(subject, parsedClaims.getSubject());
        assertEquals("ADMIN", parsedClaims.get("role"));
        assertEquals("read,write,delete", parsedClaims.get("permissions"));
        assertEquals(12345, parsedClaims.get("userId"));
        assertEquals(true, parsedClaims.get("active"));
    }

    @Test
    void testTokenIssuedAt() {
        String subject = "testuser";
        Map<String, Object> claims = new HashMap<>();
        String token = jwtService.generate(subject, claims);

        Claims parsedClaims = jwtService.parse(token);
        Date issuedAt = parsedClaims.getIssuedAt();
        Date now = new Date();

        assertNotNull(issuedAt);
        assertTrue(issuedAt.before(now) || issuedAt.equals(now));
    }
}
