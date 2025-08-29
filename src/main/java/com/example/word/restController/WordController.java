package com.example.word.restController;

import com.example.word.model.domain.DefinitionPos;
import com.example.word.model.dto.WordOfTheDayResponse;
import com.example.word.persistence.WordEntity;
import com.example.word.persistence.WordRepository;
import com.example.word.service.WordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import java.util.stream.Collectors;
import java.util.List;

@Tag(name = "Word resource", description = "APIs for random word and its definition")
@RestController
@AllArgsConstructor
@RequestMapping("/wordOfTheDay")
public class WordController {

    private final WordService wordService;
    private final WordRepository wordRepository;

    @Operation(summary = "Get word and its definition")
    @GetMapping
    public Mono<WordOfTheDayResponse> getDefinitionAndPos(){
        return Mono.just(wordService.getDefinitionAndPosCached());
    }

    @Operation(summary = "Get history of words")
    @GetMapping("/history")
    public List<WordOfTheDayResponse> getWordHistory() {
        List<WordEntity> entities = wordRepository.findAllByOrderByPublishedAtDesc();

        return entities.stream()
                .map(entity -> new WordOfTheDayResponse(
                        entity.getWord(),
                        entity.getDefinitions().stream()
                                .map(def -> new DefinitionPos(def, ""))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }
}
