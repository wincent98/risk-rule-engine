package com.fta.risk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RiskEvaluatorTest {

    private final RiskEvaluator evaluator = new RiskEvaluator();

    private static RiskContext clean() {
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
                .vpn(false)
                .chargebackCount(0)
                .kycLevel(2)
                .hourOfDay(14)
                .velocityScore(10);
    }

    @Test
    void aCleanTransactionIsApproved() {
        RiskResult r = evaluator.evaluate(clean());
        assertEquals(Decision.APPROVE, r.decision());
    }

    @Test
    void theHardCeilingShortCircuits() {
        RiskResult r = evaluator.evaluate(clean().amountCents(2_500_000L));
        assertEquals(Decision.REJECT, r.decision());
        assertEquals("R02", r.hitRuleIds().get(r.hitRuleIds().size() - 1));
        assertFalse(r.hitRuleIds().contains("R20"), "rules after the ceiling must not run");
    }

    @Test
    void repeatChargebacksShortCircuit() {
        RiskResult r = evaluator.evaluate(clean().chargebackCount(3));
        assertEquals(Decision.REJECT, r.decision());
        assertTrue(r.hitRuleIds().contains("R08"));
        assertFalse(r.hitRuleIds().contains("R23"));
    }

    @Test
    void unverifiedAccountWithRealMoneyIsRejected() {
        RiskResult r = evaluator.evaluate(clean().kycLevel(0).amountCents(150_000L));
        assertEquals(Decision.REJECT, r.decision());
        assertTrue(r.hitRuleIds().contains("R10"));
    }

    @Test
    void velocityCeilingShortCircuits() {
        RiskResult r = evaluator.evaluate(clean().velocityScore(99));
        assertEquals(Decision.REJECT, r.decision());
        assertTrue(r.hitRuleIds().contains("R29"));
    }

    @Test
    void geoMismatchIsVisibleToLaterRules() {
        RiskResult r = evaluator.evaluate(clean().ipCountry("US").vpn(true));
        assertTrue(r.hitRuleIds().contains("R05"));
        assertTrue(r.hitRuleIds().contains("R12"), "R12 depends on the flag R05 writes");
    }

    @Test
    void foreignCurrencyFeedsTheNormalisedAmountRule() {
        RiskResult r = evaluator.evaluate(clean().currency("USD").amountCents(500_000L));
        assertTrue(r.hitRuleIds().contains("R03"));
        assertTrue(r.hitRuleIds().contains("R14"), "R14 reads the amount R03 writes");
    }

    @Test
    void trustedDeviceLowersTheScore() {
        RiskResult withTrust = evaluator.evaluate(clean().deviceId("trusted-9"));
        RiskResult without = evaluator.evaluate(clean().deviceId("dev-9"));
        assertTrue(withTrust.score() < without.score());
        assertTrue(withTrust.hitRuleIds().contains("R24"));
    }

    @Test
    void hitRuleIdsFollowEvaluationOrder() {
        RiskResult r = evaluator.evaluate(clean().amountCents(600_000L).ipCountry("US"));
        int i01 = r.hitRuleIds().indexOf("R01");
        int i05 = r.hitRuleIds().indexOf("R05");
        assertTrue(i01 >= 0 && i05 > i01, "R01 must come before R05");
    }

    @Test
    void evaluationDoesNotLeakScratchBetweenCalls() {
        RiskContext ctx = clean().ipCountry("US");
        evaluator.evaluate(ctx);
        RiskResult second = evaluator.evaluate(ctx.ipCountry("CN"));
        assertFalse(second.hitRuleIds().contains("R05"));
        assertFalse(second.hitRuleIds().contains("R12"));
    }
}
