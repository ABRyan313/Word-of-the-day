package com.example.word.service;

import com.example.word.model.domain.DefinitionPos;
import com.example.word.model.dto.DictionaryEntry;
import com.example.word.model.dto.WordOfTheDayResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@AllArgsConstructor
@Service
public class WordService {

    private final WebClient webClient;

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
}
