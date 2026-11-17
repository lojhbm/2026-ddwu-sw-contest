package com.contest.una.event.ai;

import java.util.List;

public record AssignmentAnalysis(
        List<AssignmentCandidate> candidates,
        List<String> warnings
) {
}
