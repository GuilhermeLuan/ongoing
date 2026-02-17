package dev.guilhermeluan.ongoing.notification;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import dev.guilhermeluan.ongoing.subscriptions.entities.Subscriptions;
import dev.guilhermeluan.ongoing.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final Resend resend;
    private final EmailTemplateBuilder emailTemplateBuilder;
    @Value("${resend.from_email}")
    private String resendFromEmail;

    public void sendRenewalReminder(User user, List<Subscriptions> subscriptions, LocalDate localDate) {
        String body = emailTemplateBuilder.buildRenewalReminder(user, subscriptions, localDate);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(resendFromEmail)
                .to(user.getEmail())
                .subject("As seguintes assinaturas são renovar amanhã")
                .html(body)
                .build();

        try {
            CreateEmailResponse data = resend.emails().send(params);

            log.info("Email sent to {} with id {}", user.getEmail(), data.getId());
        } catch (ResendException e) {
            throw new RuntimeException(e);
        }
    }

}
