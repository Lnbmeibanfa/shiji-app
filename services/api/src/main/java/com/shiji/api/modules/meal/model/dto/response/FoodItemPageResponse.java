package com.shiji.api.modules.meal.model.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FoodItemPageResponse {

    private final List<FoodItemSummaryResponse> items;
    private final boolean hasNext;
    private final int page;
    private final int size;
}
