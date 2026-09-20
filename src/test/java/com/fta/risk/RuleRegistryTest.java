package com.fta.risk;

import com.fta.risk.rules.DefaultRules;
import com.fta.risk.rules.R01LargeTicket;
import com.fta.risk.rules.R02HardCeiling;
import com.fta.risk.rules.R14LargeAdjustedAmount;
import com.fta.risk.rules.R22TinyAmount;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleRegistryTest {

    @Test
    void defaultSetHasAll32RulesInPriorityOrder() {
        RuleRegistry registry = new RuleRegistry(DefaultRules.all());
        List<RiskRule> rules = registry.snapshot();
        assertEquals(32, rules.size());
        for (int i = 1; i < rules.size(); i++) {
            assertTrue(rules.get(i - 1).priority() < rules.get(i).priority(),
                    "rules must be sorted by priority");
        }
        assertEquals("R01", rules.get(0).id());
        assertEquals("R32", rules.get(31).id());
    }

    @Test
    void evaluationOrderComesFromPriorityNotRegistrationOrder() {
        RuleRegistry registry = new RuleRegistry(Arrays.asList(new R02HardCeiling(), new R01LargeTicket()));
        List<String> ids = registry.snapshot().stream().map(RiskRule::id).collect(Collectors.toList());
        assertEquals(Arrays.asList("R01", "R02"), ids);
    }

    @Test
    void duplicateRuleIdsAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new RuleRegistry(Arrays.asList(new R01LargeTicket(), new R01LargeTicket())));
    }

    @Test
    void aConsumerWithoutItsProviderFailsFast() {
        // R14 reads "adjustedAmount" but R03 (its provider) is missing
        assertThrows(IllegalStateException.class,
                () -> new RuleRegistry(Collections.singletonList(new R14LargeAdjustedAmount())));
    }

    @Test
    void disablingAProviderIsRefusedWhileConsumersStayEnabled() {
        RuleRegistry registry = new RuleRegistry(DefaultRules.all());
        assertThrows(IllegalStateException.class, () -> registry.setEnabled("R03", false));
        assertTrue(registry.isEnabled("R03"), "failed toggle must roll back");
        assertEquals(32, registry.snapshot().size());
    }

    @Test
    void disablingProviderAndConsumersTogetherWorks() {
        RuleRegistry registry = new RuleRegistry(DefaultRules.all());
        registry.setEnabled("R14", false);
        registry.setEnabled("R22", false);
        registry.setEnabled("R03", false);
        assertFalse(registry.isEnabled("R03"));
        assertEquals(29, registry.snapshot().size());
    }

    @Test
    void unknownRuleIdIsRejected() {
        RuleRegistry registry = new RuleRegistry(DefaultRules.all());
        assertThrows(IllegalArgumentException.class, () -> registry.setEnabled("R99", false));
        assertThrows(IllegalArgumentException.class, () -> registry.isEnabled("R99"));
    }

    @Test
    void aSnapshotInHandIsNotAffectedByLaterToggles() {
        RuleRegistry registry = new RuleRegistry(DefaultRules.all());
        List<RiskRule> inFlight = registry.snapshot();
        registry.setEnabled("R01", false);
        assertEquals(32, inFlight.size(), "in-flight evaluation keeps its original rule set");
        assertEquals(31, registry.snapshot().size(), "new evaluations see the toggle");
        assertThrows(UnsupportedOperationException.class, () -> inFlight.add(new R01LargeTicket()));
    }

    @Test
    void reenablingRestoresTheRule() {
        RuleRegistry registry = new RuleRegistry(DefaultRules.all());
        registry.setEnabled("R07", false);
        assertFalse(registry.isEnabled("R07"));
        registry.setEnabled("R07", true);
        assertTrue(registry.isEnabled("R07"));
        assertEquals(32, registry.snapshot().size());
    }
}
