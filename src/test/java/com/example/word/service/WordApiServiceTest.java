package com.example.word.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static reactor.core.publisher.Mono.when;

@ExtendWith(MockitoExtension.class)
public class WordApiServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WebClient webClient;

    private WordApiService wordApiService;

    @BeforeEach
    void setUp() {
        wordApiService = new WordApiService(webClient);
    }

    @Test
    void getWord_returnsRandomWord() {
        String[] mockResponse = {"example"};

        when(webClient.get()
                .uri(anyString())
                .retrieve()
                .bodyToMono(String[].class))
                .thenReturn(Mono.just(mockResponse));

        StepVerifier.create(wordApiService.getWord())
                .expectNext("example")
                .verifyComplete();
    }

}
