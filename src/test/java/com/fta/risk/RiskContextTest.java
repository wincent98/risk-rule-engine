package com.fta.risk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RiskContextTest {

    @Test
    void settersChain() {
        RiskContext c = new RiskContext().amountCents(100L).currency("USD").kycLevel(3);
        assertEquals(100L, c.amountCents());
        assertEquals("USD", c.currency());
        assertEquals(3, c.kycLevel());
    }

    @Test
    void defaultsAreDomestic() {
        RiskContext c = new RiskContext();
        assertEquals("CNY", c.currency());
        assertEquals("CN", c.countryCode());
        assertEquals("CN", c.ipCountry());
    }

    @Test
    void scratchStoresAndReadsBack() {
        RiskContext c = new RiskContext();
        c.put("k", "v");
        assertEquals("v", c.text("k"));
        assertNull(c.text("missing"));
    }

    @Test
    void flagOnlyTrueForBooleanTrue() {
        RiskContext c = new RiskContext();
        assertFalse(c.flag("k"));
        c.put("k", Boolean.TRUE);
        assertTrue(c.flag("k"));
        c.put("k", "true");
        assertFalse(c.flag("k"));
    }

    @Test
    void longValueFallsBackWhenAbsentOrWrongType() {
        RiskContext c = new RiskContext();
        assertEquals(7L, c.longValue("k", 7L));
        c.put("k", "not a long");
        assertEquals(7L, c.longValue("k", 7L));
        c.put("k", 9L);
        assertEquals(9L, c.longValue("k", 7L));
    }

    @Test
    void clearScratchWipesEverything() {
        RiskContext c = new RiskContext();
        c.put("k", 1L);
        c.clearScratch();
        assertNull(c.get("k"));
    }
}
