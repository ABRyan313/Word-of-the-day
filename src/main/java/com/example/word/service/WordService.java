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

    public Mono<String> getWord() {
        String randomWordApi = "https://random-word-api.herokuapp.com/word";
        return webClient
                .get()
                .uri(randomWordApi)
                .retrieve()
                .bodyToMono(String[].class)
                .map(words -> words[0]);
    }

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

    public Mono<WordOfTheDayResponse> getDefinitionAndPos() {
        return getWord()
                .flatMap(word -> getWordDefinitions(word)
                        .map(defs -> new WordOfTheDayResponse(word, defs))
                );
    }

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
