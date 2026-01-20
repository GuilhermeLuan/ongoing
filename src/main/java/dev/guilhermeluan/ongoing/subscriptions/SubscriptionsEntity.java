package dev.guilhermeluan.ongoing.subscriptions;

import jakarta.persistence.*;
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
@Entity
@Table(name = "tb_subscriptions")
public class SubscriptionsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "next_payment_date", nullable = false)
    private LocalDate nextPaymentDate;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private Boolean notify = true;

    @Column(nullable = false, length = 3)
    private String currency = "BRL";

    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categories")
    private CategoryEntity category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_payment_method")
    private PaymentMethodEntity paymentMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_billing_cycle")
    private BillingCycleEntity billingCycle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_subscription_type")
    private SubscriptionTypeEntity subscriptionType;
}
