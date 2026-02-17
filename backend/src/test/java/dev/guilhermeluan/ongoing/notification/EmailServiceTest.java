package dev.guilhermeluan.ongoing.notification;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import dev.guilhermeluan.ongoing.subscriptions.entities.BillingCycle;
import dev.guilhermeluan.ongoing.subscriptions.entities.Currency;
import dev.guilhermeluan.ongoing.subscriptions.entities.Subscriptions;
import dev.guilhermeluan.ongoing.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @InjectMocks
    private EmailService emailService;

    @Mock
    private EmailTemplateBuilder emailTemplateBuilder;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Resend resend;

    @Test
    void sendRenewalReminder_ShouldBuildTemplateAndSendEmail() throws Exception {
        User user = User.builder().id(1L).name("Guilherme").email("gui@test.com").build();
        LocalDate date = LocalDate.of(2026, 2, 18);
        List<Subscriptions> subs = List.of(
                Subscriptions.builder()
                        .name("Netflix")
                        .value(new BigDecimal("55.90"))
                        .currency(Currency.BRL)
                        .billingCycle(BillingCycle.MONTHLY)
                        .build()
        );

        when(emailTemplateBuilder.buildRenewalReminder(user, subs, date))
                .thenReturn("<html>test</html>");

        emailService.sendRenewalReminder(user, subs, date);

        verify(emailTemplateBuilder).buildRenewalReminder(user, subs, date);

        ArgumentCaptor<CreateEmailOptions> captor = ArgumentCaptor.forClass(CreateEmailOptions.class);
        verify(resend.emails()).send(captor.capture());
        assertEquals(List.of(user.getEmail()), captor.getValue().getTo());
    }
}
