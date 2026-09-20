package com.fta.risk.rules;

import com.fta.risk.RuleEvaluation;
import com.fta.risk.RuleOutcome;
import com.fta.risk.RiskContext;
import com.fta.risk.RiskRule;

import java.util.Collections;
import java.util.Set;

/** Geo mismatch behind a vpn is much worse than either alone. */
public final class R12GeoMismatchVpn implements RiskRule {

    @Override
    public String id() {
        return "R12";
    }

    @Override
    public int priority() {
        return 120;
    }

    @Override
    public Set<String> requires() {
        return Collections.singleton(ScratchKeys.GEO_MISMATCH);
    }

    @Override
    public RuleOutcome apply(RiskContext ctx, RuleEvaluation eval) {
        if (ctx.flag(ScratchKeys.GEO_MISMATCH) && ctx.vpn()) {
            eval.addScore(25);
            return RuleOutcome.HIT;
        }
        return RuleOutcome.PASS;
    }
}
