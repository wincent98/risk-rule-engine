package com.fta.risk;

import com.fta.risk.rules.DefaultRules;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The default evaluator runs a precompiled pipeline; custom registries run the generic
 * rule loop. Both engines must produce identical results for identical rule sets,
 * including under runtime toggles.
 */
class EngineEquivalenceTest {

    private RiskEvaluator compiled() {
        return new RiskEvaluator(new RuleRegistry(DefaultRules.all()), true);
    }

    private RiskEvaluator generic() {
        return new RiskEvaluator(new RuleRegistry(DefaultRules.all()), false);
    }

    private static void assertSameResult(RiskContext ctx, RiskEvaluator a, RiskEvaluator b, String what) {
        RiskResult ra = a.evaluate(ctx);
        RiskResult rb = b.evaluate(ctx);
        assertEquals(ra.decision(), rb.decision(), what + " decision");
        assertEquals(ra.hitRuleIds(), rb.hitRuleIds(), what + " hitRuleIds");
        assertEquals(ra.score(), rb.score(), what + " score");
    }

    @Test
    void bothEnginesAgreeOnEveryGoldenSample() throws Exception {
        RiskEvaluator fast = compiled();
        RiskEvaluator slow = generic();
        List<String> lines = readGoldenSamples();
        for (int i = 0; i < lines.size(); i++) {
            RiskContext ctx = SampleFactory.decode(lines.get(i).split("\t", -1));
            assertSameResult(ctx, fast, slow, "golden #" + i);
        }
    }

    @Test
    void bothEnginesAgreeUnderRandomToggles() {
        Random random = new Random(20260920L);
        for (int round = 0; round < 40; round++) {
            RiskEvaluator fast = compiled();
            RiskEvaluator slow = generic();
            for (int i = 1; i <= 32; i++) {
                if (random.nextInt(100) < 25) {
                    String id = String.format("R%02d", i);
                    try {
                        fast.setRuleEnabled(id, false);
                        slow.setRuleEnabled(id, false);
                    } catch (IllegalStateException expected) {
                        // disabling a provider while its consumers are enabled is refused
                    }
                }
            }
            for (int s = 0; s < 64; s++) {
                RiskContext ctx = SampleFactory.sample(round * 64 + s);
                assertSameResult(ctx, fast, slow, "round=" + round + " sample=" + s);
            }
        }
    }

    private static List<String> readGoldenSamples() throws Exception {
        InputStream in = EngineEquivalenceTest.class.getResourceAsStream("/golden/samples.tsv");
        assertNotNull(in);
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
        }
        return lines;
    }
}
