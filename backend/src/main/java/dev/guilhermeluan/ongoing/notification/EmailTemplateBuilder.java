package dev.guilhermeluan.ongoing.notification;

import dev.guilhermeluan.ongoing.subscriptions.entities.Currency;
import dev.guilhermeluan.ongoing.subscriptions.entities.Subscriptions;
import dev.guilhermeluan.ongoing.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
public class EmailTemplateBuilder {

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public String buildRenewalReminder(User user, List<Subscriptions> subscriptions, LocalDate date) {
        String formattedDate = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        var sb = new StringBuilder();
        sb.append(doctype());
        sb.append("<html lang=\"pt-BR\"><head><meta charset=\"UTF-8\">");
        sb.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        sb.append("<title>Lembrete de Renovação - Ongoing</title>");
        sb.append("</head>");
        sb.append("<body style=\"margin:0;padding:0;background-color:#f4f4f5;font-family:'Inter',Arial,Helvetica,sans-serif;\">");
        sb.append(wrapper(user, subscriptions, formattedDate));
        sb.append("</body></html>");
        return sb.toString();
    }

    private String doctype() {
        return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">";
    }

    private String wrapper(User user, List<Subscriptions> subscriptions, String formattedDate) {
        var sb = new StringBuilder();
        sb.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color:#f4f4f5;padding:32px 16px;\">");
        sb.append("<tr><td align=\"center\">");
        sb.append("<table role=\"presentation\" width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"max-width:600px;width:100%;background-color:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 6px rgba(0,0,0,0.07);\">");
        sb.append(header());
        sb.append(body(user, subscriptions, formattedDate));
        sb.append(footer());
        sb.append("</table>");
        sb.append("</td></tr></table>");
        return sb.toString();
    }

    private String header() {
        return "<tr><td style=\"background:linear-gradient(135deg,#22c55e,#8b5cf6);padding:32px 40px;text-align:center;\">"
               + "<h1 style=\"margin:0;font-family:'Plus Jakarta Sans','Inter',Arial,Helvetica,sans-serif;font-size:28px;font-weight:700;color:#ffffff;letter-spacing:-0.5px;\">Ongoing</h1>"
               + "<p style=\"margin:8px 0 0;font-size:14px;color:rgba(255,255,255,0.9);\">Lembrete de Renovação</p>"
               + "</td></tr>";
    }

    private String body(User user, List<Subscriptions> subscriptions, String formattedDate) {
        var sb = new StringBuilder();
        sb.append("<tr><td style=\"padding:32px 40px;\">");

        sb.append("<p style=\"margin:0 0 4px;font-size:18px;color:#18181b;font-weight:600;\">")
                .append("Olá, ").append(escapeHtml(user.getName())).append("!")
                .append("</p>");

        int count = subscriptions.size();
        sb.append("<p style=\"margin:0 0 24px;font-size:15px;color:#52525b;line-height:1.6;\">")
                .append("Você tem <strong style=\"color:#18181b;\">").append(count)
                .append(count == 1 ? " assinatura" : " assinaturas")
                .append("</strong> renovando amanhã (").append(formattedDate).append("):")
                .append("</p>");

        for (Subscriptions sub : subscriptions) {
            sb.append(subscriptionCard(sub));
        }

        sb.append(totalSection(subscriptions));

        sb.append(dashboardButton());

        sb.append("</td></tr>");
        return sb.toString();
    }

    private String subscriptionCard(Subscriptions sub) {
        String categoryName = sub.getCategory() != null ? sub.getCategory().getName() : "";
        String paymentMethodName = sub.getPaymentMethod() != null ? sub.getPaymentMethod().getName() : "";
        String billingCycleDisplay = sub.getBillingCycle() != null ? sub.getBillingCycle().getDisplayName() : "";

        var sb = new StringBuilder();
        sb.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color:#fafafa;border:1px solid #e4e4e7;border-radius:12px;margin-bottom:12px;\">");
        sb.append("<tr><td style=\"padding:16px 20px;\">");

        sb.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">");
        sb.append("<tr>");
        sb.append("<td style=\"font-size:16px;font-weight:600;color:#18181b;font-family:'Plus Jakarta Sans','Inter',Arial,Helvetica,sans-serif;\">")
                .append(escapeHtml(sub.getName()))
                .append("</td>");
        sb.append("<td align=\"right\" style=\"font-size:16px;font-weight:700;color:#22c55e;\">")
                .append(formatCurrency(sub.getValue(), sub.getCurrency()))
                .append("</td>");
        sb.append("</tr>");

        sb.append("<tr>");
        sb.append("<td style=\"font-size:13px;color:#71717a;padding-top:6px;\">");
        if (!paymentMethodName.isEmpty()) {
            sb.append(escapeHtml(paymentMethodName));
        }
        if (!categoryName.isEmpty()) {
            if (!paymentMethodName.isEmpty()) sb.append(" · ");
            sb.append(escapeHtml(categoryName));
        }
        sb.append("</td>");
        sb.append("<td align=\"right\" style=\"font-size:13px;color:#71717a;padding-top:6px;\">")
                .append(escapeHtml(billingCycleDisplay))
                .append("</td>");
        sb.append("</tr>");

        sb.append("</table>");
        sb.append("</td></tr></table>");
        return sb.toString();
    }

    private String totalSection(List<Subscriptions> subscriptions) {
        // Group totals by currency to avoid mixing BRL + USD + EUR into a single sum
        Map<Currency, BigDecimal> totalsByCurrency = subscriptions.stream()
                .collect(Collectors.groupingBy(
                        Subscriptions::getCurrency,
                        TreeMap::new,
                        Collectors.reducing(BigDecimal.ZERO, Subscriptions::getValue, BigDecimal::add)
                ));

        var sb = new StringBuilder();
        sb.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin:20px 0 24px;border-top:2px solid #e4e4e7;\">");
        sb.append("<tr><td style=\"padding-top:16px;\">");

        totalsByCurrency.forEach((currency, total) ->
                sb.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin-bottom:4px;\"><tr>")
                        .append("<td style=\"font-size:16px;font-weight:600;color:#18181b;\">Total</td>")
                        .append("<td align=\"right\" style=\"font-size:20px;font-weight:700;color:#18181b;\">")
                        .append(formatCurrency(total, currency))
                        .append("</td></tr></table>")
        );

        sb.append("</td></tr></table>");
        return sb.toString();
    }

    private String dashboardButton() {
        return "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">"
               + "<tr><td align=\"center\">"
               + "<a href=\"" + frontendUrl + "/dashboard\" target=\"_blank\" "
               + "style=\"display:inline-block;background-color:#22c55e;color:#ffffff;font-size:15px;font-weight:600;"
               + "text-decoration:none;padding:14px 32px;border-radius:10px;font-family:'Inter',Arial,Helvetica,sans-serif;\">"
               + "Ver no Dashboard"
               + "</a>"
               + "</td></tr></table>";
    }

    private String footer() {
        return "<tr><td style=\"background-color:#fafafa;padding:24px 40px;text-align:center;border-top:1px solid #e4e4e7;\">"
               + "<p style=\"margin:0;font-size:13px;color:#a1a1aa;\">Ongoing — Gerencie suas assinaturas</p>"
               + "</td></tr>";
    }

    private String formatCurrency(BigDecimal value, Currency currency) {
        Locale locale = switch (currency) {
            case BRL -> Locale.of("pt", "BR");
            case USD -> Locale.US;
            case EUR -> Locale.FRANCE;
        };
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        return formatter.format(value);
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
