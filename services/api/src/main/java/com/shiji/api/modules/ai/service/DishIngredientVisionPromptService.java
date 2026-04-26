package com.shiji.api.modules.ai.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

@Service
@RequiredArgsConstructor
public class DishIngredientVisionPromptService {

    private static final String TEMPLATE_PATH = "prompts/dashscope/dish-ingredient-vision-prompt.txt";
    private final DishIngredientVocabProvider vocabProvider;

    private volatile String template;

    public String buildPrompt() {
        DishIngredientVocabProvider.Snapshot snapshot = vocabProvider.loadSnapshot();
        String content = loadTemplate();
        String dishVocab =
                snapshot.dishes().stream()
                        .map(d -> d.getDishName())
                        .distinct()
                        .collect(Collectors.joining("、"));
        String aliasVocab =
                snapshot.aliases().stream()
                        .map(a -> a.getAliasName())
                        .distinct()
                        .collect(Collectors.joining("、"));
        String foodVocab =
                snapshot.ingredients().stream()
                        .map(f -> f.getFoodName())
                        .distinct()
                        .collect(Collectors.joining("、"));
        return content.replace("{{DISH_VOCAB}}", dishVocab)
                .replace("{{DISH_ALIAS_VOCAB}}", aliasVocab)
                .replace("{{FOOD_ITEM_VOCAB}}", foodVocab);
    }

    private String loadTemplate() {
        if (template != null) {
            return template;
        }
        synchronized (this) {
            if (template != null) {
                return template;
            }
            ClassPathResource res = new ClassPathResource(TEMPLATE_PATH);
            try {
                template = StreamUtils.copyToString(res.getInputStream(), StandardCharsets.UTF_8);
                return template;
            } catch (IOException e) {
                throw new IllegalStateException("Failed to read prompt template " + TEMPLATE_PATH, e);
            }
        }
    }
}
