package com.fta.risk.rules;

import com.fta.risk.Decision;
import com.fta.risk.RiskContext;
import com.fta.risk.RiskEvaluator;
import com.fta.risk.RiskResult;
import com.fta.risk.RiskRule;
import com.fta.risk.RuleRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * Hand-inlined compilation of the default rule set.
 *
 * The generic engine loop in {@link RiskEvaluator} pays one megamorphic call per rule,
 * which the JIT cannot fully inline past its node budget; that costs ~3x throughput. This
 * class is the same logic written straight-line, one block per rule, each guarded by the
 * rule's bit in the enabled mask, so the JIT sees exactly the shape of the old
 * hand-written evaluate method. Behaviour is locked to the rule objects by
 * EngineEquivalenceTest (golden samples plus random toggle combinations).
 *
 * Maintenance rule: this class must mirror DefaultRules rule-for-rule. If a rule is added
 * to or removed from DefaultRules, {@link #createIfMatches} stops matching and the
 * evaluator silently falls back to the (correct, slower) generic loop, so a forgotten
 * update degrades throughput, never correctness.
 *
 * Snapshot semantics: the caller reads the mask once per evaluation, so an in-flight
 * evaluation always runs to completion with the rule set it started with.
 */
public final class DefaultPipeline {

    private static final String[] EXPECTED_IDS = {
            "R01", "R02", "R03", "R04", "R05", "R06", "R07", "R08",
            "R09", "R10", "R11", "R12", "R13", "R14", "R15", "R16",
            "R17", "R18", "R19", "R20", "R21", "R22", "R23", "R24",
            "R25", "R26", "R27", "R28", "R29", "R30", "R31", "R32"
    };

    private static final long M01 = 1L << 0;
    private static final long M02 = 1L << 1;
    private static final long M03 = 1L << 2;
    private static final long M04 = 1L << 3;
    private static final long M05 = 1L << 4;
    private static final long M06 = 1L << 5;
    private static final long M07 = 1L << 6;
    private static final long M08 = 1L << 7;
    private static final long M09 = 1L << 8;
    private static final long M10 = 1L << 9;
    private static final long M11 = 1L << 10;
    private static final long M12 = 1L << 11;
    private static final long M13 = 1L << 12;
    private static final long M14 = 1L << 13;
    private static final long M15 = 1L << 14;
    private static final long M16 = 1L << 15;
    private static final long M17 = 1L << 16;
    private static final long M18 = 1L << 17;
    private static final long M19 = 1L << 18;
    private static final long M20 = 1L << 19;
    private static final long M21 = 1L << 20;
    private static final long M22 = 1L << 21;
    private static final long M23 = 1L << 22;
    private static final long M24 = 1L << 23;
    private static final long M25 = 1L << 24;
    private static final long M26 = 1L << 25;
    private static final long M27 = 1L << 26;
    private static final long M28 = 1L << 27;
    private static final long M29 = 1L << 28;
    private static final long M30 = 1L << 29;
    private static final long M31 = 1L << 30;
    private static final long M32 = 1L << 31;

    /**
     * @return a pipeline aligned with the registry's rule order, or null when the registry
     *         does not hold exactly the default rule set (bit i must mean rule i)
     */
    public static DefaultPipeline createIfMatches(RuleRegistry registry) {
        List<RiskRule> ordered = registry.all();
        if (ordered.size() != EXPECTED_IDS.length) {
            return null;
        }
        for (int i = 0; i < EXPECTED_IDS.length; i++) {
            if (!EXPECTED_IDS[i].equals(ordered.get(i).id())) {
                return null;
            }
        }
        return new DefaultPipeline();
    }

    private DefaultPipeline() {
    }

    /** Mask with all 32 default rules enabled. */
    private static final long FULL_MASK = 0xFFFFFFFFL;

    public RiskResult evaluate(RiskContext ctx, long mask) {
        if (mask == FULL_MASK) {
            return evaluateFull(ctx);
        }
        return evaluateMasked(ctx, mask);
    }

    /**
     * All rules enabled: the exact shape of the original hand-written evaluate method,
     * no mask tests on the hot path.
     */
    private RiskResult evaluateFull(RiskContext ctx) {
        List<String> hits = new ArrayList<>();
        int score = 0;

        // R01 large ticket
        if (ctx.amountCents() > 500_000L) {
            score += 25;
            hits.add("R01");
        }

        // R02 hard ceiling, nothing below this line runs once it fires
        if (ctx.amountCents() > 2_000_000L) {
            hits.add("R02");
            return new RiskResult(Decision.REJECT, hits, score);
        }

        // R03 normalise foreign currency into a comparable amount for later rules
        if (!"CNY".equals(ctx.currency())) {
            long adjusted = ctx.amountCents() * 7L;
            ctx.put(ScratchKeys.ADJUSTED_AMOUNT, adjusted);
            score += 5;
            hits.add("R03");
        } else {
            ctx.put(ScratchKeys.ADJUSTED_AMOUNT, ctx.amountCents());
        }

        // R04 brand new account
        if (ctx.accountAgeDays() < 7) {
            score += 30;
            hits.add("R04");
        }

        // R05 ip does not match the declared country, later rules key off this
        if (!ctx.ipCountry().equals(ctx.countryCode())) {
            ctx.put(ScratchKeys.GEO_MISMATCH, Boolean.TRUE);
            score += 20;
            hits.add("R05");
        }

        // R06 card issued outside the declared country
        if (!ctx.binCountry().equals(ctx.countryCode())) {
            score += 15;
            hits.add("R06");
        }

        // R07 anonymising network
        if (ctx.vpn()) {
            score += 18;
            hits.add("R07");
        }

        // R08 repeat chargebacks are an immediate stop
        if (ctx.chargebackCount() >= 3) {
            hits.add("R08");
            return new RiskResult(Decision.REJECT, hits, score);
        }

        // R09 bucket the score so far, read back by R18, R31 and R32
        if (score >= 60) {
            ctx.put(ScratchKeys.RISK_BAND, "HIGH");
            hits.add("R09");
        } else if (score >= 30) {
            ctx.put(ScratchKeys.RISK_BAND, "MID");
            hits.add("R09");
        } else {
            ctx.put(ScratchKeys.RISK_BAND, "LOW");
        }

        // R10 unverified account moving real money
        if (ctx.kycLevel() == 0 && ctx.amountCents() > 100_000L) {
            hits.add("R10");
            return new RiskResult(Decision.REJECT, hits, score);
        }

        // R11 burst of transactions
        if (ctx.txCount24h() > 20) {
            score += 12;
            hits.add("R11");
        }

        // R12 geo mismatch behind a vpn is much worse than either alone
        if (ctx.flag(ScratchKeys.GEO_MISMATCH) && ctx.vpn()) {
            score += 25;
            hits.add("R12");
        }

        // R13 device seen for the first time today
        if (ctx.deviceFirstSeenDays() == 0) {
            score += 22;
            hits.add("R13");
        }

        // R14 works off the normalised amount from R03
        if (ctx.longValue(ScratchKeys.ADJUSTED_AMOUNT, 0L) > 3_000_000L) {
            score += 20;
            hits.add("R14");
        }

        // R15 high risk merchant categories: atm, gambling, wire transfer
        if (ctx.mcc() == 6011 || ctx.mcc() == 7995 || ctx.mcc() == 4829) {
            score += 28;
            hits.add("R15");
        }

        // R16 small hours
        if (ctx.hourOfDay() < 5) {
            score += 8;
            hits.add("R16");
        }

        // R17 velocity model
        if (ctx.velocityScore() > 80) {
            score += 24;
            hits.add("R17");
        }

        // R18 reads the band written by R09
        if ("HIGH".equals(ctx.text(ScratchKeys.RISK_BAND)) && ctx.txCount24h() > 10) {
            hits.add("R18");
            return new RiskResult(Decision.REJECT, hits, score);
        }

        // R19 way above the customer's own baseline
        if (ctx.avgAmount30d() > 0L && ctx.amountCents() > ctx.avgAmount30d() * 5L) {
            score += 26;
            hits.add("R19");
        }

        // R20 long standing clean account
        if (ctx.accountAgeDays() > 365 && ctx.chargebackCount() == 0) {
            score -= 15;
            hits.add("R20");
        }

        // R21 a verified customer travelling is not the same as a mismatch on a fresh account
        if (ctx.flag(ScratchKeys.GEO_MISMATCH) && ctx.kycLevel() >= 2) {
            score -= 10;
            hits.add("R21");
        }

        // R22 tiny normalised amount
        if (ctx.longValue(ScratchKeys.ADJUSTED_AMOUNT, 0L) < 1_000L) {
            score -= 5;
            hits.add("R22");
        }

        // R23 fully verified
        if (ctx.kycLevel() >= 3) {
            score -= 12;
            hits.add("R23");
        }

        // R24 device on the allow list
        if (ctx.deviceId().startsWith("trusted-")) {
            score -= 20;
            hits.add("R24");
        }

        // R25 domestic usd corridor
        if ("USD".equals(ctx.currency()) && "US".equals(ctx.countryCode())) {
            score -= 5;
            hits.add("R25");
        }

        // R26 groceries
        if (ctx.mcc() == 5411) {
            score -= 8;
            hits.add("R26");
        }

        // R27 dormant account waking up
        if (ctx.txCount24h() == 0) {
            score += 6;
            hits.add("R27");
        }

        // R28 suspiciously round amount
        if (ctx.amountCents() >= 100_000L && ctx.amountCents() % 100_000L == 0L) {
            score += 9;
            hits.add("R28");
        }

        // R29 velocity model screaming
        if (ctx.velocityScore() > 95) {
            hits.add("R29");
            return new RiskResult(Decision.REJECT, hits, score);
        }

        // R30 accumulated score over the hard ceiling
        if (score >= 100) {
            hits.add("R30");
            return new RiskResult(Decision.REJECT, hits, score);
        }

        // R31 mid band plus a fresh device still deserves a look
        if ("MID".equals(ctx.text(ScratchKeys.RISK_BAND)) && ctx.deviceFirstSeenDays() < 3) {
            score += 7;
            hits.add("R31");
        }

        // R32 trusted device on a low band pulls the score down one last notch
        if ("LOW".equals(ctx.text(ScratchKeys.RISK_BAND)) && ctx.deviceId().startsWith("trusted-")) {
            score -= 6;
            hits.add("R32");
        }

        Decision decision = score >= RiskEvaluator.REVIEW_THRESHOLD ? Decision.REVIEW : Decision.APPROVE;
        return new RiskResult(decision, hits, score);
    }

    /** General case: each rule guarded by its bit in the enabled mask. */
    private RiskResult evaluateMasked(RiskContext ctx, long mask) {
        List<String> hits = new ArrayList<>();
        int score = 0;

        // R01 large ticket
        if ((mask & M01) != 0L && ctx.amountCents() > 500_000L) {
            score += 25;
            hits.add("R01");
        }

        // R02 hard ceiling, nothing below this line runs once it fires
        if ((mask & M02) != 0L && ctx.amountCents() > 2_000_000L) {
            hits.add("R02");
            return new RiskResult(Decision.REJECT, hits, score);
        }

        // R03 normalise foreign currency into a comparable amount for later rules
        if ((mask & M03) != 0L) {
            if (!"CNY".equals(ctx.currency())) {
                long adjusted = ctx.amountCents() * 7L;
                ctx.put(ScratchKeys.ADJUSTED_AMOUNT, adjusted);
                score += 5;
                hits.add("R03");
            } else {
                ctx.put(ScratchKeys.ADJUSTED_AMOUNT, ctx.amountCents());
            }
        }

        // R04 brand new account
        if ((mask & M04) != 0L && ctx.accountAgeDays() < 7) {
            score += 30;
            hits.add("R04");
        }

        // R05 ip does not match the declared country, later rules key off this
        if ((mask & M05) != 0L && !ctx.ipCountry().equals(ctx.countryCode())) {
            ctx.put(ScratchKeys.GEO_MISMATCH, Boolean.TRUE);
            score += 20;
            hits.add("R05");
        }

        // R06 card issued outside the declared country
        if ((mask & M06) != 0L && !ctx.binCountry().equals(ctx.countryCode())) {
            score += 15;
            hits.add("R06");
        }

        // R07 anonymising network
        if ((mask & M07) != 0L && ctx.vpn()) {
            score += 18;
            hits.add("R07");
        }

        // R08 repeat chargebacks are an immediate stop
        if ((mask & M08) != 0L && ctx.chargebackCount() >= 3) {
            hits.add("R08");
            return new RiskResult(Decision.REJECT, hits, score);
        }

        // R09 bucket the score so far, read back by R18, R31 and R32
        if ((mask & M09) != 0L) {
            if (score >= 60) {
                ctx.put(ScratchKeys.RISK_BAND, "HIGH");
                hits.add("R09");
            } else if (score >= 30) {
                ctx.put(ScratchKeys.RISK_BAND, "MID");
                hits.add("R09");
            } else {
                ctx.put(ScratchKeys.RISK_BAND, "LOW");
            }
        }

        // R10 unverified account moving real money
        if ((mask & M10) != 0L && ctx.kycLevel() == 0 && ctx.amountCents() > 100_000L) {
            hits.add("R10");
            return new RiskResult(Decision.REJECT, hits, score);
        }

        // R11 burst of transactions
        if ((mask & M11) != 0L && ctx.txCount24h() > 20) {
            score += 12;
            hits.add("R11");
        }

        // R12 geo mismatch behind a vpn is much worse than either alone
        if ((mask & M12) != 0L && ctx.flag(ScratchKeys.GEO_MISMATCH) && ctx.vpn()) {
            score += 25;
            hits.add("R12");
        }

        // R13 device seen for the first time today
        if ((mask & M13) != 0L && ctx.deviceFirstSeenDays() == 0) {
            score += 22;
            hits.add("R13");
        }

        // R14 works off the normalised amount from R03
        if ((mask & M14) != 0L && ctx.longValue(ScratchKeys.ADJUSTED_AMOUNT, 0L) > 3_000_000L) {
            score += 20;
            hits.add("R14");
        }

        // R15 high risk merchant categories: atm, gambling, wire transfer
        if ((mask & M15) != 0L && (ctx.mcc() == 6011 || ctx.mcc() == 7995 || ctx.mcc() == 4829)) {
            score += 28;
            hits.add("R15");
        }

        // R16 small hours
        if ((mask & M16) != 0L && ctx.hourOfDay() < 5) {
            score += 8;
            hits.add("R16");
        }

        // R17 velocity model
        if ((mask & M17) != 0L && ctx.velocityScore() > 80) {
            score += 24;
            hits.add("R17");
        }

        // R18 reads the band written by R09
        if ((mask & M18) != 0L && "HIGH".equals(ctx.text(ScratchKeys.RISK_BAND)) && ctx.txCount24h() > 10) {
            hits.add("R18");
            return new RiskResult(Decision.REJECT, hits, score);
        }

        // R19 way above the customer's own baseline
        if ((mask & M19) != 0L && ctx.avgAmount30d() > 0L && ctx.amountCents() > ctx.avgAmount30d() * 5L) {
            score += 26;
            hits.add("R19");
        }

        // R20 long standing clean account
        if ((mask & M20) != 0L && ctx.accountAgeDays() > 365 && ctx.chargebackCount() == 0) {
            score -= 15;
            hits.add("R20");
        }

        // R21 a verified customer travelling is not the same as a mismatch on a fresh account
        if ((mask & M21) != 0L && ctx.flag(ScratchKeys.GEO_MISMATCH) && ctx.kycLevel() >= 2) {
            score -= 10;
            hits.add("R21");
        }

        // R22 tiny normalised amount
        if ((mask & M22) != 0L && ctx.longValue(ScratchKeys.ADJUSTED_AMOUNT, 0L) < 1_000L) {
            score -= 5;
            hits.add("R22");
        }

        // R23 fully verified
        if ((mask & M23) != 0L && ctx.kycLevel() >= 3) {
            score -= 12;
            hits.add("R23");
        }

        // R24 device on the allow list
        if ((mask & M24) != 0L && ctx.deviceId().startsWith("trusted-")) {
            score -= 20;
            hits.add("R24");
        }

        // R25 domestic usd corridor
        if ((mask & M25) != 0L && "USD".equals(ctx.currency()) && "US".equals(ctx.countryCode())) {
            score -= 5;
            hits.add("R25");
        }

        // R26 groceries
        if ((mask & M26) != 0L && ctx.mcc() == 5411) {
            score -= 8;
            hits.add("R26");
        }

        // R27 dormant account waking up
        if ((mask & M27) != 0L && ctx.txCount24h() == 0) {
            score += 6;
            hits.add("R27");
        }

        // R28 suspiciously round amount
        if ((mask & M28) != 0L && ctx.amountCents() >= 100_000L && ctx.amountCents() % 100_000L == 0L) {
            score += 9;
            hits.add("R28");
        }

        // R29 velocity model screaming
        if ((mask & M29) != 0L && ctx.velocityScore() > 95) {
            hits.add("R29");
            return new RiskResult(Decision.REJECT, hits, score);
        }

        // R30 accumulated score over the hard ceiling
        if ((mask & M30) != 0L && score >= 100) {
            hits.add("R30");
            return new RiskResult(Decision.REJECT, hits, score);
        }

        // R31 mid band plus a fresh device still deserves a look
        if ((mask & M31) != 0L && "MID".equals(ctx.text(ScratchKeys.RISK_BAND)) && ctx.deviceFirstSeenDays() < 3) {
            score += 7;
            hits.add("R31");
        }

        // R32 trusted device on a low band pulls the score down one last notch
        if ((mask & M32) != 0L && "LOW".equals(ctx.text(ScratchKeys.RISK_BAND))
                && ctx.deviceId().startsWith("trusted-")) {
            score -= 6;
            hits.add("R32");
        }

        Decision decision = score >= RiskEvaluator.REVIEW_THRESHOLD ? Decision.REVIEW : Decision.APPROVE;
        return new RiskResult(decision, hits, score);
    }
}
