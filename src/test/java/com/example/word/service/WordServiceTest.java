package com.example.word.service;

import com.example.word.model.dto.DictionaryEntry;
import com.example.word.model.dto.WordOfTheDayResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class WordServiceTest {

    @Test
    void getWord_returnsRandomWord() {
        WebClient webClient = mock(WebClient.class, RETURNS_DEEP_STUBS);

        String[] words = {"example"};
        Mono<String[]> monoWords = Mono.just(words);

        when(webClient.get()
                .uri(anyString())
                .retrieve()
                .bodyToMono(String[].class))
                .thenReturn(monoWords);

        WordService wordService = new WordService(webClient);

        StepVerifier.create(wordService.getWord())
                .expectNext("example")
                .verifyComplete();
    }

    @Test
    void getWordDefinitions_returnsDefinitionsList() {
        WebClient webClient = mock(WebClient.class, RETURNS_DEEP_STUBS);

        // Mock DictionaryEntry and its nested structure
        DictionaryEntry.Definition definition = new DictionaryEntry.Definition();
        definition.setDefinition("Sample definition");

        DictionaryEntry.Meaning meaning = new DictionaryEntry.Meaning();
        meaning.setPartOfSpeech("verb");
        meaning.setDefinitions(List.of(definition));

        DictionaryEntry entry = new DictionaryEntry();
        entry.setMeanings(List.of(meaning));

        Mono<DictionaryEntry[]> monoEntries = Mono.just(new DictionaryEntry[]{entry});

        when(webClient.get()
                .uri(anyString())
                .retrieve()
                .bodyToMono(DictionaryEntry[].class))
                .thenReturn(monoEntries);

        WordService wordService = new WordService(webClient);

        StepVerifier.create(wordService.getWordDefinitions("sample"))
                .expectNextMatches(defs ->
                        defs.size() == 1 &&
                                defs.get(0).getDefinition().equals("Sample definition") &&
                                defs.get(0).getPartOfSpeech().equals("verb")
                )
                .verifyComplete();
    }

    @Test
    void getWordDefinitions_returnsEmptyListOnError() {
        WebClient webClient = mock(WebClient.class, RETURNS_DEEP_STUBS);

        when(webClient.get()
                .uri(anyString())
                .retrieve()
                .bodyToMono(DictionaryEntry[].class))
                .thenReturn(Mono.error(new RuntimeException("Not found")));

        WordService wordService = new WordService(webClient);

        StepVerifier.create(wordService.getWordDefinitions("unknown"))
                .expectNext(List.of())
                .verifyComplete();
    }

    @Test
    void getDefinitionAndPos_returnsWordOfTheDayResponse() {
        WebClient webClient = mock(WebClient.class, RETURNS_DEEP_STUBS);

        String[] words = {"example"};
        Mono<String[]> monoWords = Mono.just(words);

        DictionaryEntry.Definition definition = new DictionaryEntry.Definition();
        definition.setDefinition("Sample definition");

        DictionaryEntry.Meaning meaning = new DictionaryEntry.Meaning();
        meaning.setPartOfSpeech("noun");
        meaning.setDefinitions(List.of(definition));

        DictionaryEntry entry = new DictionaryEntry();
        entry.setMeanings(List.of(meaning));

        Mono<DictionaryEntry[]> monoEntries = Mono.just(new DictionaryEntry[]{entry});

        when(webClient.get()
                .uri(contains("random-word-api"))
                .retrieve()
                .bodyToMono(String[].class))
                .thenReturn(monoWords);

        when(webClient.get()
                .uri(contains("dictionaryapi.dev"))
                .retrieve()
                .bodyToMono(DictionaryEntry[].class))
                .thenReturn(monoEntries);

        WordService wordService = new WordService(webClient);

        StepVerifier.create(wordService.getDefinitionAndPos())
                .expectNextMatches(response ->
                        response.getWord().equals("example") &&
                                response.getDefinitions().size() == 1 &&
                                response.getDefinitions().get(0).getDefinition().equals("Sample definition") &&
                                response.getDefinitions().get(0).getPartOfSpeech().equals("noun")
                )
                .verifyComplete();
    }

    // Unit test for getDefinitionAndPosCached
    // Place this in src/test/java/com/example/word/service/WordServiceTest.java

    @Test
    void getDefinitionAndPosCached_returnsCachedWordOfTheDayResponse() {
        WebClient webClient = mock(WebClient.class, RETURNS_DEEP_STUBS);

        String[] words = {"example"};
        Mono<String[]> monoWords = Mono.just(words);

        DictionaryEntry.Definition definition = new DictionaryEntry.Definition();
        definition.setDefinition("Sample definition");

        DictionaryEntry.Meaning meaning = new DictionaryEntry.Meaning();
        meaning.setPartOfSpeech("noun");
        meaning.setDefinitions(List.of(definition));

        DictionaryEntry entry = new DictionaryEntry();
        entry.setMeanings(List.of(meaning));

        Mono<DictionaryEntry[]> monoEntries = Mono.just(new DictionaryEntry[]{entry});

        when(webClient.get()
                .uri(contains("random-word-api"))
                .retrieve()
                .bodyToMono(String[].class))
                .thenReturn(monoWords);

        when(webClient.get()
                .uri(contains("dictionaryapi.dev"))
                .retrieve()
                .bodyToMono(DictionaryEntry[].class))
                .thenReturn(monoEntries);

        WordService wordService = new WordService(webClient);

        WordOfTheDayResponse response = wordService.getDefinitionAndPosCached();

        assert response.getWord().equals("example");
        assert response.getDefinitions().size() == 1;
        assert response.getDefinitions().get(0).getDefinition().equals("Sample definition");
        assert response.getDefinitions().get(0).getPartOfSpeech().equals("noun");
    }
}