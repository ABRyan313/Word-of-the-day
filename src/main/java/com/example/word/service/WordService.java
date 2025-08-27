package com.example.word.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@AllArgsConstructor
@Service
public class WordService {

    private final WebClient webClient;

    public Mono<String> getWord() {
        String randomWordApi ="https://random-word-api.herokuapp.com/word";
        return webClient
                .get()
                .uri(randomWordApi)
                .retrieve()
                .bodyToMono(String[].class)
                .map(words -> words[0]);
    }

    public Mono<String> wordDefinition(String word){
        String definitionApi ="https://api.dictionaryapi.dev/api/v2/entries/en/" + word;
        return webClient
                 .get()
                 .uri(definitionApi)
                 .retrieve()
                 .bodyToMono(String.class)
                 .onErrorResume(e -> Mono.just("Definition not found"));
     }

    public Mono<Map<String, String>> getWordAndDefinition(){
        return getWord()
                .flatMap(word -> wordDefinition(word)
                        .map(definition -> Map.of("word", word, "definition", definition)));
    }
}
