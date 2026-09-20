package com.fta.risk.rules;

import com.fta.risk.RuleEvaluation;
import com.fta.risk.RuleOutcome;
import com.fta.risk.RiskContext;
import com.fta.risk.RiskRule;

/** Dormant account waking up. */
public final class R27DormantWake implements RiskRule {

    @Override
    public String id() {
        return "R27";
    }

    @Override
    public int priority() {
        return 270;
    }

    @Override
    public RuleOutcome apply(RiskContext ctx, RuleEvaluation eval) {
        if (ctx.txCount24h() == 0) {
            eval.addScore(6);
            return RuleOutcome.HIT;
        }
        return RuleOutcome.PASS;
    }
}
