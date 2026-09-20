package com.fta.risk;

/**
 * What a rule decided about the current transaction.
 *
 * PASS   - the rule did not match; evaluation continues.
 * HIT    - the rule matched; its id is recorded and evaluation continues.
 * REJECT - terminal; the engine records the hit and stops evaluating further rules.
 */
public enum RuleOutcome {
    PASS,
    HIT,
    REJECT
}
