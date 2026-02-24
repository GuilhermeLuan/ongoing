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
        sb.append("<body style=\"margin:0;padding:0;background-color:#f0fdf4;font-family:'Inter','Segoe UI',Arial,Helvetica,sans-serif;\">");
        sb.append(wrapper(user, subscriptions, formattedDate));
        sb.append("</body></html>");
        return sb.toString();
    }

    private String doctype() {
        return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">";
    }

    private String wrapper(User user, List<Subscriptions> subscriptions, String formattedDate) {
        var sb = new StringBuilder();
        sb.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:linear-gradient(180deg,#f0fdf4 0%,#ffffff 48%,#fafafa 100%);padding:32px 16px;\">");
        sb.append("<tr><td align=\"center\">");
        sb.append("<table role=\"presentation\" width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"max-width:600px;width:100%;background-color:#ffffff;border:1px solid #e5e5e5;border-radius:16px;overflow:hidden;box-shadow:0 8px 24px -12px rgba(0,0,0,0.25),0 4px 12px -6px rgba(0,0,0,0.16);\">");
        sb.append(header());
        sb.append(body(user, subscriptions, formattedDate));
        sb.append(footer());
        sb.append("</table>");
        sb.append("</td></tr></table>");
        return sb.toString();
    }

    private String header() {
        return "<tr><td style=\"height:8px;padding:0;background:linear-gradient(135deg,#22c55e 0%,#8b5cf6 100%);\"></td></tr>"
               + "<tr><td style=\"padding:28px 40px 24px;background-color:#ffffff;border-bottom:1px solid #e5e5e5;\">"
               + "<p style=\"margin:0 0 12px;text-align:center;\">"
               + "<span style=\"display:inline-block;background-color:#dcfce7;color:#166534;border:1px solid #bbf7d0;border-radius:999px;padding:6px 12px;font-size:12px;font-weight:700;letter-spacing:0.2px;text-transform:uppercase;\">Renovacoes de amanha</span>"
               + "</p>"
               + "<h1 style=\"margin:0;text-align:center;font-family:'Plus Jakarta Sans','Inter','Segoe UI',Arial,Helvetica,sans-serif;font-size:30px;font-weight:800;color:#171717;letter-spacing:-0.6px;line-height:1.2;\">Ongoing</h1>"
               + "<p style=\"margin:8px 0 0;text-align:center;font-size:14px;color:#525252;line-height:1.6;\">Sua rotina de assinaturas com o mesmo visual do dashboard.</p>"
               + "</td></tr>";
    }

    private String body(User user, List<Subscriptions> subscriptions, String formattedDate) {
        var sb = new StringBuilder();
        sb.append("<tr><td style=\"padding:32px 40px;\">");

        sb.append("<p style=\"margin:0 0 6px;font-size:20px;color:#171717;font-weight:700;font-family:'Plus Jakarta Sans','Inter','Segoe UI',Arial,Helvetica,sans-serif;\">")
                .append("Olá, ").append(escapeHtml(user.getName())).append("!")
                .append("</p>");

        int count = subscriptions.size();
        sb.append("<p style=\"margin:0 0 18px;font-size:15px;color:#525252;line-height:1.7;\">")
                .append("Você tem <strong style=\"color:#18181b;\">").append(count)
                .append(count == 1 ? " assinatura" : " assinaturas")
                .append("</strong> renovando amanhã (").append(formattedDate).append("):")
                .append("</p>");

        sb.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin:0 0 18px;background-color:#fafafa;border:1px solid #e5e5e5;border-radius:12px;\">");
        sb.append("<tr><td style=\"padding:14px 16px;font-size:13px;color:#404040;line-height:1.5;\">");
        sb.append("<strong style=\"color:#16a34a;\">Resumo:</strong> mantenha o dashboard atualizado para evitar cobranças inesperadas.");
        sb.append("</td></tr></table>");

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
        sb.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color:#ffffff;border:1px solid #e5e5e5;border-left:4px solid #22c55e;border-radius:12px;margin-bottom:12px;\">");
        sb.append("<tr><td style=\"padding:16px 20px;\">");

        sb.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">");
        sb.append("<tr>");
        sb.append("<td style=\"font-size:16px;font-weight:700;color:#171717;font-family:'Plus Jakarta Sans','Inter','Segoe UI',Arial,Helvetica,sans-serif;\">")
                .append(escapeHtml(sub.getName()))
                .append("</td>");
        sb.append("<td align=\"right\" style=\"font-size:16px;font-weight:800;color:#16a34a;\">")
                .append(formatCurrency(sub.getValue(), sub.getCurrency()))
                .append("</td>");
        sb.append("</tr>");

        sb.append("<tr>");
        sb.append("<td style=\"font-size:13px;color:#525252;padding-top:8px;\">");
        if (!paymentMethodName.isEmpty()) {
            sb.append(escapeHtml(paymentMethodName));
        }
        if (!categoryName.isEmpty()) {
            if (!paymentMethodName.isEmpty()) sb.append(" · ");
            sb.append(escapeHtml(categoryName));
        }
        sb.append("</td>");
        sb.append("<td align=\"right\" style=\"font-size:12px;color:#6d28d9;padding-top:8px;font-weight:700;\">")
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
        sb.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin:20px 0 24px;background-color:#fafafa;border:1px solid #e5e5e5;border-radius:12px;\">");
        sb.append("<tr><td style=\"padding:16px 18px;\">");

        totalsByCurrency.forEach((currency, total) ->
                sb.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin-bottom:4px;\"><tr>")
                        .append("<td style=\"font-size:12px;font-weight:700;color:#525252;text-transform:uppercase;letter-spacing:0.3px;\">Total</td>")
                        .append("<td align=\"right\" style=\"font-size:22px;font-weight:800;color:#171717;font-family:'Plus Jakarta Sans','Inter','Segoe UI',Arial,Helvetica,sans-serif;\">")
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
               + "style=\"display:inline-block;background:linear-gradient(135deg,#22c55e 0%,#16a34a 100%);color:#ffffff;font-size:15px;font-weight:700;"
               + "text-decoration:none;padding:14px 32px;border-radius:12px;border:1px solid #16a34a;box-shadow:0 0 18px rgba(34,197,94,0.25);font-family:'Inter','Segoe UI',Arial,Helvetica,sans-serif;\">"
               + "Ver no Dashboard"
               + "</a>"
               + "</td></tr></table>";
    }

    private String footer() {
        return "<tr><td style=\"background-color:#fafafa;padding:24px 40px;text-align:center;border-top:1px solid #e5e5e5;\">"
               + "<p style=\"margin:0;font-size:13px;color:#737373;\">Ongoing — Gerencie suas assinaturas</p>"
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
