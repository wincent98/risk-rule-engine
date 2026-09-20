package com.fta.risk.rules;

import com.fta.risk.RuleEvaluation;
import com.fta.risk.RuleOutcome;
import com.fta.risk.RiskContext;
import com.fta.risk.RiskRule;

/** Accumulated score over the hard ceiling. */
public final class R30ScoreCeiling implements RiskRule {

    @Override
    public String id() {
        return "R30";
    }

    @Override
    public int priority() {
        return 300;
    }

    @Override
    public RuleOutcome apply(RiskContext ctx, RuleEvaluation eval) {
        if (eval.score() >= 100) {
            return RuleOutcome.REJECT;
        }
        return RuleOutcome.PASS;
    }
}
