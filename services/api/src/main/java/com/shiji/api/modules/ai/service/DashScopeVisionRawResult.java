package com.shiji.api.modules.ai.service;

import java.util.List;

public record DashScopeVisionRawResult(
        String outcome, List<Candidate> dishCandidates, List<Candidate> ingredients) {

    public record Candidate(String name, Double confidence) {}
}
