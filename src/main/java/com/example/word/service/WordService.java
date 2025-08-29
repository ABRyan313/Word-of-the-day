package com.example.word.service;

import com.example.word.model.domain.DefinitionPos;
import com.example.word.model.dto.DictionaryEntry;
import com.example.word.model.dto.WordOfTheDayResponse;
import com.example.word.persistence.WordEntity;
import com.example.word.persistence.WordRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class WordService {

    private final WebClient webClient;
    private final WordRepository wordRepository;
    private static final long TTL_HOURS = 24;

    /**
     * Fetches a random word from the external Random Word API.
     *
     * @return a Mono emitting a single random word as a String
     */
    public Mono<String> getWord() {
        String randomWordApi = "https://random-word-api.herokuapp.com/word";
        return webClient
                .get()
                .uri(randomWordApi)
                .retrieve()
                .bodyToMono(String[].class)
                .map(words -> words[0]);
    }

    /**
     * Fetches definitions and parts of speech for a given word from the external Dictionary API.
     *
     * @param word the word to look up
     * @return a Mono emitting a list of DefinitionPos objects containing definitions and parts of speech;
     *         emits an empty list if the word is not found or an error occurs
     */
    public Mono<List<DefinitionPos>> getWordDefinitions(String word) {
        String definitionApi = "https://api.dictionaryapi.dev/api/v2/entries/en/" + word;

        return webClient.get()
                .uri(definitionApi)
                .retrieve()
                .bodyToMono(DictionaryEntry[].class)
                .map(entries -> entries[0].getMeanings().stream()
                        .flatMap(m -> m.getDefinitions().stream()
                                .map(d -> new DefinitionPos(d.getDefinition(), m.getPartOfSpeech()))
                        )
                        .toList()
                )
                .onErrorResume(e -> Mono.just(List.of())); // return empty list if word not found
    }

    /**
     * Asynchronously fetches a random word and its definitions with parts of speech
     * from external APIs.
     *
     * @return a Mono emitting a WordOfTheDayResponse containing the word and its definitions
     */
    public Mono<WordOfTheDayResponse> getDefinitionAndPos() {
        return getWord()
                .flatMap(word -> getWordDefinitions(word)
                        .map(defs -> new WordOfTheDayResponse(word, defs))
                );
    }

    /**
     * Retrieves the cached Word of the Day and its definitions. If a word has already been published today,
     * returns it from the database. Otherwise, fetches a new word and its definitions from external APIs,
     * saves them to the database, and returns the result.
     *
     * @return the WordOfTheDayResponse containing the word and its definitions
     */
    @Cacheable("wordOfTheDay")
    public WordOfTheDayResponse getDefinitionAndPosCached() {
        // Save in DB if not already saved for today
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        WordEntity existing = wordRepository.findFirstByPublishedAtAfterOrderByPublishedAtDesc(startOfDay);

        if (existing != null) {
            // Convert List<String> from DB to List<DefinitionPos>
            List<DefinitionPos> defsFromDb = existing.getDefinitions().stream()
                    .map(def -> new DefinitionPos(def, ""))
                    .toList();

            return new WordOfTheDayResponse(existing.getWord(), defsFromDb);
        }

        // Fetch new word from external API
        String word = getWord().block();  // blocking to return WordOfTheDayResponse
        List<DefinitionPos> definitions = getWordDefinitions(word).block();

        // Save new word in DB
        WordEntity entity = new WordEntity();
        entity.setWord(word);
        entity.setDefinitions(definitions.stream()
                .map(DefinitionPos::getDefinition)
                .toList()
        );
        entity.setPublishedAt(LocalDateTime.now());
        wordRepository.save(entity);

        return new WordOfTheDayResponse(word, definitions);
    }

    /**
     * Retrieves the history of all previously published words of the day.
     * Each entry includes the word and its definitions.
     *
     * @return a list of WordOfTheDayResponse objects ordered by publish date descending
     */
    public List<WordOfTheDayResponse> getWordHistory() {
        return wordRepository.findAllByOrderByPublishedAtDesc()
                .stream()
                .map(entity -> new WordOfTheDayResponse(
                        entity.getWord(),
                        entity.getDefinitions().stream()
                                .map(def -> new DefinitionPos(def, ""))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }
}
