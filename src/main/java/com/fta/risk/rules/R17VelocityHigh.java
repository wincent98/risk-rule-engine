package com.fta.risk.rules;

import com.fta.risk.RuleEvaluation;
import com.fta.risk.RuleOutcome;
import com.fta.risk.RiskContext;
import com.fta.risk.RiskRule;

/** Velocity model. */
public final class R17VelocityHigh implements RiskRule {

    @Override
    public String id() {
        return "R17";
    }

    @Override
    public int priority() {
        return 170;
    }

    @Override
    public RuleOutcome apply(RiskContext ctx, RuleEvaluation eval) {
        if (ctx.velocityScore() > 80) {
            eval.addScore(24);
            return RuleOutcome.HIT;
        }
        return RuleOutcome.PASS;
    }
}
