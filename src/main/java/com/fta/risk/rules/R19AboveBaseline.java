package com.fta.risk.rules;

import com.fta.risk.RuleEvaluation;
import com.fta.risk.RuleOutcome;
import com.fta.risk.RiskContext;
import com.fta.risk.RiskRule;

/** Way above the customer's own baseline. */
public final class R19AboveBaseline implements RiskRule {

    @Override
    public String id() {
        return "R19";
    }

    @Override
    public int priority() {
        return 190;
    }

    @Override
    public RuleOutcome apply(RiskContext ctx, RuleEvaluation eval) {
        if (ctx.avgAmount30d() > 0L && ctx.amountCents() > ctx.avgAmount30d() * 5L) {
            eval.addScore(26);
            return RuleOutcome.HIT;
        }
        return RuleOutcome.PASS;
    }
}
