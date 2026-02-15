package dev.guilhermeluan.ongoing.dashboard;

import dev.guilhermeluan.ongoing.dashboard.dto.DashboardResponse;
import dev.guilhermeluan.ongoing.exchange.ExchangeRateService;
import dev.guilhermeluan.ongoing.subscriptions.SubscriptionsService;
import dev.guilhermeluan.ongoing.subscriptions.entities.BillingCycle;
import dev.guilhermeluan.ongoing.subscriptions.entities.Category;
import dev.guilhermeluan.ongoing.subscriptions.entities.Currency;
import dev.guilhermeluan.ongoing.subscriptions.entities.Subscriptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private ExchangeRateService exchangeRateService;

    @Mock
    private SubscriptionsService subscriptionsService;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void getDashboard_ShouldReturnZeroedValues_WhenNoSubscriptions() {
        when(subscriptionsService.findActiveByUserId(1L)).thenReturn(List.of());

        DashboardResponse response = dashboardService.getDashboard(1L);

        assertThat(response.spendingByCategory()).isEmpty();
        assertThat(response.monthlyAverage()).isEqualByComparingTo("0.00");
        assertThat(response.thisMonthTotal()).isEqualByComparingTo("0.00");
        assertThat(response.yearlyTotal()).isEqualByComparingTo("0.00");
        assertThat(response.currency()).isEqualTo(Currency.BRL);
    }

    @Test
    void getDashboard_ShouldCalculateSpendingByCategory_WhenMultipleCategories() {
        Category streaming = Category.builder().id(1L).name("STREAMING").build();
        Category gaming = Category.builder().id(2L).name("GAMING").build();

        List<Subscriptions> subscriptions = List.of(
                createSubscription("Netflix", streaming, new BigDecimal("50.00"), Currency.BRL, BillingCycle.MONTHLY, LocalDate.now()),
                createSubscription("Spotify", streaming, new BigDecimal("30.00"), Currency.BRL, BillingCycle.MONTHLY, LocalDate.now()),
                createSubscription("Xbox", gaming, new BigDecimal("45.00"), Currency.BRL, BillingCycle.MONTHLY, LocalDate.now())
        );

        when(subscriptionsService.findActiveByUserId(1L)).thenReturn(subscriptions);
        when(exchangeRateService.convertToBrl(any(), eq(Currency.BRL))).thenAnswer(inv -> inv.getArgument(0));

        DashboardResponse response = dashboardService.getDashboard(1L);

        assertThat(response.spendingByCategory()).hasSize(2);
        assertThat(response.spendingByCategory().get(0).categoryName()).isEqualTo("STREAMING");
        assertThat(response.spendingByCategory().get(0).total()).isEqualByComparingTo("80.00");
        assertThat(response.spendingByCategory().get(1).categoryName()).isEqualTo("GAMING");
        assertThat(response.spendingByCategory().get(1).total()).isEqualByComparingTo("45.00");
    }

    @Test
    void getDashboard_ShouldOrderCategoriesByTotalDescending() {
        Category streaming = Category.builder().id(1L).name("STREAMING").build();
        Category gaming = Category.builder().id(2L).name("GAMING").build();
        Category productivity = Category.builder().id(3L).name("PRODUCTIVITY").build();

        List<Subscriptions> subscriptions = List.of(
                createSubscription("Netflix", streaming, new BigDecimal("30.00"), Currency.BRL, BillingCycle.MONTHLY, LocalDate.now()),
                createSubscription("Xbox", gaming, new BigDecimal("100.00"), Currency.BRL, BillingCycle.MONTHLY, LocalDate.now()),
                createSubscription("Microsoft 365", productivity, new BigDecimal("50.00"), Currency.BRL, BillingCycle.MONTHLY, LocalDate.now())
        );

        when(subscriptionsService.findActiveByUserId(1L)).thenReturn(subscriptions);
        when(exchangeRateService.convertToBrl(any(), eq(Currency.BRL))).thenAnswer(inv -> inv.getArgument(0));

        DashboardResponse response = dashboardService.getDashboard(1L);

        assertThat(response.spendingByCategory()).hasSize(3);
        assertThat(response.spendingByCategory().get(0).categoryName()).isEqualTo("GAMING");
        assertThat(response.spendingByCategory().get(0).total()).isEqualByComparingTo("100.00");
        assertThat(response.spendingByCategory().get(1).categoryName()).isEqualTo("PRODUCTIVITY");
        assertThat(response.spendingByCategory().get(1).total()).isEqualByComparingTo("50.00");
        assertThat(response.spendingByCategory().get(2).categoryName()).isEqualTo("STREAMING");
        assertThat(response.spendingByCategory().get(2).total()).isEqualByComparingTo("30.00");
    }

    @Test
    void getDashboard_ShouldConvertUsdToBrl_WhenCurrencyIsUsd() {
        Category streaming = Category.builder().id(1L).name("STREAMING").build();

        List<Subscriptions> subscriptions = List.of(
                createSubscription("Netflix", streaming, new BigDecimal("10.00"), Currency.USD, BillingCycle.MONTHLY, LocalDate.now())
        );

        when(subscriptionsService.findActiveByUserId(1L)).thenReturn(subscriptions);
        when(exchangeRateService.convertToBrl(new BigDecimal("10.00"), Currency.USD)).thenReturn(new BigDecimal("50.00"));

        DashboardResponse response = dashboardService.getDashboard(1L);

        assertThat(response.monthlyAverage()).isEqualByComparingTo("50.00");
        assertThat(response.yearlyTotal()).isEqualByComparingTo("600.00");
    }

    @Test
    void getDashboard_ShouldConvertEurToBrl_WhenCurrencyIsEur() {
        Category streaming = Category.builder().id(1L).name("STREAMING").build();

        List<Subscriptions> subscriptions = List.of(
                createSubscription("Spotify", streaming, new BigDecimal("10.00"), Currency.EUR, BillingCycle.MONTHLY, LocalDate.now())
        );

        when(subscriptionsService.findActiveByUserId(1L)).thenReturn(subscriptions);
        when(exchangeRateService.convertToBrl(new BigDecimal("10.00"), Currency.EUR)).thenReturn(new BigDecimal("55.00"));

        DashboardResponse response = dashboardService.getDashboard(1L);

        assertThat(response.monthlyAverage()).isEqualByComparingTo("55.00");
        assertThat(response.yearlyTotal()).isEqualByComparingTo("660.00");
    }

    @Test
    void getDashboard_ShouldCalculateMonthlyAverage_WhenYearlySubscription() {
        Category productivity = Category.builder().id(1L).name("PRODUCTIVITY").build();

        List<Subscriptions> subscriptions = List.of(
                createSubscription("Microsoft 365", productivity, new BigDecimal("120.00"), Currency.BRL, BillingCycle.YEARLY, LocalDate.now())
        );

        when(subscriptionsService.findActiveByUserId(1L)).thenReturn(subscriptions);
        when(exchangeRateService.convertToBrl(any(), eq(Currency.BRL))).thenAnswer(inv -> inv.getArgument(0));

        DashboardResponse response = dashboardService.getDashboard(1L);

        assertThat(response.monthlyAverage()).isEqualByComparingTo("10.00");
    }

    @Test
    void getDashboard_ShouldCalculateYearlyTotal_WhenMixOfMonthlyAndYearly() {
        Category streaming = Category.builder().id(1L).name("STREAMING").build();
        Category productivity = Category.builder().id(2L).name("PRODUCTIVITY").build();

        List<Subscriptions> subscriptions = List.of(
                createSubscription("Netflix", streaming, new BigDecimal("50.00"), Currency.BRL, BillingCycle.MONTHLY, LocalDate.now()),
                createSubscription("Microsoft 365", productivity, new BigDecimal("120.00"), Currency.BRL, BillingCycle.YEARLY, LocalDate.now())
        );

        when(subscriptionsService.findActiveByUserId(1L)).thenReturn(subscriptions);
        when(exchangeRateService.convertToBrl(any(), eq(Currency.BRL))).thenAnswer(inv -> inv.getArgument(0));

        DashboardResponse response = dashboardService.getDashboard(1L);

        assertThat(response.yearlyTotal()).isEqualByComparingTo("720.00");
    }

    @Test
    void getDashboard_ShouldCalculateThisMonthTotal_WhenOnlyCurrentMonthSubscriptions() {
        Category streaming = Category.builder().id(1L).name("STREAMING").build();
        Category gaming = Category.builder().id(2L).name("GAMING").build();

        LocalDate thisMonth = LocalDate.now();
        LocalDate nextMonth = LocalDate.now().plusMonths(1);

        List<Subscriptions> subscriptions = List.of(
                createSubscription("Netflix", streaming, new BigDecimal("50.00"), Currency.BRL, BillingCycle.MONTHLY, thisMonth),
                createSubscription("Xbox", gaming, new BigDecimal("45.00"), Currency.BRL, BillingCycle.MONTHLY, nextMonth)
        );

        when(subscriptionsService.findActiveByUserId(1L)).thenReturn(subscriptions);
        when(exchangeRateService.convertToBrl(any(), eq(Currency.BRL))).thenAnswer(inv -> inv.getArgument(0));

        DashboardResponse response = dashboardService.getDashboard(1L);

        assertThat(response.thisMonthTotal()).isEqualByComparingTo("50.00");
    }

    @Test
    void getDashboard_ShouldHandleMixedCurrencies_WhenMultipleCurrencies() {
        Category streaming = Category.builder().id(1L).name("STREAMING").build();
        Category gaming = Category.builder().id(2L).name("GAMING").build();
        Category productivity = Category.builder().id(3L).name("PRODUCTIVITY").build();

        List<Subscriptions> subscriptions = List.of(
                createSubscription("Netflix", streaming, new BigDecimal("30.00"), Currency.BRL, BillingCycle.MONTHLY, LocalDate.now()),
                createSubscription("Xbox", gaming, new BigDecimal("10.00"), Currency.USD, BillingCycle.MONTHLY, LocalDate.now()),
                createSubscription("Spotify", productivity, new BigDecimal("10.00"), Currency.EUR, BillingCycle.MONTHLY, LocalDate.now())
        );

        when(subscriptionsService.findActiveByUserId(1L)).thenReturn(subscriptions);
        when(exchangeRateService.convertToBrl(any(), eq(Currency.BRL))).thenAnswer(inv -> inv.getArgument(0));
        when(exchangeRateService.convertToBrl(new BigDecimal("10.00"), Currency.USD)).thenReturn(new BigDecimal("50.00"));
        when(exchangeRateService.convertToBrl(new BigDecimal("10.00"), Currency.EUR)).thenReturn(new BigDecimal("55.00"));

        DashboardResponse response = dashboardService.getDashboard(1L);

        assertThat(response.monthlyAverage()).isEqualByComparingTo("135.00");
        assertThat(response.yearlyTotal()).isEqualByComparingTo("1620.00");
    }

    @Test
    void getDashboard_ShouldCalculateMonthlyAverage_WhenQuarterlySubscription() {
        Category productivity = Category.builder().id(1L).name("PRODUCTIVITY").build();

        List<Subscriptions> subscriptions = List.of(
                createSubscription("Adobe", productivity, new BigDecimal("90.00"), Currency.BRL, BillingCycle.QUARTERLY, LocalDate.now())
        );

        when(subscriptionsService.findActiveByUserId(1L)).thenReturn(subscriptions);
        when(exchangeRateService.convertToBrl(any(), eq(Currency.BRL))).thenAnswer(inv -> inv.getArgument(0));

        DashboardResponse response = dashboardService.getDashboard(1L);

        assertThat(response.monthlyAverage()).isEqualByComparingTo("30.00");
    }

    private Subscriptions createSubscription(String name, Category category, BigDecimal value,
                                             Currency currency, BillingCycle billingCycle, LocalDate nextPaymentDate) {
        return Subscriptions.builder()
                .name(name)
                .value(value)
                .currency(currency)
                .billingCycle(billingCycle)
                .category(category)
                .nextPaymentDate(nextPaymentDate)
                .active(true)
                .build();
    }
}
