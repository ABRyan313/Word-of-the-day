package com.example.word.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents a dictionary entry containing a word and its meanings.
 * Includes nested classes for meaning and definition details.
 */

@Getter
@Setter
public class DictionaryEntry {
    private String word;
    private List<Meaning> meanings;

    @Getter
    @Setter
    public static class Meaning {
        private String partOfSpeech;
        private List<Definition> definitions;
    }

    @Getter
    @Setter
    public static class Definition {
        private String definition;
    }
}
