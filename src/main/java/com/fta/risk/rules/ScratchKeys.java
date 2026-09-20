package com.fta.risk.rules;

/**
 * Names of the scratch keys rules use to hand intermediate values to later rules.
 * Centralised so producers and consumers cannot drift apart on a typo.
 */
public final class ScratchKeys {

    /** Written by R03: amount normalised to CNY cents. Read by R14 and R22. */
    public static final String ADJUSTED_AMOUNT = "adjustedAmount";

    /** Written by R05: TRUE when ip country differs from the declared country. Read by R12 and R21. */
    public static final String GEO_MISMATCH = "geoMismatch";

    /** Written by R09: LOW / MID / HIGH band of the running score. Read by R18, R31 and R32. */
    public static final String RISK_BAND = "riskBand";

    private ScratchKeys() {
    }
}
