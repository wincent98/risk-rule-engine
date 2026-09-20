package com.fta.risk.rules;

import com.fta.risk.RuleEvaluation;
import com.fta.risk.RuleOutcome;
import com.fta.risk.RiskContext;
import com.fta.risk.RiskRule;

/** Suspiciously round amount. */
public final class R28RoundAmount implements RiskRule {

    @Override
    public String id() {
        return "R28";
    }

    @Override
    public int priority() {
        return 280;
    }

    @Override
    public RuleOutcome apply(RiskContext ctx, RuleEvaluation eval) {
        if (ctx.amountCents() >= 100_000L && ctx.amountCents() % 100_000L == 0L) {
            eval.addScore(9);
            return RuleOutcome.HIT;
        }
        return RuleOutcome.PASS;
    }
}
