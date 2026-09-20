package com.fta.risk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Holds the known rules and the set of currently enabled ones.
 *
 * Rules are ordered by their explicit {@link RiskRule#priority()}, never by registration
 * order. Scratch dependencies declared via {@link RiskRule#provides()} and
 * {@link RiskRule#requires()} are validated whenever the enabled set changes: a rule whose
 * requirements cannot be met by an earlier enabled rule makes the change fail fast.
 *
 * The enabled view is published as an immutable snapshot behind a volatile reference, so a
 * reader that grabbed the snapshot keeps evaluating with that exact rule set even if another
 * thread enables or disables rules mid-evaluation.
 *
 * At most 64 rules are supported, so the enabled set also fits in a single volatile long
 * (bit i corresponds to the rule at sorted position i) for callers on the hot path.
 */
public final class RuleRegistry {

    private final List<RiskRule> ordered;
    private final Map<String, RiskRule> byId;
    private final Set<String> disabled = new LinkedHashSet<>();

    private volatile List<RiskRule> snapshot;
    private volatile long enabledMask;

    public RuleRegistry(Collection<? extends RiskRule> rules) {
        List<RiskRule> sorted = new ArrayList<>(rules);
        sorted.sort(Comparator.comparingInt(RiskRule::priority));
        if (sorted.size() > 64) {
            throw new IllegalArgumentException("at most 64 rules are supported, got " + sorted.size());
        }

        Map<String, RiskRule> ids = new HashMap<>();
        Set<Integer> priorities = new HashSet<>();
        for (RiskRule rule : sorted) {
            if (rule.id() == null || rule.id().isEmpty()) {
                throw new IllegalArgumentException("rule without an id: " + rule.getClass().getName());
            }
            if (ids.put(rule.id(), rule) != null) {
                throw new IllegalArgumentException("duplicate rule id: " + rule.id());
            }
            if (!priorities.add(rule.priority())) {
                throw new IllegalArgumentException("duplicate priority " + rule.priority()
                        + " on rule " + rule.id());
            }
        }

        this.ordered = Collections.unmodifiableList(sorted);
        this.byId = Collections.unmodifiableMap(ids);
        validateDependencies(sorted);
        this.snapshot = this.ordered;
        this.enabledMask = sorted.size() == 64 ? -1L : (1L << sorted.size()) - 1L;
    }

    /** The currently enabled rules, in evaluation order. Immutable; a single volatile read. */
    public List<RiskRule> snapshot() {
        return snapshot;
    }

    /**
     * Bit i is set when the rule at sorted position i is currently enabled.
     * A single volatile read; pair it with {@link #all()} (position i) to interpret.
     */
    public long enabledMask() {
        return enabledMask;
    }

    /** All registered rules in evaluation order, including disabled ones. */
    public List<RiskRule> all() {
        return ordered;
    }

    /**
     * Enable or disable a single rule. Takes effect for evaluations that start after this
     * call returns; evaluations already in flight keep the snapshot they started with.
     *
     * @throws IllegalArgumentException if the rule id is unknown
     * @throws IllegalStateException    if the change would leave a rule without a provider
     *                                  for a scratch key it requires
     */
    public synchronized void setEnabled(String ruleId, boolean enabled) {
        if (!byId.containsKey(ruleId)) {
            throw new IllegalArgumentException("unknown rule: " + ruleId);
        }
        boolean changed = enabled ? disabled.remove(ruleId) : disabled.add(ruleId);
        if (!changed) {
            return;
        }
        List<RiskRule> active = new ArrayList<>(ordered.size() - disabled.size());
        for (RiskRule rule : ordered) {
            if (!disabled.contains(rule.id())) {
                active.add(rule);
            }
        }
        try {
            validateDependencies(active);
        } catch (IllegalStateException e) {
            // roll back: never publish a rule set whose dependencies do not hold
            if (enabled) {
                disabled.add(ruleId);
            } else {
                disabled.remove(ruleId);
            }
            throw e;
        }
        snapshot = Collections.unmodifiableList(active);
        long mask = 0L;
        for (int i = 0; i < ordered.size(); i++) {
            if (!disabled.contains(ordered.get(i).id())) {
                mask |= 1L << i;
            }
        }
        enabledMask = mask;
    }

    public synchronized boolean isEnabled(String ruleId) {
        if (!byId.containsKey(ruleId)) {
            throw new IllegalArgumentException("unknown rule: " + ruleId);
        }
        return !disabled.contains(ruleId);
    }

    private static void validateDependencies(List<RiskRule> active) {
        Set<String> available = new HashSet<>();
        for (RiskRule rule : active) {
            for (String key : rule.requires()) {
                if (!available.contains(key)) {
                    throw new IllegalStateException("rule " + rule.id() + " requires scratch key '"
                            + key + "' but no enabled rule with a lower priority provides it");
                }
            }
            available.addAll(rule.provides());
        }
    }
}
