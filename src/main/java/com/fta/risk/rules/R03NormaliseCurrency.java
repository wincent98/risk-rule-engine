package com.fta.risk.rules;

import com.fta.risk.RuleEvaluation;
import com.fta.risk.RuleOutcome;
import com.fta.risk.RiskContext;
import com.fta.risk.RiskRule;

import java.util.Collections;
import java.util.Set;

/** Normalise foreign currency into a comparable amount for later rules. */
public final class R03NormaliseCurrency implements RiskRule {

    @Override
    public String id() {
        return "R03";
    }

    @Override
    public int priority() {
        return 30;
    }

    @Override
    public Set<String> provides() {
        return Collections.singleton(ScratchKeys.ADJUSTED_AMOUNT);
    }

    @Override
    public RuleOutcome apply(RiskContext ctx, RuleEvaluation eval) {
        if (!"CNY".equals(ctx.currency())) {
            long adjusted = ctx.amountCents() * 7L;
            ctx.put(ScratchKeys.ADJUSTED_AMOUNT, adjusted);
            eval.addScore(5);
            return RuleOutcome.HIT;
        }
        ctx.put(ScratchKeys.ADJUSTED_AMOUNT, ctx.amountCents());
        return RuleOutcome.PASS;
    }
}
