package com.fta.risk.rules;

import com.fta.risk.RuleEvaluation;
import com.fta.risk.RuleOutcome;
import com.fta.risk.RiskContext;
import com.fta.risk.RiskRule;

/** Large ticket. */
public final class R01LargeTicket implements RiskRule {

    @Override
    public String id() {
        return "R01";
    }

    @Override
    public int priority() {
        return 10;
    }

    @Override
    public RuleOutcome apply(RiskContext ctx, RuleEvaluation eval) {
        if (ctx.amountCents() > 500_000L) {
            eval.addScore(25);
            return RuleOutcome.HIT;
        }
        return RuleOutcome.PASS;
    }
}
