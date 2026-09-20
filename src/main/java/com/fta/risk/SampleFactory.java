package com.fta.risk;

import java.util.Random;

/** Deterministic transaction generator shared by the golden sample set and the benchmark. */
public final class SampleFactory {

    private static final String[] CURRENCIES = {"CNY", "USD", "EUR", "JPY"};
    private static final String[] COUNTRIES = {"CN", "US", "DE", "JP", "SG"};
    private static final int[] MCCS = {5411, 5812, 6011, 7995, 4829, 5999, 7011};

    private SampleFactory() {
    }

    public static RiskContext sample(int index) {
        Random r = new Random(index * 2654435761L);
        RiskContext ctx = new RiskContext();
        ctx.amountCents(pickAmount(r));
        ctx.currency(CURRENCIES[r.nextInt(CURRENCIES.length)]);
        ctx.countryCode(COUNTRIES[r.nextInt(COUNTRIES.length)]);
        ctx.accountAgeDays(r.nextInt(900));
        ctx.txCount24h(r.nextInt(30));
        ctx.avgAmount30d(r.nextInt(400) * 1000L);
        ctx.deviceId((r.nextInt(4) == 0 ? "trusted-" : "dev-") + r.nextInt(1000));
        ctx.deviceFirstSeenDays(r.nextInt(6));
        ctx.ipCountry(COUNTRIES[r.nextInt(COUNTRIES.length)]);
        ctx.binCountry(COUNTRIES[r.nextInt(COUNTRIES.length)]);
        ctx.mcc(MCCS[r.nextInt(MCCS.length)]);
        ctx.vpn(r.nextInt(4) == 0);
        ctx.chargebackCount(pickChargebacks(r));
        ctx.kycLevel(r.nextInt(4));
        ctx.hourOfDay(r.nextInt(24));
        ctx.velocityScore(r.nextInt(101));
        return ctx;
    }

    private static int pickChargebacks(Random r) {
        int bucket = r.nextInt(20);
        if (bucket < 13) {
            return 0;
        }
        if (bucket < 17) {
            return 1;
        }
        if (bucket < 19) {
            return 2;
        }
        return 3 + r.nextInt(2);
    }

    private static long pickAmount(Random r) {
        int bucket = r.nextInt(10);
        if (bucket == 0) {
            return r.nextInt(900) + 100L;
        }
        if (bucket == 9 && r.nextInt(3) == 0) {
            return 2_100_000L + r.nextInt(500_000);
        }
        if (bucket % 3 == 0) {
            return (r.nextInt(20) + 1) * 100_000L;
        }
        return r.nextInt(1_500_000) + 1_000L;
    }

    public static String encode(RiskContext c) {
        return c.amountCents() + "\t" + c.currency() + "\t" + c.countryCode() + "\t" + c.accountAgeDays()
                + "\t" + c.txCount24h() + "\t" + c.avgAmount30d() + "\t" + c.deviceId()
                + "\t" + c.deviceFirstSeenDays() + "\t" + c.ipCountry() + "\t" + c.binCountry()
                + "\t" + c.mcc() + "\t" + c.vpn() + "\t" + c.chargebackCount() + "\t" + c.kycLevel()
                + "\t" + c.hourOfDay() + "\t" + c.velocityScore();
    }

    public static RiskContext decode(String[] f) {
        return new RiskContext()
                .amountCents(Long.parseLong(f[0]))
                .currency(f[1])
                .countryCode(f[2])
                .accountAgeDays(Integer.parseInt(f[3]))
                .txCount24h(Integer.parseInt(f[4]))
                .avgAmount30d(Long.parseLong(f[5]))
                .deviceId(f[6])
                .deviceFirstSeenDays(Integer.parseInt(f[7]))
                .ipCountry(f[8])
                .binCountry(f[9])
                .mcc(Integer.parseInt(f[10]))
                .vpn(Boolean.parseBoolean(f[11]))
                .chargebackCount(Integer.parseInt(f[12]))
                .kycLevel(Integer.parseInt(f[13]))
                .hourOfDay(Integer.parseInt(f[14]))
                .velocityScore(Integer.parseInt(f[15]));
    }
}
