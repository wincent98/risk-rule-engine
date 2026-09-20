package com.fta.risk.rules;

import com.fta.risk.RuleEvaluation;
import com.fta.risk.RuleOutcome;
import com.fta.risk.RiskContext;
import com.fta.risk.RiskRule;

import java.util.Collections;
import java.util.Set;

/** Reads the band written by R09. */
public final class R18HighBandBurst implements RiskRule {

    @Override
    public String id() {
        return "R18";
    }

    @Override
    public int priority() {
        return 180;
    }

    @Override
    public Set<String> requires() {
        return Collections.singleton(ScratchKeys.RISK_BAND);
    }

    @Override
    public RuleOutcome apply(RiskContext ctx, RuleEvaluation eval) {
        if ("HIGH".equals(ctx.text(ScratchKeys.RISK_BAND)) && ctx.txCount24h() > 10) {
            return RuleOutcome.REJECT;
        }
        return RuleOutcome.PASS;
    }
}
