package dev.guilhermeluan.ongoing.dashboard;

import dev.guilhermeluan.ongoing.subscriptions.entities.BillingCycle;
import dev.guilhermeluan.ongoing.subscriptions.entities.Subscriptions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.Locale;

/**
 * Subscription projection where the monetary value is already converted to BRL.
 */
public record ConvertedSubscription(
        Subscriptions subscription,
        BigDecimal priceInBrl
) {

    private static BigDecimal scaled(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static String normalizedBillingCycleName(BillingCycle billingCycle) {
        if (billingCycle == null || billingCycle.getDisplayName() == null) {
            throw new IllegalStateException("Billing cycle must be present on subscription");
        }
        return billingCycle.getDisplayName().trim().toLowerCase(Locale.ROOT);
    }

    public BigDecimal monthlyPrice() {
        return switch (normalizedBillingCycleName(subscription.getBillingCycle())) {
            case "monthly" -> scaled(priceInBrl);
            case "quarterly" -> scaled(priceInBrl.divide(BigDecimal.valueOf(3), 8, RoundingMode.HALF_UP));
            case "semi-annual" -> scaled(priceInBrl.divide(BigDecimal.valueOf(6), 8, RoundingMode.HALF_UP));
            case "annual" -> scaled(priceInBrl.divide(BigDecimal.valueOf(12), 8, RoundingMode.HALF_UP));
            case "weekly" ->
                    scaled(priceInBrl.multiply(BigDecimal.valueOf(52)).divide(BigDecimal.valueOf(12), 8, RoundingMode.HALF_UP));
            case "bi-weekly" ->
                    scaled(priceInBrl.multiply(BigDecimal.valueOf(26)).divide(BigDecimal.valueOf(12), 8, RoundingMode.HALF_UP));
            default -> throw new IllegalStateException("Unsupported billing cycle: " + subscription.getBillingCycle());
        };
    }

    public BigDecimal yearlyPrice() {
        return switch (normalizedBillingCycleName(subscription.getBillingCycle())) {
            case "monthly" -> scaled(priceInBrl.multiply(BigDecimal.valueOf(12)));
            case "quarterly" -> scaled(priceInBrl.multiply(BigDecimal.valueOf(4)));
            case "semi-annual" -> scaled(priceInBrl.multiply(BigDecimal.valueOf(2)));
            case "annual" -> scaled(priceInBrl);
            case "weekly" -> scaled(priceInBrl.multiply(BigDecimal.valueOf(52)));
            case "bi-weekly" -> scaled(priceInBrl.multiply(BigDecimal.valueOf(26)));
            default -> throw new IllegalStateException("Unsupported billing cycle: " + subscription.getBillingCycle());
        };
    }

    public boolean isDueIn(YearMonth month) {
        if (subscription.getNextPaymentDate() == null) {
            return false;
        }
        return YearMonth.from(subscription.getNextPaymentDate()).equals(month);
    }
}
