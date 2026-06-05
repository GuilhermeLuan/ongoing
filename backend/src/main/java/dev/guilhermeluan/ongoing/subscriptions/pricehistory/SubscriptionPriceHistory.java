package dev.guilhermeluan.ongoing.subscriptions.pricehistory;

import dev.guilhermeluan.ongoing.subscriptions.entities.Subscriptions;
import dev.guilhermeluan.ongoing.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_subscription_price_history")
public class SubscriptionPriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscriptions subscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "old_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal oldValue;

    @Column(name = "new_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal newValue;

    @Column(name = "change_percentage", nullable = false, precision = 8, scale = 2)
    private BigDecimal changePercentage;

    @Column(name = "is_price_spike", nullable = false)
    private Boolean isPriceSpike;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @PrePersist
    protected void onCreate() {
        if (changedAt == null) {
            changedAt = LocalDateTime.now();
        }
    }
}
