package dev.guilhermeluan.ongoing.notification;

import dev.guilhermeluan.ongoing.subscriptions.SubscriptionsService;
import dev.guilhermeluan.ongoing.subscriptions.entities.Subscriptions;
import dev.guilhermeluan.ongoing.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final SubscriptionsService subscriptionsService;
    private final EmailService emailService;
    private final StringRedisTemplate redis;

    @Scheduled(cron = "0 0 8 * * *")
    public void sendRenewalReminders() {
        LocalDate date = LocalDate.now().plusDays(1);
        log.info("Starting renewal reminder job for date {}", date);
        Map<User, List<Subscriptions>> grouped = subscriptionsService.findRenewalSubscriptionsGroupedByUser(date);

        AtomicInteger sentCount = new AtomicInteger(0);

        grouped.forEach((user, subscriptions) -> {
            try {
                String redisKey = String.format("reminder:%s:%s", user.getId(), date);
                if (Boolean.FALSE.equals(redis.hasKey(redisKey))) {
                    emailService.sendRenewalReminder(user, subscriptions, date);
                    redis.opsForValue().set(redisKey, "sent", 48, TimeUnit.HOURS);
                    sentCount.incrementAndGet();
                }
            } catch (Exception e) {
                log.error("Failed to send renewal reminders for user {}", user.getId(), e);
            }
        });

        log.info("Renewal reminder job finished: sent {} of {} emails", sentCount.get(), grouped.size());
    }
}
