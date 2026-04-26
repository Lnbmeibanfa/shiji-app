package com.shiji.api.modules.ai.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashScopePingResponse {

    private final String reply;

    private final String model;
}
