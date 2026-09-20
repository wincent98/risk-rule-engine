package com.fta.risk.rules;

import com.fta.risk.RuleEvaluation;
import com.fta.risk.RuleOutcome;
import com.fta.risk.RiskContext;
import com.fta.risk.RiskRule;

import java.util.Collections;
import java.util.Set;

/** Mid band plus a fresh device still deserves a look. */
public final class R31MidBandFreshDevice implements RiskRule {

    @Override
    public String id() {
        return "R31";
    }

    @Override
    public int priority() {
        return 310;
    }

    @Override
    public Set<String> requires() {
        return Collections.singleton(ScratchKeys.RISK_BAND);
    }

    @Override
    public RuleOutcome apply(RiskContext ctx, RuleEvaluation eval) {
        if ("MID".equals(ctx.text(ScratchKeys.RISK_BAND)) && ctx.deviceFirstSeenDays() < 3) {
            eval.addScore(7);
            return RuleOutcome.HIT;
        }
        return RuleOutcome.PASS;
    }
}
