package com.fta.risk;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RiskResultTest {

    @Test
    void exposesItsFields() {
        RiskResult r = new RiskResult(Decision.REVIEW, Arrays.asList("R01", "R02"), 42);
        assertEquals(Decision.REVIEW, r.decision());
        assertEquals(2, r.hitRuleIds().size());
        assertEquals(42, r.score());
    }

    @Test
    void hitRuleIdsAreImmutable() {
        RiskResult r = new RiskResult(Decision.APPROVE, new ArrayList<>(), 0);
        assertThrows(UnsupportedOperationException.class, () -> r.hitRuleIds().add("R99"));
    }

    @Test
    void keepsTheGivenOrder() {
        List<String> ids = Arrays.asList("R05", "R01", "R09");
        assertEquals(ids, new RiskResult(Decision.APPROVE, ids, 0).hitRuleIds());
    }

    @Test
    void printsACompactForm() {
        RiskResult r = new RiskResult(Decision.REJECT, Arrays.asList("R01", "R02"), -3);
        assertEquals("REJECT|R01,R02|-3", r.toString());
    }
}
