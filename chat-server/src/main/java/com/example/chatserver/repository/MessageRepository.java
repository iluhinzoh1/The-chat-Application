package com.example.chatserver.repository;

import com.example.chatserver.Entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // Достаем последние сообщения из базы (сортированные по времени)
    List<Message> findTop200ByOrderByTimestampAsc();
}
