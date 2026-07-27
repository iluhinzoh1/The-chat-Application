package com.example.chatserver.repository;

import com.example.chatserver.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Метод для быстрого поиска юзера по логину (пригодится для авторизации)
    Optional<User> findByUsername(String username);

    // Проверка, существует ли уже такой логин
    boolean existsByUsername(String username);
}
