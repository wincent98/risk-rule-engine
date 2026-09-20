package com.fta.risk.rules;

import com.fta.risk.RuleEvaluation;
import com.fta.risk.RuleOutcome;
import com.fta.risk.RiskContext;
import com.fta.risk.RiskRule;

import java.util.Collections;
import java.util.Set;

/** Ip does not match the declared country, later rules key off this. */
public final class R05GeoMismatch implements RiskRule {

    @Override
    public String id() {
        return "R05";
    }

    @Override
    public int priority() {
        return 50;
    }

    @Override
    public Set<String> provides() {
        return Collections.singleton(ScratchKeys.GEO_MISMATCH);
    }

    @Override
    public RuleOutcome apply(RiskContext ctx, RuleEvaluation eval) {
        if (!ctx.ipCountry().equals(ctx.countryCode())) {
            ctx.put(ScratchKeys.GEO_MISMATCH, Boolean.TRUE);
            eval.addScore(20);
            return RuleOutcome.HIT;
        }
        return RuleOutcome.PASS;
    }
}
