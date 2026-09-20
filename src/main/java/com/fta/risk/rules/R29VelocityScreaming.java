package com.fta.risk.rules;

import com.fta.risk.RuleEvaluation;
import com.fta.risk.RuleOutcome;
import com.fta.risk.RiskContext;
import com.fta.risk.RiskRule;

/** Velocity model screaming. */
public final class R29VelocityScreaming implements RiskRule {

    @Override
    public String id() {
        return "R29";
    }

    @Override
    public int priority() {
        return 290;
    }

    @Override
    public RuleOutcome apply(RiskContext ctx, RuleEvaluation eval) {
        if (ctx.velocityScore() > 95) {
            return RuleOutcome.REJECT;
        }
        return RuleOutcome.PASS;
    }
}
