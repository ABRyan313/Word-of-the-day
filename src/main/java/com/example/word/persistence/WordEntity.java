package com.example.word.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "word", uniqueConstraints = {
        @UniqueConstraint(columnNames = "word")
})
public class WordEntity {

    @Id
    @GeneratedValue
    @Setter
    private Long id;

    @Setter
    @Column(nullable = false, unique = true)
    private String word;

    // tells JPA this is a collection of Strings, stored in a separate table
    @Getter
    @Setter
    @ElementCollection
    @CollectionTable(name = "word_definitions", joinColumns = @JoinColumn(name = "word_id"))
    @Column(name = "definition")
    private List<String> definitions = new ArrayList<>();

    @Setter
    private LocalDateTime publishedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
