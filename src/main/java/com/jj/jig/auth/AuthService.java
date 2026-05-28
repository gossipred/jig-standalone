package com.jj.jig.auth;

import com.jj.jig.user.UserRepository;
import com.jj.jig.user.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserSession userSession;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserSession userSession) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userSession = userSession;
    }

    public boolean login(String username, String rawPassword) {
        return userRepository.findByUsername(username)
                .filter(u -> u.isEnabled())
                .filter(u -> passwordEncoder.matches(rawPassword, u.getPassword()))
                .map(u -> { userSession.login(u); return true; })
                .orElse(false);
    }

    public void logout() {
        userSession.logout();
    }

    public boolean verifyAdmin(String username, String rawPassword) {
        return userRepository.findByUsername(username)
                .filter(u -> u.isEnabled())
                .filter(u -> passwordEncoder.matches(rawPassword, u.getPassword()))
                .filter(u -> u.getRole() == UserRole.ADMIN)
                .isPresent();
    }
}
