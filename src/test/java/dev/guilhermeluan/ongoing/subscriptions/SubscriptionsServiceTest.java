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
                .billingCycle(BillingCycle.builder().id(1L).build())
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
                .billingCycle(BillingCycle.builder().id(2L).build())
                .build();

        var result = service.calculateNextBillingDate(subscription);

        assertEquals(expectedDate, result);
    }

    @Test
    void calculateNextBillingDate_ShouldThrowBadRequestException_WhenBillingCycleIsUnknown() {

        var startDate = LocalDate.of(2023, 1, 1);

        Subscriptions subscription = Subscriptions.builder()
                .startDate(startDate)
                .billingCycle(BillingCycle.builder().id(99L).build())
                .build();

        assertThrows(BadRequestException.class, () -> service.calculateNextBillingDate(subscription));
    }
}