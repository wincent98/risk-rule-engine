package com.fta.risk;

/** Throughput benchmark. Run it before and after a refactor and compare ops/sec. */
public final class RiskBenchmark {

    private static final int WARMUP = 200_000;
    private static final int ROUNDS = 5;
    private static final int PER_ROUND = 400_000;

    public static void main(String[] args) {
        RiskEvaluator evaluator = new RiskEvaluator();

        RiskContext[] pool = new RiskContext[512];
        for (int i = 0; i < pool.length; i++) {
            pool[i] = SampleFactory.sample(i);
        }

        long sink = 0;
        for (int i = 0; i < WARMUP; i++) {
            sink += evaluator.evaluate(pool[i & 511]).score();
        }

        System.out.println("risk-rule-engine 3.2.0 benchmark");
        System.out.println("warmup=" + WARMUP + " rounds=" + ROUNDS + " perRound=" + PER_ROUND);

        double best = 0;
        for (int round = 0; round < ROUNDS; round++) {
            long start = System.nanoTime();
            for (int i = 0; i < PER_ROUND; i++) {
                sink += evaluator.evaluate(pool[i & 511]).score();
            }
            long elapsed = System.nanoTime() - start;
            double ops = PER_ROUND / (elapsed / 1_000_000_000.0);
            best = Math.max(best, ops);
            System.out.printf("  round %d: %,.0f ops/sec%n", round + 1, ops);
        }

        System.out.println();
        System.out.printf("best throughput : %,.0f ops/sec%n", best);
        System.out.println("checksum        : " + sink);
    }

    private RiskBenchmark() {
    }
}
