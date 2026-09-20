package com.fta.risk.rules;

import com.fta.risk.RuleEvaluation;
import com.fta.risk.RuleOutcome;
import com.fta.risk.RiskContext;
import com.fta.risk.RiskRule;

import java.util.Collections;
import java.util.Set;

/** Trusted device on a low band pulls the score down one last notch. */
public final class R32TrustedLowBand implements RiskRule {

    @Override
    public String id() {
        return "R32";
    }

    @Override
    public int priority() {
        return 320;
    }

    @Override
    public Set<String> requires() {
        return Collections.singleton(ScratchKeys.RISK_BAND);
    }

    @Override
    public RuleOutcome apply(RiskContext ctx, RuleEvaluation eval) {
        if ("LOW".equals(ctx.text(ScratchKeys.RISK_BAND)) && ctx.deviceId().startsWith("trusted-")) {
            eval.addScore(-6);
            return RuleOutcome.HIT;
        }
        return RuleOutcome.PASS;
    }
}
