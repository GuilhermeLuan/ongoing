package dev.guilhermeluan.ongoing.subscriptions.entities;

/**
 * Enum representing subscription billing cycles.
 * Defines the frequency at which a subscription is billed.
 */
public enum BillingCycle {
    /**
     * Monthly billing cycle (every 30 days)
     */
    MONTHLY("Monthly"),

    /**
     * Quarterly billing cycle (every 3 months)
     */
    QUARTERLY("Quarterly"),

    /**
     * Semi-annual billing cycle (every 6 months)
     */
    SEMI_ANNUAL("Semi-Annual"),

    /**
     * Annual/Yearly billing cycle (every 12 months)
     */
    YEARLY("Annual"),

    /**
     * Weekly billing cycle (every 7 days)
     */
    WEEKLY("Weekly"),

    /**
     * Bi-weekly billing cycle (every 14 days)
     */
    BIWEEKLY("Bi-weekly");

    private final String displayName;

    BillingCycle(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Converts legacy database name to enum value.
     *
     * @param name Legacy name from tb_billing_cycles table
     * @return Corresponding BillingCycle enum value
     * @throws IllegalArgumentException if name doesn't match any cycle
     */
    public static BillingCycle fromDisplayName(String name) {
        for (BillingCycle cycle : values()) {
            if (cycle.displayName.equalsIgnoreCase(name)) {
                return cycle;
            }
        }
        throw new IllegalArgumentException("Unknown billing cycle: " + name);
    }

    /**
     * Returns the human-readable display name.
     * Used for mapping from legacy database values.
     */
    public String getDisplayName() {
        return displayName;
    }
}
