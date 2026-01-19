package dev.guilhermeluan.ongoing.subscription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private String billingCycle;
    private LocalDate nextBillingDate;
    private Boolean isActive;
}
