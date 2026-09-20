package com.fta.risk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Runtime enable/disable through the evaluator's public entry point. */
class RuntimeToggleTest {

    private static RiskContext vpnTransaction() {
        return new RiskContext()
                .amountCents(10_000L)
                .currency("CNY")
                .countryCode("CN")
                .accountAgeDays(400)
                .txCount24h(3)
                .avgAmount30d(50_000L)
                .deviceId("dev-1")
                .deviceFirstSeenDays(30)
                .ipCountry("CN")
                .binCountry("CN")
                .mcc(5812)
                .vpn(true)
                .chargebackCount(0)
                .kycLevel(2)
                .hourOfDay(14)
                .velocityScore(10);
    }

    @Test
    void disablingARuleRemovesItFromLaterEvaluations() {
        RiskEvaluator evaluator = new RiskEvaluator();
        assertTrue(evaluator.evaluate(vpnTransaction()).hitRuleIds().contains("R07"));

        evaluator.setRuleEnabled("R07", false);
        RiskResult after = evaluator.evaluate(vpnTransaction());
        assertFalse(after.hitRuleIds().contains("R07"));
        assertFalse(evaluator.isRuleEnabled("R07"));

        evaluator.setRuleEnabled("R07", true);
        assertTrue(evaluator.evaluate(vpnTransaction()).hitRuleIds().contains("R07"));
    }

    @Test
    void disablingAShortCircuitRuleLetsLaterRulesRun() {
        RiskEvaluator evaluator = new RiskEvaluator();
        RiskContext ctx = vpnTransaction().chargebackCount(4);

        RiskResult before = evaluator.evaluate(ctx);
        assertTrue(before.hitRuleIds().contains("R08"));
        assertFalse(before.hitRuleIds().contains("R11"));

        evaluator.setRuleEnabled("R08", false);
        RiskResult after = evaluator.evaluate(ctx);
        assertFalse(after.hitRuleIds().contains("R08"));
        assertFalse(after.decision() == Decision.REJECT && after.hitRuleIds().contains("R08"));
    }
}
