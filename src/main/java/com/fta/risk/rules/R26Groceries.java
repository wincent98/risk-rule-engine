package com.fta.risk.rules;

import com.fta.risk.RuleEvaluation;
import com.fta.risk.RuleOutcome;
import com.fta.risk.RiskContext;
import com.fta.risk.RiskRule;

/** Groceries. */
public final class R26Groceries implements RiskRule {

    @Override
    public String id() {
        return "R26";
    }

    @Override
    public int priority() {
        return 260;
    }

    @Override
    public RuleOutcome apply(RiskContext ctx, RuleEvaluation eval) {
        if (ctx.mcc() == 5411) {
            eval.addScore(-8);
            return RuleOutcome.HIT;
        }
        return RuleOutcome.PASS;
    }
}
