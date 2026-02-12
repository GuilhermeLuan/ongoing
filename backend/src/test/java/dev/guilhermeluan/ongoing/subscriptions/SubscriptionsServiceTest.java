package dev.guilhermeluan.ongoing.subscriptions;

import dev.guilhermeluan.ongoing.exception.BadRequestException;
import dev.guilhermeluan.ongoing.subscriptions.entities.BillingCycle;
import dev.guilhermeluan.ongoing.subscriptions.entities.Subscriptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class SubscriptionsServiceTest {
    @InjectMocks
    private SubscriptionsService service;

    @Mock
    private SubscriptionsRepository subscriptionsRepository;


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
}