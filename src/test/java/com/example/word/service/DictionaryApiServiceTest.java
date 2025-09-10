package com.example.word.service;

import com.example.word.model.domain.DefinitionPos;
import com.example.word.model.dto.DictionaryEntry;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class DictionaryApiServiceTest {

    @Test
    void getDefinitions_returnsDefinitions() {
        WebClient webClient = Mockito.mock(WebClient.class, Mockito.RETURNS_DEEP_STUBS);

        DictionaryEntry entry = new DictionaryEntry();
        DictionaryEntry.Meaning meaning = new DictionaryEntry.Meaning();
        meaning.setPartOfSpeech("noun");
        DictionaryEntry.Definition def = new DictionaryEntry.Definition();
        def.setDefinition("A test definition");
        meaning.setDefinitions(List.of(def));
        entry.setMeanings(List.of(meaning));

        when(webClient.get()
                .uri(anyString())
                .retrieve()
                .bodyToMono(DictionaryEntry[].class))
                .thenReturn(Mono.just(new DictionaryEntry[]{entry}));

        DictionaryApiService service = new DictionaryApiService(webClient);

        Mono<List<DefinitionPos>> result = service.getDefinitions("test");

        StepVerifier.create(result)
                .expectNextMatches(list -> list.size() == 1 &&
                        list.get(0).getDefinition().equals("A test definition") &&
                        list.get(0).getPartOfSpeech().equals("noun"))
                .verifyComplete();
    }

    @Test
    void getDefinitions_handlesError() {
        WebClient webClient = Mockito.mock(WebClient.class, Mockito.RETURNS_DEEP_STUBS);

        when(webClient.get()
                .uri(anyString())
                .retrieve()
                .bodyToMono(DictionaryEntry[].class))
                .thenReturn(Mono.error(new RuntimeException("API error")));

        DictionaryApiService service = new DictionaryApiService(webClient);

        Mono<List<DefinitionPos>> result = service.getDefinitions("test");

        StepVerifier.create(result)
                .expectNext(List.of())
                .verifyComplete();
    }
}
