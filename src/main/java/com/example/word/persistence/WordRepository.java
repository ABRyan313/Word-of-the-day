package com.example.word.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface WordRepository extends JpaRepository<WordEntity, Long> {

    // Find today's word
    WordEntity findFirstByPublishedAtAfterOrderByPublishedAtDesc(LocalDateTime afterTime);

    // Fetch all words for history
    List<WordEntity> findAllByOrderByPublishedAtDesc();
}
