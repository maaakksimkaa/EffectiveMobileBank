package bank.effectivemobilebank.service;

import bank.effectivemobilebank.model.User;
import bank.effectivemobilebank.model.UserRole;
import bank.effectivemobilebank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setPasswordHash("encoded_password");
        testUser.setRoles(new HashSet<>(List.of(UserRole.USER)));
    }

    @Test
    void testCreateUser_Success() {
        String username = "newuser";
        String rawPassword = "password123";
        Set<UserRole> roles = new HashSet<>(Arrays.asList(UserRole.USER, UserRole.ADMIN));
        String encodedPassword = "encoded_password_hash";

        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        User result = userService.createUser(username, rawPassword, roles);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(encodedPassword, result.getPasswordHash());
        assertEquals(roles, result.getRoles());

        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCreateUser_WithEmptyRoles() {
        String username = "newuser";
        String rawPassword = "password123";
        Set<UserRole> roles = new HashSet<>();
        String encodedPassword = "encoded_password_hash";

        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        User result = userService.createUser(username, rawPassword, roles);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(encodedPassword, result.getPasswordHash());
        assertTrue(result.getRoles().isEmpty());
    }

    @Test
    void testFindByUsername_UserExists() {
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findByUsername(username);

        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userRepository).findByUsername(username);
    }

    @Test
    void testFindByUsername_UserNotExists() {
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        Optional<User> result = userService.findByUsername(username);

        assertFalse(result.isPresent());
        verify(userRepository).findByUsername(username);
    }

    @Test
    void testFindAll_UsersExist() {
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser, result.getFirst());
        verify(userRepository).findAll();
    }

    @Test
    void testFindAll_NoUsers() {
        when(userRepository.findAll()).thenReturn(new ArrayList<>());

        List<User> result = userService.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findAll();
    }

    @Test
    void testDeleteById_Success() {
        UUID userId = UUID.randomUUID();
        userService.deleteById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void testDeleteById_UserNotExists() {
        UUID userId = UUID.randomUUID();
        userService.deleteById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void testUpdateRoles_Success() {
        UUID userId = UUID.randomUUID();
        Set<UserRole> newRoles = new HashSet<>(Arrays.asList(UserRole.ADMIN));

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.updateRoles(userId, newRoles);

        assertNotNull(result);
        assertEquals(newRoles, result.getRoles());
        verify(userRepository).findById(userId);
        verify(userRepository).save(testUser);
    }

    @Test
    void testUpdateRoles_UserNotExists() {
        UUID userId = UUID.randomUUID();
        Set<UserRole> newRoles = new HashSet<>(Arrays.asList(UserRole.ADMIN));

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> {
            userService.updateRoles(userId, newRoles);
        });

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUpdateRoles_WithEmptyRoles() {
        UUID userId = UUID.randomUUID();
        Set<UserRole> newRoles = new HashSet<>();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.updateRoles(userId, newRoles);

        assertNotNull(result);
        assertTrue(result.getRoles().isEmpty());
        verify(userRepository).findById(userId);
        verify(userRepository).save(testUser);
    }
}
