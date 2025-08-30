package com.example.word.model.dto;

import com.example.word.model.domain.DefinitionPos;
import lombok.*;

import java.util.List;
/**
 * Response DTO representing the word of the day and its definitions.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WordOfTheDayResponse {
    private String word;
    private List<DefinitionPos> definitions;
}
