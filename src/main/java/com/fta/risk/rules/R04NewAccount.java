package com.fta.risk.rules;

import com.fta.risk.RuleEvaluation;
import com.fta.risk.RuleOutcome;
import com.fta.risk.RiskContext;
import com.fta.risk.RiskRule;

/** Brand new account. */
public final class R04NewAccount implements RiskRule {

    @Override
    public String id() {
        return "R04";
    }

    @Override
    public int priority() {
        return 40;
    }

    @Override
    public RuleOutcome apply(RiskContext ctx, RuleEvaluation eval) {
        if (ctx.accountAgeDays() < 7) {
            eval.addScore(30);
            return RuleOutcome.HIT;
        }
        return RuleOutcome.PASS;
    }
}
