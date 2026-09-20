package com.fta.risk;

import java.util.Collections;
import java.util.Set;

/**
 * A single risk rule. Rules are self-contained, ordered by an explicit {@link #priority()}
 * (lower runs first), and declare any scratch keys they read or write so the registry can
 * validate cross-rule dependencies instead of relying on registration order.
 */
public interface RiskRule {

    /** Stable rule id, e.g. "R07". Appears in {@link RiskResult#hitRuleIds()} when the rule hits. */
    String id();

    /** Explicit ordering key. Lower priority rules are evaluated before higher ones. */
    int priority();

    /** Scratch keys this rule writes for later rules. */
    default Set<String> provides() {
        return Collections.emptySet();
    }

    /** Scratch keys this rule reads; each must be provided by an earlier enabled rule. */
    default Set<String> requires() {
        return Collections.emptySet();
    }

    /**
     * Evaluate this rule against the transaction. Implementations may adjust the running
     * score via {@link RuleEvaluation#addScore(int)} and read the score accumulated so far
     * via {@link RuleEvaluation#score()}.
     */
    RuleOutcome apply(RiskContext ctx, RuleEvaluation eval);
}
