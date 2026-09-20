package com.fta.risk.rules;

import com.fta.risk.RuleEvaluation;
import com.fta.risk.RuleOutcome;
import com.fta.risk.RiskContext;
import com.fta.risk.RiskRule;

/** Fully verified. */
public final class R23FullyVerified implements RiskRule {

    @Override
    public String id() {
        return "R23";
    }

    @Override
    public int priority() {
        return 230;
    }

    @Override
    public RuleOutcome apply(RiskContext ctx, RuleEvaluation eval) {
        if (ctx.kycLevel() >= 3) {
            eval.addScore(-12);
            return RuleOutcome.HIT;
        }
        return RuleOutcome.PASS;
    }
}
