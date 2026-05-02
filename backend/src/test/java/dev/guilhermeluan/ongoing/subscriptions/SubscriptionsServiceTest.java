// HU01 - Criar Assinatura
package dev.guilhermeluan.ongoing.subscriptions;

import dev.guilhermeluan.ongoing.exception.BadRequestException;
import dev.guilhermeluan.ongoing.exception.NotFoundException;
import dev.guilhermeluan.ongoing.subscriptions.entities.BillingCycle;
import dev.guilhermeluan.ongoing.subscriptions.entities.Subscriptions;
import dev.guilhermeluan.ongoing.user.User;
import dev.guilhermeluan.ongoing.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionsServiceTest {
    @InjectMocks
    private SubscriptionsService service;

    @Mock
    private SubscriptionsRepository subscriptionsRepository;

    @Mock
    private UserRepository userRepository;

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
    void save_ShouldSetUserAndCalculateNextPaymentDate_WhenValidSubscription() {
        Long userId = 1L;
        User user = User.builder().id(userId).email("user@example.com").build();
        Subscriptions subscription = Subscriptions.builder()
                .startDate(LocalDate.of(2023, 1, 1))
                .billingCycle(BillingCycle.MONTHLY)
                .build();

        when(userRepository.getReferenceById(userId)).thenReturn(user);
        when(subscriptionsRepository.save(any(Subscriptions.class))).thenAnswer(inv -> inv.getArgument(0));

        Subscriptions result = service.save(subscription, userId);

        assertEquals(user, result.getUser());
        assertEquals(LocalDate.of(2023, 2, 1), result.getNextPaymentDate());
        verify(subscriptionsRepository).save(subscription);
    }

    @Test
    void findAll_ShouldDelegateToFindWithFilters_WhenAnyFilterPresent() {
        Long userId = 1L;
        Pageable pageable = Pageable.unpaged();
        Page<Subscriptions> page = new PageImpl<>(List.of());

        when(subscriptionsRepository.findWithFilters("Netflix", null, null, userId, pageable)).thenReturn(page);

        Page<Subscriptions> result = service.findAll("Netflix", null, null, pageable, userId);

        assertEquals(page, result);
        verify(subscriptionsRepository).findWithFilters("Netflix", null, null, userId, pageable);
        verify(subscriptionsRepository, never()).findAllByUserId(any(), any());
    }

    @Test
    void findAll_ShouldDelegateToFindAllByUserId_WhenNoFilters() {
        Long userId = 1L;
        Pageable pageable = Pageable.unpaged();
        Page<Subscriptions> page = new PageImpl<>(List.of());

        when(subscriptionsRepository.findAllByUserId(userId, pageable)).thenReturn(page);

        Page<Subscriptions> result = service.findAll(null, null, null, pageable, userId);

        assertEquals(page, result);
        verify(subscriptionsRepository).findAllByUserId(userId, pageable);
        verify(subscriptionsRepository, never()).findWithFilters(any(), any(), any(), any(), any());
    }

    @Test
    void findByIdOrThrowNotFoundException_ShouldReturnSubscription_WhenFound() {
        Long id = 1L;
        Long userId = 2L;
        Subscriptions sub = Subscriptions.builder().id(id).build();

        when(subscriptionsRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.of(sub));

        Subscriptions result = service.findByIdOrThrowNotFoundException(id, userId);

        assertEquals(sub, result);
    }

    @Test
    void findByIdOrThrowNotFoundException_ShouldThrow_WhenNotFound() {
        Long id = 1L;
        Long userId = 2L;

        when(subscriptionsRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.findByIdOrThrowNotFoundException(id, userId));
    }

    @Test
    void deleteById_ShouldDeleteSubscription_WhenExists() {
        Long id = 1L;
        Long userId = 2L;
        Subscriptions sub = Subscriptions.builder().id(id).build();

        when(subscriptionsRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.of(sub));

        service.deleteById(id, userId);

        verify(subscriptionsRepository).deleteById(id);
    }

    @Test
    void deleteById_ShouldThrow_WhenNotFound() {
        Long id = 1L;
        Long userId = 2L;

        when(subscriptionsRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.deleteById(id, userId));
        verify(subscriptionsRepository, never()).deleteById(any());
    }

    @Test
    void update_ShouldRecalculateNextPaymentDate_WhenCalled() {
        Long id = 1L;
        Long userId = 2L;
        Subscriptions existing = Subscriptions.builder().id(id).build();
        Subscriptions updated = Subscriptions.builder()
                .id(id)
                .startDate(LocalDate.of(2023, 1, 1))
                .billingCycle(BillingCycle.MONTHLY)
                .build();

        when(subscriptionsRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.of(existing));
        when(subscriptionsRepository.save(any(Subscriptions.class))).thenAnswer(inv -> inv.getArgument(0));

        Subscriptions result = service.update(id, updated, userId);

        assertEquals(LocalDate.of(2023, 2, 1), result.getNextPaymentDate());
        verify(subscriptionsRepository).save(updated);
    }

    @Test
    void findRenewalSubscriptionsGroupedByUser_ShouldGroupByUser_WhenSubscriptionsExist() {
        User user1 = User.builder().id(1L).build();
        User user2 = User.builder().id(2L).build();

        Subscriptions sub1 = Subscriptions.builder().id(1L).user(user1).build();
        Subscriptions sub2 = Subscriptions.builder().id(2L).user(user1).build();
        Subscriptions sub3 = Subscriptions.builder().id(3L).user(user2).build();

        LocalDate date = LocalDate.of(2023, 2, 1);
        when(subscriptionsRepository.findByNextPaymentDateAndActiveAndNotify(date))
                .thenReturn(List.of(sub1, sub2, sub3));

        Map<User, List<Subscriptions>> result = service.findRenewalSubscriptionsGroupedByUser(date);

        assertEquals(2, result.size());
        assertEquals(2, result.get(user1).size());
        assertEquals(1, result.get(user2).size());
    }
}
