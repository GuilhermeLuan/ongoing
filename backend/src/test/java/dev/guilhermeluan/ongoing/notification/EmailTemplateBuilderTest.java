package dev.guilhermeluan.ongoing.notification;

import dev.guilhermeluan.ongoing.subscriptions.entities.*;
import dev.guilhermeluan.ongoing.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailTemplateBuilderTest {

    private EmailTemplateBuilder builder;

    @BeforeEach
    void setUp() throws Exception {
        builder = new EmailTemplateBuilder();
        Field field = EmailTemplateBuilder.class.getDeclaredField("frontendUrl");
        field.setAccessible(true);
        field.set(builder, "https://ongoing.app");
    }

    @Test
    void buildRenewalReminder_ShouldContainUserName() {
        User user = User.builder().id(1L).name("Guilherme").email("gui@test.com").build();
        List<Subscriptions> subs = List.of(buildSubscription("Netflix", "55.90"));

        String html = builder.buildRenewalReminder(user, subs, LocalDate.of(2026, 2, 18));

        assertTrue(html.contains("Guilherme"));
    }

    @Test
    void buildRenewalReminder_ShouldContainFormattedDate() {
        User user = User.builder().id(1L).name("Ana").email("ana@test.com").build();
        List<Subscriptions> subs = List.of(buildSubscription("Spotify", "21.90"));

        String html = builder.buildRenewalReminder(user, subs, LocalDate.of(2026, 2, 18));

        assertTrue(html.contains("18/02/2026"));
    }

    @Test
    void buildRenewalReminder_ShouldContainSubscriptionNames() {
        User user = User.builder().id(1L).name("Ana").email("ana@test.com").build();
        List<Subscriptions> subs = List.of(
                buildSubscription("Netflix", "55.90"),
                buildSubscription("Spotify", "21.90")
        );

        String html = builder.buildRenewalReminder(user, subs, LocalDate.of(2026, 2, 18));

        assertTrue(html.contains("Netflix"));
        assertTrue(html.contains("Spotify"));
    }

    @Test
    void buildRenewalReminder_ShouldContainFormattedValues() {
        User user = User.builder().id(1L).name("Ana").email("ana@test.com").build();
        List<Subscriptions> subs = List.of(buildSubscription("Netflix", "55.90"));

        String html = builder.buildRenewalReminder(user, subs, LocalDate.of(2026, 2, 18));

        assertTrue(html.contains("55,90"), "Should contain BRL formatted value");
    }

    @Test
    void buildRenewalReminder_ShouldContainTotalValue() {
        User user = User.builder().id(1L).name("Ana").email("ana@test.com").build();
        List<Subscriptions> subs = List.of(
                buildSubscription("Netflix", "55.90"),
                buildSubscription("Spotify", "21.90")
        );

        String html = builder.buildRenewalReminder(user, subs, LocalDate.of(2026, 2, 18));

        assertTrue(html.contains("77,80"), "Should contain total value (55.90 + 21.90)");
    }

    @Test
    void buildRenewalReminder_ShouldContainCategoryAndPaymentMethod() {
        User user = User.builder().id(1L).name("Ana").email("ana@test.com").build();
        Category category = Category.builder().id(1L).name("Entretenimento").build();
        PaymentMethod payment = PaymentMethod.builder().id(1L).name("Cartão Nubank").build();
        Subscriptions sub = Subscriptions.builder()
                .name("Netflix")
                .value(new BigDecimal("55.90"))
                .currency(Currency.BRL)
                .billingCycle(BillingCycle.MONTHLY)
                .category(category)
                .paymentMethod(payment)
                .build();

        String html = builder.buildRenewalReminder(user, List.of(sub), LocalDate.of(2026, 2, 18));

        assertTrue(html.contains("Entretenimento"));
        assertTrue(html.contains("Cartão Nubank"));
    }

    @Test
    void buildRenewalReminder_ShouldContainBillingCycleDisplayName() {
        User user = User.builder().id(1L).name("Ana").email("ana@test.com").build();
        List<Subscriptions> subs = List.of(buildSubscription("Netflix", "55.90"));

        String html = builder.buildRenewalReminder(user, subs, LocalDate.of(2026, 2, 18));

        assertTrue(html.contains("Monthly"));
    }

    @Test
    void buildRenewalReminder_ShouldContainDashboardLink() {
        User user = User.builder().id(1L).name("Ana").email("ana@test.com").build();
        List<Subscriptions> subs = List.of(buildSubscription("Netflix", "55.90"));

        String html = builder.buildRenewalReminder(user, subs, LocalDate.of(2026, 2, 18));

        assertTrue(html.contains("https://ongoing.app/dashboard"));
        assertTrue(html.contains("Ver no Dashboard"));
    }

    @Test
    void buildRenewalReminder_ShouldContainBrandingElements() {
        User user = User.builder().id(1L).name("Ana").email("ana@test.com").build();
        List<Subscriptions> subs = List.of(buildSubscription("Netflix", "55.90"));

        String html = builder.buildRenewalReminder(user, subs, LocalDate.of(2026, 2, 18));

        assertTrue(html.contains("Ongoing"), "Should contain brand name");
        assertTrue(html.contains("#22c55e"), "Should contain primary green color");
        assertTrue(html.contains("#8b5cf6"), "Should contain accent purple color");
        assertTrue(html.contains("linear-gradient"), "Should contain gradient");
        assertTrue(html.contains("Gerencie suas assinaturas"), "Should contain footer text");
    }

    @Test
    void buildRenewalReminder_ShouldUseSingularWhenOneSubscription() {
        User user = User.builder().id(1L).name("Ana").email("ana@test.com").build();
        List<Subscriptions> subs = List.of(buildSubscription("Netflix", "55.90"));

        String html = builder.buildRenewalReminder(user, subs, LocalDate.of(2026, 2, 18));

        assertTrue(html.contains("1 assinatura"));
        assertFalse(html.contains("1 assinaturas"));
    }

    @Test
    void buildRenewalReminder_ShouldUsePluralWhenMultipleSubscriptions() {
        User user = User.builder().id(1L).name("Ana").email("ana@test.com").build();
        List<Subscriptions> subs = List.of(
                buildSubscription("Netflix", "55.90"),
                buildSubscription("Spotify", "21.90")
        );

        String html = builder.buildRenewalReminder(user, subs, LocalDate.of(2026, 2, 18));

        assertTrue(html.contains("2 assinaturas"));
    }

    @Test
    void buildRenewalReminder_ShouldEscapeHtmlInUserName() {
        User user = User.builder().id(1L).name("<script>alert('xss')</script>").email("xss@test.com").build();
        List<Subscriptions> subs = List.of(buildSubscription("Netflix", "55.90"));

        String html = builder.buildRenewalReminder(user, subs, LocalDate.of(2026, 2, 18));

        assertFalse(html.contains("<script>"), "Should escape HTML in user name");
        assertTrue(html.contains("&lt;script&gt;"));
    }

    @Test
    void buildRenewalReminder_ShouldHandleNullCategoryAndPaymentMethod() {
        User user = User.builder().id(1L).name("Ana").email("ana@test.com").build();
        Subscriptions sub = Subscriptions.builder()
                .name("Custom Sub")
                .value(new BigDecimal("10.00"))
                .currency(Currency.BRL)
                .billingCycle(BillingCycle.MONTHLY)
                .category(null)
                .paymentMethod(null)
                .build();

        String html = builder.buildRenewalReminder(user, List.of(sub), LocalDate.of(2026, 2, 18));

        assertTrue(html.contains("Custom Sub"));
    }

    @Test
    void buildRenewalReminder_ShouldFormatUSDCurrency() {
        User user = User.builder().id(1L).name("Ana").email("ana@test.com").build();
        Subscriptions sub = Subscriptions.builder()
                .name("ChatGPT")
                .value(new BigDecimal("20.00"))
                .currency(Currency.USD)
                .billingCycle(BillingCycle.MONTHLY)
                .build();

        String html = builder.buildRenewalReminder(user, List.of(sub), LocalDate.of(2026, 2, 18));

        assertTrue(html.contains("$20.00"), "Should format USD correctly");
    }

    @Test
    void buildRenewalReminder_ShouldShowSeparateTotalsForMixedCurrencies() {
        User user = User.builder().id(1L).name("Ana").email("ana@test.com").build();
        Subscriptions brlSub = Subscriptions.builder()
                .name("Netflix")
                .value(new BigDecimal("55.90"))
                .currency(Currency.BRL)
                .billingCycle(BillingCycle.MONTHLY)
                .build();
        Subscriptions usdSub = Subscriptions.builder()
                .name("ChatGPT")
                .value(new BigDecimal("20.00"))
                .currency(Currency.USD)
                .billingCycle(BillingCycle.MONTHLY)
                .build();

        String html = builder.buildRenewalReminder(user, List.of(brlSub, usdSub), LocalDate.of(2026, 2, 18));

        assertTrue(html.contains("55,90"), "Should contain BRL total");
        assertTrue(html.contains("$20.00"), "Should contain USD total separately");
        // The two totals must NOT be summed — 75.90 should not appear anywhere
        assertFalse(html.contains("75,90"), "Should NOT mix BRL and USD into a single total");
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
