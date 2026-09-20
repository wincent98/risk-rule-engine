package com.fta.risk.rules;

import com.fta.risk.RuleEvaluation;
import com.fta.risk.RuleOutcome;
import com.fta.risk.RiskContext;
import com.fta.risk.RiskRule;

import java.util.Collections;
import java.util.Set;

/** Works off the normalised amount from R03. */
public final class R14LargeAdjustedAmount implements RiskRule {

    @Override
    public String id() {
        return "R14";
    }

    @Override
    public int priority() {
        return 140;
    }

    @Override
    public Set<String> requires() {
        return Collections.singleton(ScratchKeys.ADJUSTED_AMOUNT);
    }

    @Override
    public RuleOutcome apply(RiskContext ctx, RuleEvaluation eval) {
        if (ctx.longValue(ScratchKeys.ADJUSTED_AMOUNT, 0L) > 3_000_000L) {
            eval.addScore(20);
            return RuleOutcome.HIT;
        }
        return RuleOutcome.PASS;
    }
}
