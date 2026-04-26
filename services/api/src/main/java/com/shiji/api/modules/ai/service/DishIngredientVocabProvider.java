package com.shiji.api.modules.ai.service;

import com.shiji.api.modules.meal.model.entity.DishAliasEntity;
import com.shiji.api.modules.meal.model.entity.DishEntity;
import com.shiji.api.modules.meal.model.entity.FoodItemEntity;
import com.shiji.api.modules.meal.repository.DishAliasRepository;
import com.shiji.api.modules.meal.repository.DishRepository;
import com.shiji.api.modules.meal.repository.FoodItemRepository;
import java.util.List;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DishIngredientVocabProvider {

    private final DishRepository dishRepository;
    private final DishAliasRepository dishAliasRepository;
    private final FoodItemRepository foodItemRepository;

    public Snapshot loadSnapshot() {
        List<DishEntity> dishes = dishRepository.findAllByEdibleStatus(1);
        List<DishAliasEntity> aliases = dishAliasRepository.findAllByOrderByIdAsc();
        List<FoodItemEntity> ingredients = foodItemRepository.findAllByEdibleStatus(1);
        return Snapshot.builder().dishes(dishes).aliases(aliases).ingredients(ingredients).build();
    }

    @Builder
    public record Snapshot(
            List<DishEntity> dishes,
            List<DishAliasEntity> aliases,
            List<FoodItemEntity> ingredients) {}
}
