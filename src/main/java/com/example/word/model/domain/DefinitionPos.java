package com.example.word.model.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a word definition along with its part of speech.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefinitionPos {

    private String definition;
    private String partOfSpeech;
}
