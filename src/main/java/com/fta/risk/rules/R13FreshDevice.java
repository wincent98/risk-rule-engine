package com.fta.risk.rules;

import com.fta.risk.RuleEvaluation;
import com.fta.risk.RuleOutcome;
import com.fta.risk.RiskContext;
import com.fta.risk.RiskRule;

/** Device seen for the first time today. */
public final class R13FreshDevice implements RiskRule {

    @Override
    public String id() {
        return "R13";
    }

    @Override
    public int priority() {
        return 130;
    }

    @Override
    public RuleOutcome apply(RiskContext ctx, RuleEvaluation eval) {
        if (ctx.deviceFirstSeenDays() == 0) {
            eval.addScore(22);
            return RuleOutcome.HIT;
        }
        return RuleOutcome.PASS;
    }
}
