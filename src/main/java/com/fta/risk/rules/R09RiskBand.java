package com.fta.risk.rules;

import com.fta.risk.RuleEvaluation;
import com.fta.risk.RuleOutcome;
import com.fta.risk.RiskContext;
import com.fta.risk.RiskRule;

import java.util.Collections;
import java.util.Set;

/** Bucket the score so far, read back by R18, R31 and R32. */
public final class R09RiskBand implements RiskRule {

    @Override
    public String id() {
        return "R09";
    }

    @Override
    public int priority() {
        return 90;
    }

    @Override
    public Set<String> provides() {
        return Collections.singleton(ScratchKeys.RISK_BAND);
    }

    @Override
    public RuleOutcome apply(RiskContext ctx, RuleEvaluation eval) {
        if (eval.score() >= 60) {
            ctx.put(ScratchKeys.RISK_BAND, "HIGH");
            return RuleOutcome.HIT;
        }
        if (eval.score() >= 30) {
            ctx.put(ScratchKeys.RISK_BAND, "MID");
            return RuleOutcome.HIT;
        }
        ctx.put(ScratchKeys.RISK_BAND, "LOW");
        return RuleOutcome.PASS;
    }
}
