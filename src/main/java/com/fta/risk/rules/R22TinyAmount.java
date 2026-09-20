package com.fta.risk.rules;

import com.fta.risk.RuleEvaluation;
import com.fta.risk.RuleOutcome;
import com.fta.risk.RiskContext;
import com.fta.risk.RiskRule;

import java.util.Collections;
import java.util.Set;

/** Tiny normalised amount. */
public final class R22TinyAmount implements RiskRule {

    @Override
    public String id() {
        return "R22";
    }

    @Override
    public int priority() {
        return 220;
    }

    @Override
    public Set<String> requires() {
        return Collections.singleton(ScratchKeys.ADJUSTED_AMOUNT);
    }

    @Override
    public RuleOutcome apply(RiskContext ctx, RuleEvaluation eval) {
        if (ctx.longValue(ScratchKeys.ADJUSTED_AMOUNT, 0L) < 1_000L) {
            eval.addScore(-5);
            return RuleOutcome.HIT;
        }
        return RuleOutcome.PASS;
    }
}
