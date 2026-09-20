package com.fta.risk;

import java.util.ArrayList;
import java.util.List;

/**
 * Transaction risk decision.
 *
 * History: started as a handful of checks for the 2023 launch, every campaign since then
 * bolted another branch on. The ordering matters in two ways that are easy to miss:
 * a REJECT returns immediately so later rules never run, and a few rules stash values in
 * the context that later rules read back.
 *
 * Do not reorder the blocks unless you have re-run the golden samples.
 */
public class RiskEvaluator {

    private static final int REVIEW_THRESHOLD = 55;

    public RiskResult evaluate(RiskContext ctx) {
        ctx.clearScratch();
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
            ctx.put("adjustedAmount", adjusted);
            score += 5;
            hits.add("R03");
        } else {
            ctx.put("adjustedAmount", ctx.amountCents());
        }

        // R04 brand new account
        if (ctx.accountAgeDays() < 7) {
            score += 30;
            hits.add("R04");
        }

        // R05 ip does not match the declared country, later rules key off this
        if (!ctx.ipCountry().equals(ctx.countryCode())) {
            ctx.put("geoMismatch", Boolean.TRUE);
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

        // R09 bucket the score so far, read back by R18
        if (score >= 60) {
            ctx.put("riskBand", "HIGH");
            hits.add("R09");
        } else if (score >= 30) {
            ctx.put("riskBand", "MID");
            hits.add("R09");
        } else {
            ctx.put("riskBand", "LOW");
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
        if (ctx.flag("geoMismatch") && ctx.vpn()) {
            score += 25;
            hits.add("R12");
        }

        // R13 device seen for the first time today
        if (ctx.deviceFirstSeenDays() == 0) {
            score += 22;
            hits.add("R13");
        }

        // R14 works off the normalised amount from R03
        if (ctx.longValue("adjustedAmount", 0L) > 3_000_000L) {
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
        if ("HIGH".equals(ctx.text("riskBand")) && ctx.txCount24h() > 10) {
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
        if (ctx.flag("geoMismatch") && ctx.kycLevel() >= 2) {
            score -= 10;
            hits.add("R21");
        }

        // R22 tiny normalised amount
        if (ctx.longValue("adjustedAmount", 0L) < 1_000L) {
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
        if ("MID".equals(ctx.text("riskBand")) && ctx.deviceFirstSeenDays() < 3) {
            score += 7;
            hits.add("R31");
        }

        // R32 trusted device on a low band pulls the score down one last notch
        if ("LOW".equals(ctx.text("riskBand")) && ctx.deviceId().startsWith("trusted-")) {
            score -= 6;
            hits.add("R32");
        }

        Decision decision = score >= REVIEW_THRESHOLD ? Decision.REVIEW : Decision.APPROVE;
        return new RiskResult(decision, hits, score);
    }
}
