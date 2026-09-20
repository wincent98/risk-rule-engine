package com.fta.risk;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Locks the current decision behaviour. Any refactor of RiskEvaluator has to keep
 * decision, hitRuleIds (including their order) and score identical on all 200 samples.
 */
class GoldenSampleTest {

    private static final String RESOURCE = "/golden/samples.tsv";

    @Test
    void everyGoldenSampleStillProducesTheSameResult() throws Exception {
        List<String> lines = readSamples();
        assertEquals(200, lines.size(), "golden sample set changed size");

        RiskEvaluator evaluator = new RiskEvaluator();
        List<String> mismatches = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String[] f = lines.get(i).split("\t", -1);
            RiskContext ctx = SampleFactory.decode(f);
            String wantDecision = f[16];
            String wantHits = f[17];
            int wantScore = Integer.parseInt(f[18]);

            RiskResult got = evaluator.evaluate(ctx);
            String gotHits = String.join(",", got.hitRuleIds());

            if (!wantDecision.equals(got.decision().name())) {
                mismatches.add("#" + i + " decision want=" + wantDecision + " got=" + got.decision());
            }
            if (!wantHits.equals(gotHits)) {
                mismatches.add("#" + i + " hitRuleIds want=[" + wantHits + "] got=[" + gotHits + "]");
            }
            if (wantScore != got.score()) {
                mismatches.add("#" + i + " score want=" + wantScore + " got=" + got.score());
            }
        }

        assertTrue(mismatches.isEmpty(),
                mismatches.size() + " golden mismatches, first 10:\n" + String.join("\n",
                        mismatches.subList(0, Math.min(10, mismatches.size()))));
    }

    @Test
    void goldenSetCoversEveryRule() throws Exception {
        StringBuilder all = new StringBuilder();
        for (String line : readSamples()) {
            all.append(line.split("\t", -1)[17]).append(',');
        }
        for (int i = 1; i <= 32; i++) {
            String id = String.format("R%02d", i);
            assertTrue(all.indexOf(id) >= 0, "golden set never hits " + id);
        }
    }

    private List<String> readSamples() throws Exception {
        InputStream in = GoldenSampleTest.class.getResourceAsStream(RESOURCE);
        assertNotNull(in, "missing " + RESOURCE);
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
