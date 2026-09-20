package com.fta.risk;

import java.util.Collections;
import java.util.List;

public final class RiskResult {

    private final Decision decision;
    private final List<String> hitRuleIds;
    private final int score;

    public RiskResult(Decision decision, List<String> hitRuleIds, int score) {
        this.decision = decision;
        this.hitRuleIds = Collections.unmodifiableList(hitRuleIds);
        this.score = score;
    }

    public Decision decision() {
        return decision;
    }

    public List<String> hitRuleIds() {
        return hitRuleIds;
    }

    public int score() {
        return score;
    }

    @Override
    public String toString() {
        return decision + "|" + String.join(",", hitRuleIds) + "|" + score;
    }
}
