package com.fta.risk.rules;

import com.fta.risk.RuleEvaluation;
import com.fta.risk.RuleOutcome;
import com.fta.risk.RiskContext;
import com.fta.risk.RiskRule;

/** Repeat chargebacks are an immediate stop. */
public final class R08ChargebackStop implements RiskRule {

    @Override
    public String id() {
        return "R08";
    }

    @Override
    public int priority() {
        return 80;
    }

    @Override
    public RuleOutcome apply(RiskContext ctx, RuleEvaluation eval) {
        if (ctx.chargebackCount() >= 3) {
            return RuleOutcome.REJECT;
        }
        return RuleOutcome.PASS;
    }
}
