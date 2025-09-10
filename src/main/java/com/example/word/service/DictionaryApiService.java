package com.example.word.service;

import com.example.word.model.domain.DefinitionPos;
import com.example.word.model.dto.DictionaryEntry;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;


@RequiredArgsConstructor
@Service
public class DictionaryApiService {

    private final WebClient webClient;

    /**
     * Fetches definitions and parts of speech for a given word from Dictionary API.
     *
     * @param word the word to look up
     * @return a Mono emitting a list of DefinitionPos objects; empty if not found
     */
    public Mono<List<DefinitionPos>> getDefinitions(String word) {
        String definitionApi = "https://api.dictionaryapi.dev/api/v2/entries/en/" + word;

        return webClient.get()
                .uri(definitionApi)
                .retrieve()
                .bodyToMono(DictionaryEntry[].class)
                .map(entries -> entries[0].getMeanings().stream()
                        .flatMap(m -> m.getDefinitions().stream()
                                .map(d -> new DefinitionPos(d.getDefinition(), m.getPartOfSpeech())))
                        .toList()
                )
                .onErrorResume(e -> Mono.just(List.of()));
    }
}


