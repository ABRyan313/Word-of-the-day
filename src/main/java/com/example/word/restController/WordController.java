package com.example.word.restController;

import com.example.word.model.dto.WordOfTheDayResponse;
import com.example.word.service.WordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Tag(name = "Word resource", description = "APIs for random word and its definition")
@RestController
@AllArgsConstructor
@RequestMapping("/wordOfTheDay")
public class WordController {

    private final WordService wordService;

    @Operation(summary = "Get word and its definition")
    @GetMapping
    public Mono<WordOfTheDayResponse> getDefinitionAndPos(){
        return Mono.just(wordService.getDefinitionAndPosCached());
    }
}
