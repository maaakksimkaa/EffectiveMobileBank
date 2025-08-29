package bank.effectivemobilebank.security;

import bank.effectivemobilebank.model.User;
import bank.effectivemobilebank.model.UserRole;
import bank.effectivemobilebank.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Set<SimpleGrantedAuthority> authorities = user.getRoles().stream()
            .map(UserRole::name)
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .collect(Collectors.toSet());
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(), user.getPasswordHash(), authorities);
    }
}


