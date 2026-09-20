package com.fta.risk;

/**
 * Mutable state of one in-flight evaluation: just the running score. Hits and the
 * short-circuit decision are owned by the engine loop in {@link RiskEvaluator}.
 */
public final class RuleEvaluation {

    private int score;

    public void addScore(int delta) {
        score += delta;
    }

    /** Score accumulated by the rules that have run so far in this evaluation. */
    public int score() {
        return score;
    }
}
