package com.shiji.api.modules.meal.service;

import com.shiji.api.modules.meal.model.dto.MealErrorCode;
import com.shiji.api.modules.meal.model.dto.response.FoodItemPageResponse;
import com.shiji.api.modules.meal.model.dto.response.FoodItemSummaryResponse;
import com.shiji.api.modules.meal.repository.FoodItemRepository;
import com.shiji.api.modules.meal.service.exception.MealBusinessException;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FoodItemService {

    private static final int MAX_SIZE = 50;

    private final FoodItemRepository foodItemRepository;

    public FoodItemPageResponse search(String q, int page, int size) {
        if (page < 0 || size < 1 || size > MAX_SIZE) {
            throw new MealBusinessException(MealErrorCode.MEAL_REQUEST_INVALID);
        }
        String qq = q == null ? "" : q.trim();
        PageRequest pr = PageRequest.of(page, size);
        Page<Object[]> result = foodItemRepository.searchWithNutrition(qq, pr);
        List<FoodItemSummaryResponse> items =
                result.getContent().stream()
                        .map(
                                row -> {
                                    Long id = ((Number) row[0]).longValue();
                                    String name = (String) row[1];
                                    BigDecimal cal =
                                            row[2] != null ? (BigDecimal) row[2] : null;
                                    return FoodItemSummaryResponse.builder()
                                            .id(id)
                                            .foodName(name)
                                            .caloriesPer100g(cal)
                                            .build();
                                })
                        .toList();
        return FoodItemPageResponse.builder()
                .items(items)
                .hasNext(result.hasNext())
                .page(page)
                .size(size)
                .build();
    }
}
