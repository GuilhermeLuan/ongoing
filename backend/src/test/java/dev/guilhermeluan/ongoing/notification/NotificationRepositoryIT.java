package dev.guilhermeluan.ongoing.notification;

import dev.guilhermeluan.ongoing.auth.AuthService;
import dev.guilhermeluan.ongoing.auth.dto.RegisterRequest;
import dev.guilhermeluan.ongoing.config.BaseIntegrationTest;
import dev.guilhermeluan.ongoing.subscriptions.SubscriptionsRepository;
import dev.guilhermeluan.ongoing.subscriptions.SubscriptionsService;
import dev.guilhermeluan.ongoing.subscriptions.entities.*;
import dev.guilhermeluan.ongoing.user.RefreshTokenRepository;
import dev.guilhermeluan.ongoing.user.User;
import dev.guilhermeluan.ongoing.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationRepositoryIT extends BaseIntegrationTest {

    @Autowired
    private SubscriptionsRepository subscriptionsRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private AuthService authService;
    @Autowired
    private SubscriptionsService subscriptionsService;

    private User authenticatedUser;

    @BeforeEach
    void setUp() {
        subscriptionsRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        authService.register(new RegisterRequest("Test User", "test@example.com", "password123"));
        this.authenticatedUser = userRepository.findByEmail("test@example.com").orElseThrow();
    }

    // ─── Happy Path ───────────────────────────────────────────────────────────

    @Test
    void shouldReturnSubscriptionsDueOnTargetDate() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        createSubscription(tomorrow, true, true);

        List<Subscriptions> result = subscriptionsRepository.findByNextPaymentDateAndActiveAndNotify(tomorrow);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getNextPaymentDate()).isEqualTo(tomorrow);
    }

    @Test
    void shouldFetchUserCategoryAndPaymentMethod() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        subscriptionsRepository.save(
                Subscriptions.builder()
                        .name("Test Sub")
                        .value(new BigDecimal("49.90"))
                        .billingCycle(BillingCycle.MONTHLY)
                        .currency(Currency.BRL)
                        .startDate(LocalDate.now())
                        .nextPaymentDate(tomorrow)
                        .active(true)
                        .notify(true)
                        .user(authenticatedUser)
                        .category(Category.builder().id(1L).build())
                        .paymentMethod(PaymentMethod.builder().id(1L).build())
                        .build()
        );

        List<Subscriptions> result = subscriptionsRepository.findByNextPaymentDateAndActiveAndNotify(tomorrow);

        assertThat(result).hasSize(1);
        Subscriptions sub = result.getFirst();
        assertThat(sub.getUser()).isNotNull();
        assertThat(sub.getCategory()).isNotNull();
        assertThat(sub.getPaymentMethod()).isNotNull();
    }

    @Test
    void shouldGroupMultipleSubscriptionsForSameUser() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        createSubscription(tomorrow, true, true);
        createSubscription(tomorrow, true, true);
        createSubscription(tomorrow, true, true);

        Map<User, List<Subscriptions>> grouped = subscriptionsService.findRenewalSubscriptionsGroupedByUser(tomorrow);

        assertThat(grouped).hasSize(1);
        assertThat(grouped.get(authenticatedUser)).hasSize(3);
    }

    @Test
    void shouldHandleMultipleUsersIndependently() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        authService.register(new RegisterRequest("Second User", "second@example.com", "password123"));
        User secondUser = userRepository.findByEmail("second@example.com").orElseThrow();

        createSubscription(tomorrow, true, true);
        subscriptionsRepository.save(
                Subscriptions.builder()
                        .name("Test Sub")
                        .value(new BigDecimal("49.90"))
                        .billingCycle(BillingCycle.MONTHLY)
                        .currency(Currency.BRL)
                        .startDate(LocalDate.now())
                        .nextPaymentDate(tomorrow)
                        .active(true)
                        .notify(true)
                        .user(secondUser)
                        .build()
        );

        Map<User, List<Subscriptions>> grouped = subscriptionsService.findRenewalSubscriptionsGroupedByUser(tomorrow);

        assertThat(grouped).hasSize(2);
        assertThat(grouped.get(authenticatedUser)).hasSize(1);
        assertThat(grouped.get(secondUser)).hasSize(1);
    }

    // ─── Edge Cases — Filters ─────────────────────────────────────────────────

    @Test
    void shouldExcludeSubscriptionsWithNotifyFalse() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        createSubscription(tomorrow, true, true);
        createSubscription(tomorrow, true, false);

        List<Subscriptions> result = subscriptionsRepository.findByNextPaymentDateAndActiveAndNotify(tomorrow);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getNotify()).isTrue();
    }

    @Test
    void shouldExcludeInactiveSubscriptions() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        createSubscription(tomorrow, true, true);
        createSubscription(tomorrow, false, true);

        List<Subscriptions> result = subscriptionsRepository.findByNextPaymentDateAndActiveAndNotify(tomorrow);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getActive()).isTrue();
    }

    @Test
    void shouldReturnEmptyWhenDateDoesNotMatch() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate dayAfterTomorrow = LocalDate.now().plusDays(2);
        createSubscription(tomorrow, true, true);

        List<Subscriptions> result = subscriptionsRepository.findByNextPaymentDateAndActiveAndNotify(dayAfterTomorrow);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenAllSubscriptionsAreInactiveOrNotifyOff() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        createSubscription(tomorrow, false, false);
        createSubscription(tomorrow, false, true);
        createSubscription(tomorrow, true, false);

        List<Subscriptions> result = subscriptionsRepository.findByNextPaymentDateAndActiveAndNotify(tomorrow);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldHandleSubscriptionWithNullCategoryAndPaymentMethod() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        createSubscription(tomorrow, true, true);

        List<Subscriptions> result = subscriptionsRepository.findByNextPaymentDateAndActiveAndNotify(tomorrow);

        assertThat(result).hasSize(1);
        Subscriptions sub = result.getFirst();
        assertThat(sub.getUser()).isNotNull();
        assertThat(sub.getCategory()).isNull();
        assertThat(sub.getPaymentMethod()).isNull();
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private Subscriptions createSubscription(LocalDate nextPaymentDate, boolean active, boolean notify) {
        return subscriptionsRepository.save(
                Subscriptions.builder()
                        .name("Test Sub")
                        .value(new BigDecimal("49.90"))
                        .billingCycle(BillingCycle.MONTHLY)
                        .currency(Currency.BRL)
                        .startDate(LocalDate.now())
                        .nextPaymentDate(nextPaymentDate)
                        .active(active)
                        .notify(notify)
                        .user(authenticatedUser)
                        .build()
        );
    }
}
