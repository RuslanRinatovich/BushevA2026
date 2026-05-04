package com.furniture.service;

import com.furniture.entity.User;
import com.furniture.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + username));
    }

    @Transactional
    public User save(User user) {
        if (user.getId() == null) {
            if (user.getPassword() == null || user.getPassword().isBlank()) {
                throw new RuntimeException("Пароль обязателен для нового пользователя");
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            User existing = findById(user.getId());
            if (user.getPassword() != null && !user.getPassword().isBlank()) {
                existing.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            existing.setUsername(user.getUsername());
            existing.setFullName(user.getFullName());
            existing.setRole(user.getRole());
            existing.setEnabled(user.getEnabled());  // Обновляем статус
            user = existing;
        }
        return userRepository.save(user);
    }

    @Transactional
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}