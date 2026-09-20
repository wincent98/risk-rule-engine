package com.fta.risk.rules;

import com.fta.risk.RiskRule;

import java.util.Arrays;
import java.util.List;

/**
 * The built-in rule set. Assembly is plain code: no reflection, no annotation scanning.
 * The registry sorts by each rule's explicit priority, so the order of this list does not
 * affect evaluation order.
 */
public final class DefaultRules {

    public static List<RiskRule> all() {
        return Arrays.asList(
                new R01LargeTicket(),
                new R02HardCeiling(),
                new R03NormaliseCurrency(),
                new R04NewAccount(),
                new R05GeoMismatch(),
                new R06BinCountryMismatch(),
                new R07Vpn(),
                new R08ChargebackStop(),
                new R09RiskBand(),
                new R10UnverifiedMoney(),
                new R11BurstActivity(),
                new R12GeoMismatchVpn(),
                new R13FreshDevice(),
                new R14LargeAdjustedAmount(),
                new R15HighRiskMcc(),
                new R16SmallHours(),
                new R17VelocityHigh(),
                new R18HighBandBurst(),
                new R19AboveBaseline(),
                new R20CleanVeteran(),
                new R21VerifiedTraveller(),
                new R22TinyAmount(),
                new R23FullyVerified(),
                new R24TrustedDevice(),
                new R25DomesticUsd(),
                new R26Groceries(),
                new R27DormantWake(),
                new R28RoundAmount(),
                new R29VelocityScreaming(),
                new R30ScoreCeiling(),
                new R31MidBandFreshDevice(),
                new R32TrustedLowBand());
    }

    private DefaultRules() {
    }
}
