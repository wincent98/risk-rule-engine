package com.fta.risk.rules;

import com.fta.risk.RuleEvaluation;
import com.fta.risk.RuleOutcome;
import com.fta.risk.RiskContext;
import com.fta.risk.RiskRule;

/** Anonymising network. */
public final class R07Vpn implements RiskRule {

    @Override
    public String id() {
        return "R07";
    }

    @Override
    public int priority() {
        return 70;
    }

    @Override
    public RuleOutcome apply(RiskContext ctx, RuleEvaluation eval) {
        if (ctx.vpn()) {
            eval.addScore(18);
            return RuleOutcome.HIT;
        }
        return RuleOutcome.PASS;
    }
}
