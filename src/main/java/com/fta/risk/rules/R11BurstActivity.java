package com.fta.risk.rules;

import com.fta.risk.RuleEvaluation;
import com.fta.risk.RuleOutcome;
import com.fta.risk.RiskContext;
import com.fta.risk.RiskRule;

/** Burst of transactions. */
public final class R11BurstActivity implements RiskRule {

    @Override
    public String id() {
        return "R11";
    }

    @Override
    public int priority() {
        return 110;
    }

    @Override
    public RuleOutcome apply(RiskContext ctx, RuleEvaluation eval) {
        if (ctx.txCount24h() > 20) {
            eval.addScore(12);
            return RuleOutcome.HIT;
        }
        return RuleOutcome.PASS;
    }
}
