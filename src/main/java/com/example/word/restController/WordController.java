package com.example.word.restController;

import com.example.word.model.dto.WordOfTheDayResponse;
import com.example.word.service.ApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import java.util.List;

/**
 * REST controller for word-related resources.
 * Provides endpoints to get the word of the day and its definition,
 * as well as the history of previously published words.
 */

@Tag(name = "Word resource", description = "APIs for random word and its definitions and part of speech")
@RestController
@AllArgsConstructor
@RequestMapping("/wordOfTheDay")
public class WordController {

    private final ApiService apiService;


    @Operation(summary = "Get word and its definition")
    @GetMapping
    public Mono<WordOfTheDayResponse> getDefinitionAndPos(){
        return Mono.just(apiService.getDefinitionAndPosCached());
    }

    @Operation(summary = "Get history of words")
    @GetMapping("/history")
    public List<WordOfTheDayResponse> getWordHistory() {
      return apiService.getWordHistory();
    }
}
