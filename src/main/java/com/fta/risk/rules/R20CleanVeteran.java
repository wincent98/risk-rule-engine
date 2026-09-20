package com.fta.risk.rules;

import com.fta.risk.RuleEvaluation;
import com.fta.risk.RuleOutcome;
import com.fta.risk.RiskContext;
import com.fta.risk.RiskRule;

/** Long standing clean account. */
public final class R20CleanVeteran implements RiskRule {

    @Override
    public String id() {
        return "R20";
    }

    @Override
    public int priority() {
        return 200;
    }

    @Override
    public RuleOutcome apply(RiskContext ctx, RuleEvaluation eval) {
        if (ctx.accountAgeDays() > 365 && ctx.chargebackCount() == 0) {
            eval.addScore(-15);
            return RuleOutcome.HIT;
        }
        return RuleOutcome.PASS;
    }
}
