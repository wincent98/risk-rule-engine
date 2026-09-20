package com.fta.risk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SampleFactoryTest {

    @Test
    void sampleIsDeterministicForAnIndex() {
        assertEquals(SampleFactory.encode(SampleFactory.sample(17)),
                SampleFactory.encode(SampleFactory.sample(17)));
    }

    @Test
    void differentIndexesGiveDifferentSamples() {
        assertNotEquals(SampleFactory.encode(SampleFactory.sample(1)),
                SampleFactory.encode(SampleFactory.sample(2)));
    }

    @Test
    void encodeDecodeRoundTrips() {
        RiskContext original = SampleFactory.sample(42);
        String encoded = SampleFactory.encode(original);
        assertEquals(encoded, SampleFactory.encode(SampleFactory.decode(encoded.split("\t", -1))));
    }

    @Test
    void encodedRowHasSixteenColumns() {
        assertEquals(16, SampleFactory.encode(SampleFactory.sample(3)).split("\t", -1).length);
    }

    @Test
    void generatedValuesStayInRange() {
        for (int i = 0; i < 50; i++) {
            RiskContext c = SampleFactory.sample(i);
            assertTrue(c.hourOfDay() >= 0 && c.hourOfDay() < 24);
            assertTrue(c.kycLevel() >= 0 && c.kycLevel() <= 3);
            assertTrue(c.velocityScore() >= 0 && c.velocityScore() <= 100);
            assertTrue(c.amountCents() > 0);
        }
    }
}
