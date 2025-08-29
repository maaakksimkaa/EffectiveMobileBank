package bank.effectivemobilebank.service;

import bank.effectivemobilebank.model.User;
import bank.effectivemobilebank.model.UserRole;
import bank.effectivemobilebank.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(String username, String rawPassword, Set<UserRole> roles) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Пользователь уже существует");
        }
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRoles(roles);
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public void deleteById(UUID id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public User updateRoles(UUID id, Set<UserRole> roles) {
        User user = userRepository.findById(id).orElseThrow();
        user.setRoles(roles);
        return userRepository.save(user);
    }
}


