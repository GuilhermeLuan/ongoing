package dev.guilhermeluan.ongoing.subscriptions;

import dev.guilhermeluan.ongoing.exception.BadRequestException;
import dev.guilhermeluan.ongoing.subscriptions.entities.BillingCycle;
import dev.guilhermeluan.ongoing.subscriptions.entities.Subscriptions;
import dev.guilhermeluan.ongoing.subscriptions.pricehistory.SubscriptionPriceHistory;
import dev.guilhermeluan.ongoing.subscriptions.pricehistory.SubscriptionPriceHistoryRepository;
import dev.guilhermeluan.ongoing.user.User;
import dev.guilhermeluan.ongoing.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionsServiceTest {
    @InjectMocks
    private SubscriptionsService service;

    @Mock
    private SubscriptionsRepository subscriptionsRepository;

    @Mock
    private SubscriptionPriceHistoryRepository subscriptionPriceHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionsMapper subscriptionsMapper;


    @Test
    void calculateNextBillingDate_ShouldResultPlusOneMonth_WhenBillingCycleIsMonthly() {

        var startDate = LocalDate.of(2023, 1, 1);
        var expectedDate = LocalDate.of(2023, 2, 1);

        Subscriptions subscription = Subscriptions.builder()
                .startDate(startDate)
                .billingCycle(BillingCycle.MONTHLY)
                .build();

        var result = service.calculateNextBillingDate(subscription);

        assertEquals(expectedDate, result);
    }

    @Test
    void calculateNextBillingDate_ShouldResultPlusOneYear_WhenBillingCycleIsAnnually() {

        var startDate = LocalDate.of(2023, 1, 1);
        var expectedDate = LocalDate.of(2024, 1, 1);

        Subscriptions subscription = Subscriptions.builder()
                .startDate(startDate)
                .billingCycle(BillingCycle.YEARLY)
                .build();

        var result = service.calculateNextBillingDate(subscription);

        assertEquals(expectedDate, result);
    }

    @Test
    void calculateNextBillingDate_ShouldThrowBadRequestException_WhenBillingCycleIsNull() {
        var startDate = LocalDate.of(2023, 1, 1);

        Subscriptions subscription = Subscriptions.builder()
                .startDate(startDate)
                .billingCycle(null)
                .build();

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> service.calculateNextBillingDate(subscription)
        );

        assertEquals("Billing cycle is required", exception.getReason());
    }

    @Test
    void calculateNextBillingDate_ShouldResultPlusThreeMonths_WhenBillingCycleIsQuarterly() {
        var startDate = LocalDate.of(2023, 1, 1);
        var expectedDate = LocalDate.of(2023, 4, 1);

        Subscriptions subscription = Subscriptions.builder()
                .startDate(startDate)
                .billingCycle(BillingCycle.QUARTERLY)
                .build();

        var result = service.calculateNextBillingDate(subscription);

        assertEquals(expectedDate, result);
    }

    @Test
    void calculateNextBillingDate_ShouldResultPlusSixMonths_WhenBillingCycleIsSemiAnnual() {
        var startDate = LocalDate.of(2023, 1, 1);
        var expectedDate = LocalDate.of(2023, 7, 1);

        Subscriptions subscription = Subscriptions.builder()
                .startDate(startDate)
                .billingCycle(BillingCycle.SEMI_ANNUAL)
                .build();

        var result = service.calculateNextBillingDate(subscription);

        assertEquals(expectedDate, result);
    }

    @Test
    void calculateNextBillingDate_ShouldResultPlusOneWeek_WhenBillingCycleIsWeekly() {
        var startDate = LocalDate.of(2023, 1, 1);
        var expectedDate = LocalDate.of(2023, 1, 8);

        Subscriptions subscription = Subscriptions.builder()
                .startDate(startDate)
                .billingCycle(BillingCycle.WEEKLY)
                .build();

        var result = service.calculateNextBillingDate(subscription);

        assertEquals(expectedDate, result);
    }

    @Test
    void calculateNextBillingDate_ShouldResultPlusTwoWeeks_WhenBillingCycleIsBiweekly() {
        var startDate = LocalDate.of(2023, 1, 1);
        var expectedDate = LocalDate.of(2023, 1, 15);

        Subscriptions subscription = Subscriptions.builder()
                .startDate(startDate)
                .billingCycle(BillingCycle.BIWEEKLY)
                .build();

        var result = service.calculateNextBillingDate(subscription);

        assertEquals(expectedDate, result);
    }

    @Test
    void update_ShouldCreatePriceHistory_WhenValueChanges() {
        Long userId = 9L;
        Long subscriptionId = 7L;
        var oldValue = new BigDecimal("100.00");
        var newValue = new BigDecimal("110.00");

        Subscriptions current = subscriptionWithValue(oldValue);
        Subscriptions update = subscriptionWithValue(newValue);

        when(subscriptionsRepository.findByIdAndUserId(subscriptionId, userId)).thenReturn(java.util.Optional.of(current));
        when(userRepository.getReferenceById(userId)).thenReturn(User.builder().id(userId).build());
        when(subscriptionsRepository.save(update)).thenReturn(update);

        service.update(subscriptionId, update, userId);

        ArgumentCaptor<SubscriptionPriceHistory> historyCaptor = ArgumentCaptor.forClass(SubscriptionPriceHistory.class);
        verify(subscriptionPriceHistoryRepository, times(1)).save(historyCaptor.capture());
        SubscriptionPriceHistory savedHistory = historyCaptor.getValue();
        assertEquals(oldValue, savedHistory.getOldValue());
        assertEquals(newValue, savedHistory.getNewValue());
        assertEquals(new BigDecimal("10.00"), savedHistory.getChangePercentage());
        assertEquals(Boolean.TRUE, savedHistory.getIsPriceSpike());
    }

    @Test
    void update_ShouldNotCreatePriceHistory_WhenValueDoesNotChange() {
        Long userId = 9L;
        Long subscriptionId = 7L;
        var sameValue = new BigDecimal("100.00");

        Subscriptions current = subscriptionWithValue(sameValue);
        Subscriptions update = subscriptionWithValue(sameValue);

        when(subscriptionsRepository.findByIdAndUserId(subscriptionId, userId)).thenReturn(java.util.Optional.of(current));
        when(subscriptionsRepository.save(update)).thenReturn(update);

        service.update(subscriptionId, update, userId);

        verify(subscriptionPriceHistoryRepository, never()).save(any());
        verify(userRepository, never()).getReferenceById(any());
    }

    @Test
    void update_ShouldSetSpikeFalse_WhenIncreaseIsBelowTenPercent() {
        Long userId = 9L;
        Long subscriptionId = 7L;
        var oldValue = new BigDecimal("100.00");
        var newValue = new BigDecimal("109.99");

        Subscriptions current = subscriptionWithValue(oldValue);
        Subscriptions update = subscriptionWithValue(newValue);

        when(subscriptionsRepository.findByIdAndUserId(subscriptionId, userId)).thenReturn(java.util.Optional.of(current));
        when(userRepository.getReferenceById(userId)).thenReturn(User.builder().id(userId).build());
        when(subscriptionsRepository.save(update)).thenReturn(update);

        service.update(subscriptionId, update, userId);

        ArgumentCaptor<SubscriptionPriceHistory> historyCaptor = ArgumentCaptor.forClass(SubscriptionPriceHistory.class);
        verify(subscriptionPriceHistoryRepository).save(historyCaptor.capture());
        SubscriptionPriceHistory savedHistory = historyCaptor.getValue();
        assertEquals(new BigDecimal("9.99"), savedHistory.getChangePercentage());
        assertEquals(Boolean.FALSE, savedHistory.getIsPriceSpike());
    }

    @Test
    void update_ShouldHandleZeroOldValue_WithoutDivisionError() {
        Long userId = 9L;
        Long subscriptionId = 7L;

        Subscriptions current = subscriptionWithValue(new BigDecimal("0.00"));
        Subscriptions update = subscriptionWithValue(new BigDecimal("20.00"));

        when(subscriptionsRepository.findByIdAndUserId(subscriptionId, userId)).thenReturn(java.util.Optional.of(current));
        when(userRepository.getReferenceById(userId)).thenReturn(User.builder().id(userId).build());
        when(subscriptionsRepository.save(update)).thenReturn(update);

        service.update(subscriptionId, update, userId);

        ArgumentCaptor<SubscriptionPriceHistory> historyCaptor = ArgumentCaptor.forClass(SubscriptionPriceHistory.class);
        verify(subscriptionPriceHistoryRepository).save(historyCaptor.capture());
        SubscriptionPriceHistory savedHistory = historyCaptor.getValue();
        assertEquals(new BigDecimal("0"), savedHistory.getChangePercentage());
        assertEquals(Boolean.FALSE, savedHistory.getIsPriceSpike());
    }

    @Test
    void update_ShouldUseAuthenticatedUserOwnershipCheck() {
        Long userId = 9L;
        Long subscriptionId = 7L;

        Subscriptions current = subscriptionWithValue(new BigDecimal("100.00"));
        Subscriptions update = subscriptionWithValue(new BigDecimal("101.00"));

        when(subscriptionsRepository.findByIdAndUserId(subscriptionId, userId)).thenReturn(java.util.Optional.of(current));
        when(userRepository.getReferenceById(userId)).thenReturn(User.builder().id(userId).build());
        when(subscriptionsRepository.save(update)).thenReturn(update);

        service.update(subscriptionId, update, userId);

        verify(subscriptionsRepository).findByIdAndUserId(subscriptionId, userId);
        verify(subscriptionsRepository, never()).findById(any());
    }

    private Subscriptions subscriptionWithValue(BigDecimal value) {
        return Subscriptions.builder()
                .name("Netflix")
                .description("Netflix mensal")
                .value(value)
                .startDate(LocalDate.of(2026, 1, 1))
                .billingCycle(BillingCycle.MONTHLY)
                .build();
    }
}
