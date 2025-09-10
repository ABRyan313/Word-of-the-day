package com.example.word.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class WordApiService {

    private final WebClient webClient;

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
}
