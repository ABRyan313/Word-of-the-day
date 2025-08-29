package com.example.word.model.dto;

import lombok.Data;
import java.util.List;

/**
 * Represents a dictionary entry containing a word and its meanings.
 * Includes nested classes for meaning and definition details.
 */

@Data
public class DictionaryEntry {
    private String word;
    private List<Meaning> meanings;

    @Data
    public static class Meaning {
        private String partOfSpeech;
        private List<Definition> definitions;
    }

    @Data
    public static class Definition {
        private String definition;
    }
}
