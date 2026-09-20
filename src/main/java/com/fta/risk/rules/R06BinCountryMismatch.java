package com.fta.risk.rules;

import com.fta.risk.RuleEvaluation;
import com.fta.risk.RuleOutcome;
import com.fta.risk.RiskContext;
import com.fta.risk.RiskRule;

/** Card issued outside the declared country. */
public final class R06BinCountryMismatch implements RiskRule {

    @Override
    public String id() {
        return "R06";
    }

    @Override
    public int priority() {
        return 60;
    }

    @Override
    public RuleOutcome apply(RiskContext ctx, RuleEvaluation eval) {
        if (!ctx.binCountry().equals(ctx.countryCode())) {
            eval.addScore(15);
            return RuleOutcome.HIT;
        }
        return RuleOutcome.PASS;
    }
}
