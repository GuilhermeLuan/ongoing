package dev.guilhermeluan.ongoing.dashboard;

import dev.guilhermeluan.ongoing.dashboard.dto.CategorySpending;
import dev.guilhermeluan.ongoing.dashboard.dto.DashboardResponse;
import dev.guilhermeluan.ongoing.exchange.ExchangeRateService;
import dev.guilhermeluan.ongoing.subscriptions.SubscriptionsService;
import dev.guilhermeluan.ongoing.subscriptions.entities.Currency;
import dev.guilhermeluan.ongoing.subscriptions.entities.Subscriptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final ExchangeRateService exchangeRateService;
    private final SubscriptionsService subscriptionsService;

    public DashboardResponse getDashboard(Long userId) {
        log.info("Fetching dashboard for user {}", userId);

        List<Subscriptions> subscriptions = subscriptionsService.findActiveByUserId(userId);

        if (subscriptions.isEmpty()) {
            log.debug("No active subscriptions found for user {}", userId);
            return createEmptyDashboard();
        }

        log.debug("Found {} active subscriptions for user {}", subscriptions.size(), userId);
        List<ConvertedSubscription> converted = subscriptions.stream()
                .map(this::convertToBrl)
                .toList();

        return new DashboardResponse(
                calculateSpendingByCategory(converted),
                calculateMonthlyAverage(converted),
                calculateThisMonthTotal(converted),
                calculateYearlyTotal(converted),
                Currency.BRL,
                LocalDate.now()
        );
    }

    private ConvertedSubscription convertToBrl(Subscriptions subscriptions) {
        BigDecimal priceInBrl = exchangeRateService.convertToBrl(subscriptions.getValue(), subscriptions.getCurrency());
        return new ConvertedSubscription(subscriptions, priceInBrl);
    }

    private List<CategorySpending> calculateSpendingByCategory(List<ConvertedSubscription> subscriptions) {
        return subscriptions.stream()
                .collect(Collectors.groupingBy(
                        s -> s.subscription().getCategory(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                ConvertedSubscription::monthlyPrice,
                                BigDecimal::add
                        )
                ))
                .entrySet().stream()
                .map(e -> new CategorySpending(
                        e.getKey().getId(),
                        e.getKey().getName(),
                        e.getValue()
                ))
                .sorted(Comparator.comparing(CategorySpending::total).reversed())
                .toList();
    }

    private BigDecimal calculateMonthlyAverage(List<ConvertedSubscription> subscriptions) {
        return subscriptions.stream()
                .map(ConvertedSubscription::monthlyPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateThisMonthTotal(List<ConvertedSubscription> subscriptions) {
        YearMonth currentMonth = YearMonth.now();

        return subscriptions.stream()
                .filter(s -> s.isDueIn(currentMonth))
                .map(ConvertedSubscription::priceInBrl)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateYearlyTotal(List<ConvertedSubscription> subscriptions) {
        return subscriptions.stream()
                .map(ConvertedSubscription::yearlyPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private DashboardResponse createEmptyDashboard() {
        return new DashboardResponse(
                List.of(),
                BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                Currency.BRL,
                LocalDate.now()
        );
    }
}
