package com.fta.risk;

import com.fta.risk.rules.DefaultRules;
import com.fta.risk.rules.DefaultPipeline;

import java.util.ArrayList;
import java.util.List;

/**
 * Transaction risk decision.
 *
 * The evaluator is an engine loop over the {@link RiskRule}s published by a
 * {@link RuleRegistry}: rules run in explicit priority order, a REJECT outcome stops the
 * loop immediately (later rules never run, so their ids never appear in the result), and
 * anything left over is bucketed into REVIEW or APPROVE by the accumulated score.
 *
 * When the registry holds exactly the default rule set, evaluation is delegated to
 * {@link DefaultPipeline}, a hand-inlined compilation of the same rules; behaviour is
 * identical, throughput is on par with the old hand-written if/else chain.
 */
public class RiskEvaluator {

    /** Score at or above which a transaction that survived all rules goes to REVIEW. */
    public static final int REVIEW_THRESHOLD = 55;

    private final RuleRegistry registry;
    private final DefaultPipeline pipeline;

    public RiskEvaluator() {
        this(new RuleRegistry(DefaultRules.all()));
    }

    public RiskEvaluator(RuleRegistry registry) {
        this(registry, true);
    }

    /** Test hook: usePipeline=false forces the generic loop even for the default rule set. */
    RiskEvaluator(RuleRegistry registry, boolean usePipeline) {
        this.registry = registry;
        this.pipeline = usePipeline ? DefaultPipeline.createIfMatches(registry) : null;
    }

    public RiskResult evaluate(RiskContext ctx) {
        ctx.clearScratch();
        if (pipeline != null) {
            // One volatile read: this evaluation runs to completion with exactly this rule
            // set, even if another thread toggles rules while we are evaluating.
            return pipeline.evaluate(ctx, registry.enabledMask());
        }
        // Generic path: same single volatile read, same snapshot semantics.
        List<RiskRule> rules = registry.snapshot();
        RuleEvaluation eval = new RuleEvaluation();
        List<String> hits = new ArrayList<>();

        for (RiskRule rule : rules) {
            RuleOutcome outcome = rule.apply(ctx, eval);
            if (outcome != RuleOutcome.PASS) {
                hits.add(rule.id());
            }
            if (outcome == RuleOutcome.REJECT) {
                return new RiskResult(Decision.REJECT, hits, eval.score());
            }
        }

        Decision decision = eval.score() >= REVIEW_THRESHOLD ? Decision.REVIEW : Decision.APPROVE;
        return new RiskResult(decision, hits, eval.score());
    }

    /**
     * Runtime switch for a single rule. Applies to evaluations started after this call;
     * in-flight evaluations finish with the rule set they started with.
     */
    public void setRuleEnabled(String ruleId, boolean enabled) {
        registry.setEnabled(ruleId, enabled);
    }

    public boolean isRuleEnabled(String ruleId) {
        return registry.isEnabled(ruleId);
    }
}
