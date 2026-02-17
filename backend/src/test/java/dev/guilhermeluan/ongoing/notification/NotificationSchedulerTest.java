package dev.guilhermeluan.ongoing.notification;

import dev.guilhermeluan.ongoing.subscriptions.SubscriptionsService;
import dev.guilhermeluan.ongoing.subscriptions.entities.BillingCycle;
import dev.guilhermeluan.ongoing.subscriptions.entities.Currency;
import dev.guilhermeluan.ongoing.subscriptions.entities.Subscriptions;
import dev.guilhermeluan.ongoing.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationSchedulerTest {

    @InjectMocks
    private NotificationScheduler scheduler;

    @Mock
    private SubscriptionsService subscriptionsService;

    @Mock
    private EmailService emailService;

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    void sendRenewalReminders_ShouldCallServiceNotRepository() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        when(subscriptionsService.findRenewalSubscriptionsGroupedByUser(tomorrow))
                .thenReturn(Map.of());

        scheduler.sendRenewalReminders();

        verify(subscriptionsService).findRenewalSubscriptionsGroupedByUser(tomorrow);
    }

    @Test
    void sendRenewalReminders_ShouldSendEmailWhenRedisKeyDoesNotExist() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        User user = User.builder().id(1L).name("Guilherme").email("gui@test.com").build();
        List<Subscriptions> subs = List.of(buildSubscription("Netflix", "55.90"));

        when(subscriptionsService.findRenewalSubscriptionsGroupedByUser(tomorrow))
                .thenReturn(Map.of(user, subs));
        when(redis.hasKey(anyString())).thenReturn(false);
        when(redis.opsForValue()).thenReturn(valueOperations);

        scheduler.sendRenewalReminders();

        verify(emailService).sendRenewalReminder(user, subs, tomorrow);
    }

    @Test
    void sendRenewalReminders_ShouldNotSendEmailWhenRedisKeyExists() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        User user = User.builder().id(1L).name("Guilherme").email("gui@test.com").build();
        List<Subscriptions> subs = List.of(buildSubscription("Netflix", "55.90"));

        when(subscriptionsService.findRenewalSubscriptionsGroupedByUser(tomorrow))
                .thenReturn(Map.of(user, subs));
        when(redis.hasKey(anyString())).thenReturn(true);

        scheduler.sendRenewalReminders();

        verify(emailService, never()).sendRenewalReminder(any(), any(), any());
    }

    @Test
    void sendRenewalReminders_ShouldSetRedisKeyWithTTLAfterSending() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        User user = User.builder().id(1L).name("Guilherme").email("gui@test.com").build();
        List<Subscriptions> subs = List.of(buildSubscription("Netflix", "55.90"));
        String expectedKey = "reminder:" + user.getId() + ":" + tomorrow;

        when(subscriptionsService.findRenewalSubscriptionsGroupedByUser(tomorrow))
                .thenReturn(Map.of(user, subs));
        when(redis.hasKey(expectedKey)).thenReturn(false);
        when(redis.opsForValue()).thenReturn(valueOperations);

        scheduler.sendRenewalReminders();

        verify(valueOperations).set(expectedKey, "sent", 48, TimeUnit.HOURS);
    }

    @Test
    void sendRenewalReminders_ShouldHandleMultipleUsersIndependently() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        User user1 = User.builder().id(1L).name("Guilherme").email("gui@test.com").build();
        User user2 = User.builder().id(2L).name("Ana").email("ana@test.com").build();
        List<Subscriptions> subs1 = List.of(buildSubscription("Netflix", "55.90"));
        List<Subscriptions> subs2 = List.of(buildSubscription("Spotify", "21.90"));
        String key1 = "reminder:1:" + tomorrow;
        String key2 = "reminder:2:" + tomorrow;

        when(subscriptionsService.findRenewalSubscriptionsGroupedByUser(tomorrow))
                .thenReturn(Map.of(user1, subs1, user2, subs2));
        when(redis.hasKey(key1)).thenReturn(true);
        when(redis.hasKey(key2)).thenReturn(false);
        when(redis.opsForValue()).thenReturn(valueOperations);

        scheduler.sendRenewalReminders();

        verify(emailService, never()).sendRenewalReminder(eq(user1), any(), any());
        verify(emailService).sendRenewalReminder(user2, subs2, tomorrow);
    }

    @Test
    void sendRenewalReminders_ShouldDoNothingWhenNoSubscriptions() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        when(subscriptionsService.findRenewalSubscriptionsGroupedByUser(tomorrow))
                .thenReturn(Map.of());

        scheduler.sendRenewalReminders();

        verifyNoInteractions(emailService);
        verify(redis, never()).hasKey(anyString());
    }

    private Subscriptions buildSubscription(String name, String value) {
        return Subscriptions.builder()
                .name(name)
                .value(new BigDecimal(value))
                .currency(Currency.BRL)
                .billingCycle(BillingCycle.MONTHLY)
                .build();
    }
}
