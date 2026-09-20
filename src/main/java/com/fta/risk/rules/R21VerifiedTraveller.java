package com.fta.risk.rules;

import com.fta.risk.RuleEvaluation;
import com.fta.risk.RuleOutcome;
import com.fta.risk.RiskContext;
import com.fta.risk.RiskRule;

import java.util.Collections;
import java.util.Set;

/** A verified customer travelling is not the same as a mismatch on a fresh account. */
public final class R21VerifiedTraveller implements RiskRule {

    @Override
    public String id() {
        return "R21";
    }

    @Override
    public int priority() {
        return 210;
    }

    @Override
    public Set<String> requires() {
        return Collections.singleton(ScratchKeys.GEO_MISMATCH);
    }

    @Override
    public RuleOutcome apply(RiskContext ctx, RuleEvaluation eval) {
        if (ctx.flag(ScratchKeys.GEO_MISMATCH) && ctx.kycLevel() >= 2) {
            eval.addScore(-10);
            return RuleOutcome.HIT;
        }
        return RuleOutcome.PASS;
    }
}
