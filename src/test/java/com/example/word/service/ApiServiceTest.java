package com.example.word.service;

import com.example.word.model.domain.DefinitionPos;
import com.example.word.model.dto.WordOfTheDayResponse;
import com.example.word.persistence.WordEntity;
import com.example.word.persistence.WordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ApiServiceTest {

    private WordApiService wordApiService;
    private DictionaryApiService dictionaryApiService;
    private WordRepository wordRepository;
    private ApiService apiService;

    @BeforeEach
    void setUp() {
        wordApiService = mock(WordApiService.class);
        dictionaryApiService = mock(DictionaryApiService.class);
        wordRepository = mock(WordRepository.class);

        apiService = new ApiService(wordApiService, dictionaryApiService, wordRepository);
    }

    @Test
    void whenWordExistsInDb_thenReturnFromDb() {
        // given
        WordEntity entity = new WordEntity();
        entity.setWord("cachedWord");
        entity.setDefinitions(List.of("meaning1", "meaning2"));
        entity.setPublishedAt(LocalDateTime.now());

        when(wordRepository.findFirstByPublishedAtAfterOrderByPublishedAtDesc(any()))
                .thenReturn(entity);

        // when
        WordOfTheDayResponse response = apiService.getDefinitionAndPosCached();

        // then
        assertThat(response.getWord()).isEqualTo("cachedWord");
        assertThat(response.getDefinitions()).hasSize(2);
        assertThat(response.getDefinitions().get(0).getDefinition()).isEqualTo("meaning1");

        verifyNoInteractions(wordApiService, dictionaryApiService);
    }

    @Test
    void whenWordNotInDb_thenFetchFromApisAndSave() {
        // given
        when(wordRepository.findFirstByPublishedAtAfterOrderByPublishedAtDesc(any()))
                .thenReturn(null);

        when(wordApiService.getWord()).thenReturn(Mono.just("freshWord"));
        when(dictionaryApiService.getDefinitions("freshWord"))
                .thenReturn(Mono.just(List.of(
                        new DefinitionPos("def1", "noun"),
                        new DefinitionPos("def2", "verb")
                )));

        // when
        WordOfTheDayResponse response = apiService.getDefinitionAndPosCached();

        // then
        assertThat(response.getWord()).isEqualTo("freshWord");
        assertThat(response.getDefinitions()).hasSize(2);

        // verify entity saved
        ArgumentCaptor<WordEntity> captor = ArgumentCaptor.forClass(WordEntity.class);
        verify(wordRepository).save(captor.capture());

        WordEntity saved = captor.getValue();
        assertThat(saved.getWord()).isEqualTo("freshWord");
        assertThat(saved.getDefinitions()).containsExactly("def1", "def2");

        verify(wordApiService).getWord();
        verify(dictionaryApiService).getDefinitions("freshWord");
    }

    @Test
    void getWordHistory_returnsAllWordsFromDb() {
        // given
        WordEntity first = new WordEntity();
        first.setWord("word1");
        first.setDefinitions(List.of("def1"));
        first.setPublishedAt(LocalDateTime.now());

        WordEntity second = new WordEntity();
        second.setWord("word2");
        second.setDefinitions(List.of("def2", "def3"));
        second.setPublishedAt(LocalDateTime.now().minusDays(1));

        when(wordRepository.findAllByOrderByPublishedAtDesc())
                .thenReturn(List.of(first, second));

        // when
        List<WordOfTheDayResponse> history = apiService.getWordHistory();

        // then
        assertThat(history).hasSize(2);
        assertThat(history.get(0).getWord()).isEqualTo("word1");
        assertThat(history.get(0).getDefinitions()).hasSize(1);
        assertThat(history.get(1).getWord()).isEqualTo("word2");
        assertThat(history.get(1).getDefinitions()).hasSize(2);
    }
}