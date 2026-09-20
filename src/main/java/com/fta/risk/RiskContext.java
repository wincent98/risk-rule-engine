package com.fta.risk;

import java.util.HashMap;
import java.util.Map;

/** One transaction under evaluation, plus the scratch space rules use to pass values along. */
public final class RiskContext {

    private long amountCents;
    private String currency = "CNY";
    private String countryCode = "CN";
    private int accountAgeDays;
    private int txCount24h;
    private long avgAmount30d;
    private String deviceId = "";
    private int deviceFirstSeenDays;
    private String ipCountry = "CN";
    private String binCountry = "CN";
    private int mcc;
    private boolean vpn;
    private int chargebackCount;
    private int kycLevel;
    private int hourOfDay;
    private int velocityScore;

    private final Map<String, Object> scratch = new HashMap<>();

    public long amountCents() {
        return amountCents;
    }

    public RiskContext amountCents(long v) {
        this.amountCents = v;
        return this;
    }

    public String currency() {
        return currency;
    }

    public RiskContext currency(String v) {
        this.currency = v;
        return this;
    }

    public String countryCode() {
        return countryCode;
    }

    public RiskContext countryCode(String v) {
        this.countryCode = v;
        return this;
    }

    public int accountAgeDays() {
        return accountAgeDays;
    }

    public RiskContext accountAgeDays(int v) {
        this.accountAgeDays = v;
        return this;
    }

    public int txCount24h() {
        return txCount24h;
    }

    public RiskContext txCount24h(int v) {
        this.txCount24h = v;
        return this;
    }

    public long avgAmount30d() {
        return avgAmount30d;
    }

    public RiskContext avgAmount30d(long v) {
        this.avgAmount30d = v;
        return this;
    }

    public String deviceId() {
        return deviceId;
    }

    public RiskContext deviceId(String v) {
        this.deviceId = v;
        return this;
    }

    public int deviceFirstSeenDays() {
        return deviceFirstSeenDays;
    }

    public RiskContext deviceFirstSeenDays(int v) {
        this.deviceFirstSeenDays = v;
        return this;
    }

    public String ipCountry() {
        return ipCountry;
    }

    public RiskContext ipCountry(String v) {
        this.ipCountry = v;
        return this;
    }

    public String binCountry() {
        return binCountry;
    }

    public RiskContext binCountry(String v) {
        this.binCountry = v;
        return this;
    }

    public int mcc() {
        return mcc;
    }

    public RiskContext mcc(int v) {
        this.mcc = v;
        return this;
    }

    public boolean vpn() {
        return vpn;
    }

    public RiskContext vpn(boolean v) {
        this.vpn = v;
        return this;
    }

    public int chargebackCount() {
        return chargebackCount;
    }

    public RiskContext chargebackCount(int v) {
        this.chargebackCount = v;
        return this;
    }

    public int kycLevel() {
        return kycLevel;
    }

    public RiskContext kycLevel(int v) {
        this.kycLevel = v;
        return this;
    }

    public int hourOfDay() {
        return hourOfDay;
    }

    public RiskContext hourOfDay(int v) {
        this.hourOfDay = v;
        return this;
    }

    public int velocityScore() {
        return velocityScore;
    }

    public RiskContext velocityScore(int v) {
        this.velocityScore = v;
        return this;
    }

    /** Scratch space. Some rules write here and later rules read it back. */
    public void put(String key, Object value) {
        scratch.put(key, value);
    }

    public Object get(String key) {
        return scratch.get(key);
    }

    public boolean flag(String key) {
        return Boolean.TRUE.equals(scratch.get(key));
    }

    public long longValue(String key, long fallback) {
        Object v = scratch.get(key);
        return v instanceof Long ? (Long) v : fallback;
    }

    public String text(String key) {
        Object v = scratch.get(key);
        return v == null ? null : v.toString();
    }

    public void clearScratch() {
        scratch.clear();
    }
}
