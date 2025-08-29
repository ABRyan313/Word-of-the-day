package com.example.word.service;

import com.example.word.model.dto.DictionaryEntry;
import com.example.word.model.dto.WordOfTheDayResponse;
import com.example.word.persistence.WordEntity;
import com.example.word.persistence.WordRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class WordServiceTest {

    @Mock
    private WordRepository wordRepository;

    @InjectMocks
    private WordService wordService;

    @Test
    void getWord_returnsFirstWordFromApi() {
        WebClient webClient = mock(WebClient.class, RETURNS_DEEP_STUBS);
        String[] words = {"testword"};
        Mono<String[]> monoWords = Mono.just(words);

        when(webClient.get()
                .uri("https://random-word-api.herokuapp.com/word")
                .retrieve()
                .bodyToMono(String[].class))
                .thenReturn(monoWords);

        WordService wordService = new WordService(webClient, null);

        StepVerifier.create(wordService.getWord())
                .expectNext("testword")
                .verifyComplete();
    }

    // Unit test for getWordDefinitions
    @Test
    void getWordDefinitions_returnsDefinitionsAndPos() {
        WebClient webClient = mock(WebClient.class, RETURNS_DEEP_STUBS);

        DictionaryEntry.Definition definition = new DictionaryEntry.Definition();
        definition.setDefinition("A test definition");

        DictionaryEntry.Meaning meaning = new DictionaryEntry.Meaning();
        meaning.setPartOfSpeech("verb");
        meaning.setDefinitions(List.of(definition));

        DictionaryEntry entry = new DictionaryEntry();
        entry.setMeanings(List.of(meaning));

        Mono<DictionaryEntry[]> monoEntries = Mono.just(new DictionaryEntry[]{entry});

        when(webClient.get()
                .uri(contains("dictionaryapi.dev"))
                .retrieve()
                .bodyToMono(DictionaryEntry[].class))
                .thenReturn(monoEntries);

        WordService wordService = new WordService(webClient, null);

        StepVerifier.create(wordService.getWordDefinitions("testword"))
                .expectNextMatches(defs ->
                        defs.size() == 1 &&
                                defs.get(0).getDefinition().equals("A test definition") &&
                                defs.get(0).getPartOfSpeech().equals("verb")
                )
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

        WordService wordService = new WordService(webClient, null);

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

    // Unit test for getDefinitionAndPosCached
    @Test
    void getDefinitionAndPosCached_returnsCachedWordOfTheDayResponse() {
        WebClient webClient = mock(WebClient.class, RETURNS_DEEP_STUBS);
        WordRepository wordRepository = mock(WordRepository.class);

        // Prepare DB entity for today
        WordEntity entity = new WordEntity();
        entity.setWord("cachedword");
        entity.setDefinitions(List.of("Cached definition"));
        entity.setPublishedAt(LocalDateTime.now());

        when(wordRepository.findFirstByPublishedAtAfterOrderByPublishedAtDesc(any())).thenReturn(entity);

        WordService wordService = new WordService(webClient, wordRepository);

        WordOfTheDayResponse response = wordService.getDefinitionAndPosCached();

        assert response.getWord().equals("cachedword");
        assert response.getDefinitions().size() == 1;
        assert response.getDefinitions().get(0).getDefinition().equals("Cached definition");
    }

    @Test
    void getWordHistory_returnsListOfWordOfTheDayResponses() {
        WordRepository wordRepository = mock(WordRepository.class);

        WordEntity entity1 = new WordEntity();
        entity1.setWord("word1");
        entity1.setDefinitions(List.of("def1", "def2"));
        entity1.setPublishedAt(LocalDateTime.now());

        WordEntity entity2 = new WordEntity();
        entity2.setWord("word2");
        entity2.setDefinitions(List.of("def3"));
        entity2.setPublishedAt(LocalDateTime.now().minusDays(1));

        when(wordRepository.findAllByOrderByPublishedAtDesc()).thenReturn(List.of(entity1, entity2));

        WordService wordService = new WordService(null, wordRepository);

        List<WordOfTheDayResponse> history = wordService.getWordHistory();

        assert history.size() == 2;
        assert history.get(0).getWord().equals("word1");
        assert history.get(0).getDefinitions().size() == 2;
        assert history.get(1).getWord().equals("word2");
        assert history.get(1).getDefinitions().size() == 1;
    }
}