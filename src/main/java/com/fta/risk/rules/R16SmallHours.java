package com.fta.risk.rules;

import com.fta.risk.RuleEvaluation;
import com.fta.risk.RuleOutcome;
import com.fta.risk.RiskContext;
import com.fta.risk.RiskRule;

/** Small hours. */
public final class R16SmallHours implements RiskRule {

    @Override
    public String id() {
        return "R16";
    }

    @Override
    public int priority() {
        return 160;
    }

    @Override
    public RuleOutcome apply(RiskContext ctx, RuleEvaluation eval) {
        if (ctx.hourOfDay() < 5) {
            eval.addScore(8);
            return RuleOutcome.HIT;
        }
        return RuleOutcome.PASS;
    }
}
