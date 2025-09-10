package com.example.word.service;

import com.example.word.model.domain.DefinitionPos;
import com.example.word.model.dto.WordOfTheDayResponse;
import com.example.word.persistence.WordEntity;
import com.example.word.persistence.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ApiService {

    private final WordApiService wordApiService;
    private final DictionaryApiService dictionaryApiService;
    private final WordRepository wordRepository;

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
        String word = wordApiService.getWord().block();  // blocking to return WordOfTheDayResponse
        List<DefinitionPos> definitions = dictionaryApiService.getDefinitions(word).block();

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
        List<WordEntity> entities = wordRepository.findAllByOrderByPublishedAtDesc();

        return entities.stream()
                .map(entity -> new WordOfTheDayResponse(
                        entity.getWord(),
                        entity.getDefinitions().stream()
                                .map(def -> new DefinitionPos(def, ""))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }
}
