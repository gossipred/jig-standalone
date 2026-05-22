package com.jj.jig.user;

import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserLogRepository userLogRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserLogRepository userLogRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userLogRepository = userLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public List<UserLog> findRecentLogs() {
        return userLogRepository.findTop50ByOrderByCreatedAtDesc();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    @Transactional
    public User create(UserForm form, String actorUsername) {
        String username = normalizeUsername(form.getUsername());
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists.");
        }
        if (!StringUtils.hasText(form.getPassword())) {
            throw new IllegalArgumentException("Password is required.");
        }
        requirePasswordLength(form.getPassword());

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setRole(form.getRole());
        user.setEnabled(form.isEnabled());
        User savedUser = userRepository.save(user);
        saveLog(
                savedUser,
                findActor(actorUsername),
                UserLogActionType.CREATE,
                null,
                savedUser.getRole().name(),
                "Account created. Initial password was provided by admin."
        );
        return savedUser;
    }

    @Transactional
    public User update(Long id, UserForm form, String actorUsername) {
        User user = findById(id);
        User actor = findActor(actorUsername);
        UserRole oldRole = user.getRole();
        boolean oldEnabled = user.isEnabled();
        user.setRole(form.getRole());
        user.setEnabled(form.isEnabled());
        if (oldRole != form.getRole()) {
            saveLog(
                    user,
                    actor,
                    UserLogActionType.ROLE_CHANGE,
                    oldRole.name(),
                    form.getRole().name(),
                    "Admin updated the account role."
            );
        }
        if (oldEnabled != form.isEnabled()) {
            saveLog(
                    user,
                    actor,
                    UserLogActionType.ENABLED_CHANGE,
                    Boolean.toString(oldEnabled),
                    Boolean.toString(form.isEnabled()),
                    "Admin updated the account enabled status."
            );
        }
        if (StringUtils.hasText(form.getPassword())) {
            requirePasswordLength(form.getPassword());
            user.setPassword(passwordEncoder.encode(form.getPassword()));
            saveLog(
                    user,
                    actor,
                    UserLogActionType.PASSWORD_RESET,
                    null,
                    null,
                    "Admin reset the account password."
            );
        }
        return userRepository.save(user);
    }

    @Transactional
    public void changeOwnPassword(String username, ChangePasswordForm form) {
        User user = userRepository.findByUsername(normalizeUsername(username))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        if (!passwordEncoder.matches(form.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirmation do not match.");
        }
        requirePasswordLength(form.getNewPassword());
        user.setPassword(passwordEncoder.encode(form.getNewPassword()));
        userRepository.save(user);
        saveLog(
                user,
                user,
                UserLogActionType.PASSWORD_CHANGE,
                null,
                null,
                "User changed own password."
        );
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim();
    }

    private void requirePasswordLength(String password) {
        if (password.length() < 6 || password.length() > 50) {
            throw new IllegalArgumentException("Password must be 6 to 50 characters.");
        }
    }

    private User findActor(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }
        return userRepository.findByUsername(normalizeUsername(username)).orElse(null);
    }

    private void saveLog(
            User targetUser,
            User actorUser,
            UserLogActionType actionType,
            String oldValue,
            String newValue,
            String note
    ) {
        UserLog log = new UserLog();
        log.setTargetUser(targetUser);
        log.setActorUser(actorUser);
        log.setActionType(actionType);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setNote(note);
        userLogRepository.save(log);
    }
}
