package com.fta.risk.rules;

import com.fta.risk.RuleEvaluation;
import com.fta.risk.RuleOutcome;
import com.fta.risk.RiskContext;
import com.fta.risk.RiskRule;

/** High risk merchant categories: atm, gambling, wire transfer. */
public final class R15HighRiskMcc implements RiskRule {

    @Override
    public String id() {
        return "R15";
    }

    @Override
    public int priority() {
        return 150;
    }

    @Override
    public RuleOutcome apply(RiskContext ctx, RuleEvaluation eval) {
        if (ctx.mcc() == 6011 || ctx.mcc() == 7995 || ctx.mcc() == 4829) {
            eval.addScore(28);
            return RuleOutcome.HIT;
        }
        return RuleOutcome.PASS;
    }
}
