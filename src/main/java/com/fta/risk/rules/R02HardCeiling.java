package com.fta.risk.rules;

import com.fta.risk.RuleEvaluation;
import com.fta.risk.RuleOutcome;
import com.fta.risk.RiskContext;
import com.fta.risk.RiskRule;

/** Hard ceiling, nothing below this line runs once it fires. */
public final class R02HardCeiling implements RiskRule {

    @Override
    public String id() {
        return "R02";
    }

    @Override
    public int priority() {
        return 20;
    }

    @Override
    public RuleOutcome apply(RiskContext ctx, RuleEvaluation eval) {
        if (ctx.amountCents() > 2_000_000L) {
            return RuleOutcome.REJECT;
        }
        return RuleOutcome.PASS;
    }
}
