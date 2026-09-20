package com.fta.risk.rules;

import com.fta.risk.RuleEvaluation;
import com.fta.risk.RuleOutcome;
import com.fta.risk.RiskContext;
import com.fta.risk.RiskRule;

/** Domestic usd corridor. */
public final class R25DomesticUsd implements RiskRule {

    @Override
    public String id() {
        return "R25";
    }

    @Override
    public int priority() {
        return 250;
    }

    @Override
    public RuleOutcome apply(RiskContext ctx, RuleEvaluation eval) {
        if ("USD".equals(ctx.currency()) && "US".equals(ctx.countryCode())) {
            eval.addScore(-5);
            return RuleOutcome.HIT;
        }
        return RuleOutcome.PASS;
    }
}
