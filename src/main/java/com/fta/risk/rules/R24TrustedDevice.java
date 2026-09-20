package com.fta.risk.rules;

import com.fta.risk.RuleEvaluation;
import com.fta.risk.RuleOutcome;
import com.fta.risk.RiskContext;
import com.fta.risk.RiskRule;

/** Device on the allow list. */
public final class R24TrustedDevice implements RiskRule {

    @Override
    public String id() {
        return "R24";
    }

    @Override
    public int priority() {
        return 240;
    }

    @Override
    public RuleOutcome apply(RiskContext ctx, RuleEvaluation eval) {
        if (ctx.deviceId().startsWith("trusted-")) {
            eval.addScore(-20);
            return RuleOutcome.HIT;
        }
        return RuleOutcome.PASS;
    }
}
