package com.fta.risk.rules;

import com.fta.risk.RuleEvaluation;
import com.fta.risk.RuleOutcome;
import com.fta.risk.RiskContext;
import com.fta.risk.RiskRule;

/** Unverified account moving real money. */
public final class R10UnverifiedMoney implements RiskRule {

    @Override
    public String id() {
        return "R10";
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public RuleOutcome apply(RiskContext ctx, RuleEvaluation eval) {
        if (ctx.kycLevel() == 0 && ctx.amountCents() > 100_000L) {
            return RuleOutcome.REJECT;
        }
        return RuleOutcome.PASS;
    }
}
