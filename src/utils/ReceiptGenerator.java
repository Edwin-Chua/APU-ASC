package utils;

import models.entities.Payment;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * ReceiptGenerator — No external library required.
 * Generates a styled HTML receipt and opens it in the default browser.
 * Staff can then use Ctrl+P → Save as PDF.
 */
public class ReceiptGenerator {

    /**
     * Generate an HTML receipt and open it in the default browser.
     *
     * @param payment         Saved Payment entity.
     * @param customerDisplay Customer name or ID.
     * @param vehiclePlate    Vehicle plate string.
     * @param appointmentDate ISO date string (yyyy-MM-dd).
     * @param serviceName     Human-readable service name.
     * @param outputDir       Directory to save the HTML file.
     * @return The generated HTML file.
     */
    public static File generate(
            Payment payment,
            String  customerDisplay,
            String  vehiclePlate,
            String  appointmentDate,
            String  serviceName,
            String  outputDir) throws IOException {

        // ── Output path ───────────────────────────────────────────────────────
        File dir = new File(outputDir);
        if (!dir.exists()) dir.mkdirs();

        String filename = "Receipt_" + payment.getReceiptNo()
                        + "_" + payment.getPaymentDate() + ".html";
        File outFile = new File(dir, filename);

        double amount     = payment.getAmountDouble();
        double additional = payment.getAdditionalChargeDouble();
        double total      = payment.getTotalAmountDouble();

        String html = buildHtml(
            payment.getReceiptNo(),
            payment.getPaymentId(),
            formatDate(payment.getPaymentDate()),
            payment.getAppointmentId(),
            customerDisplay,
            vehiclePlate,
            formatDate(appointmentDate),
            serviceName,
            payment.getStaffId(),
            payment.getPaymentMethod(),
            amount,
            additional,
            total
        );

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
            writer.write(html);
        }

        return outFile;
    }

    // ── HTML builder ──────────────────────────────────────────────────────────

    private static String buildHtml(
            String receiptNo, String paymentId, String paymentDate,
            String apptId, String customer, String plate,
            String apptDate, String service, String staffId,
            String method, double amount, double additional, double total) {

        return "<!DOCTYPE html>\n"
            + "<html lang=\"en\">\n"
            + "<head>\n"
            + "<meta charset=\"UTF-8\">\n"
            + "<title>Receipt " + receiptNo + "</title>\n"
            + "<style>\n"
            + buildCss()
            + "</style>\n"
            + "</head>\n"
            + "<body>\n"
            + "<div class=\"page\">\n"

            // Header
            + "  <div class=\"header\">\n"
            + "    <div class=\"company\">AutoServe</div>\n"
            + "    <div class=\"meta\">\n"
            + "      <div><span class=\"label\">Receipt No</span><strong>" + receiptNo + "</strong></div>\n"
            + "      <div><span class=\"label\">Payment ID</span>" + paymentId + "</div>\n"
            + "      <div><span class=\"label\">Date</span>" + paymentDate + "</div>\n"
            + "    </div>\n"
            + "  </div>\n"
            + "  <div class=\"divider\"></div>\n"

            // Appointment details
            + "  <h2 class=\"section-title\">Appointment Details</h2>\n"
            + "  <table class=\"info-table\">\n"
            + "    <tr>\n"
            + "      <td class=\"lbl\">Appointment ID</td><td>" + apptId + "</td>\n"
            + "      <td class=\"lbl\">Customer</td><td>" + customer + "</td>\n"
            + "    </tr>\n"
            + "    <tr class=\"alt\">\n"
            + "      <td class=\"lbl\">Vehicle Plate</td><td>" + plate + "</td>\n"
            + "      <td class=\"lbl\">Service</td><td>" + service + "</td>\n"
            + "    </tr>\n"
            + "    <tr>\n"
            + "      <td class=\"lbl\">Appt Date</td><td>" + apptDate + "</td>\n"
            + "      <td class=\"lbl\">Staff ID</td><td>" + staffId + "</td>\n"
            + "    </tr>\n"
            + "    <tr class=\"alt\">\n"
            + "      <td class=\"lbl\">Payment Method</td><td>" + method + "</td>\n"
            + "      <td class=\"lbl\">Status</td><td class=\"paid\">PAID</td>\n"
            + "    </tr>\n"
            + "  </table>\n"

            // Charges
            + "  <h2 class=\"section-title\" style=\"margin-top:24px\">Charges</h2>\n"
            + "  <table class=\"charge-table\">\n"
            + "    <thead><tr><th>Description</th><th>Amount (RM)</th></tr></thead>\n"
            + "    <tbody>\n"
            + "      <tr><td>" + service + " (Service Charge)</td><td class=\"amt\">" + String.format("%.2f", amount) + "</td></tr>\n"
            + "      <tr class=\"alt\"><td>Additional Charge</td><td class=\"amt\">" + String.format("%.2f", additional) + "</td></tr>\n"
            + "    </tbody>\n"
            + "  </table>\n"
            + "  <div class=\"total-row\">\n"
            + "    <span>TOTAL PAID</span>\n"
            + "    <span class=\"total-amt\">RM " + String.format("%.2f", total) + "</span>\n"
            + "  </div>\n"

            // Footer
            + "  <div class=\"footer-divider\"></div>\n"
            + "  <p class=\"footer\">Thank you for choosing AutoServe. Please keep this receipt for your records.</p>\n"
            + "  <p class=\"footer\">This is a computer-generated receipt and does not require a signature.</p>\n"
            + "  <p class=\"print-hint\">💡 Press <strong>Ctrl+P</strong> (or ⌘+P on Mac) to save as PDF.</p>\n"

            + "</div>\n"
            + "</body>\n"
            + "</html>\n";
    }

    // ── CSS ───────────────────────────────────────────────────────────────────

    private static String buildCss() {
        return ""
            + "  * { box-sizing: border-box; margin: 0; padding: 0; }\n"
            + "  body { font-family: -apple-system, Helvetica, Arial, sans-serif;\n"
            + "         background: #f0f0f5; display: flex; justify-content: center;\n"
            + "         padding: 40px 16px; }\n"
            + "  .page { background: #fff; width: 720px; padding: 48px 56px;\n"
            + "          border-radius: 12px; box-shadow: 0 4px 24px rgba(0,0,0,.1); }\n"

            + "  /* Header */\n"
            + "  .header { display: flex; justify-content: space-between; align-items: flex-start; }\n"
            + "  .company { font-size: 28px; font-weight: 700; color: #1C1C1E; }\n"
            + "  .meta { text-align: right; font-size: 12px; color: #6B7280; line-height: 1.9; }\n"
            + "  .meta strong { color: #1C1C1E; }\n"
            + "  .label { margin-right: 8px; }\n"
            + "  .divider { height: 2px; background: #4F46E5; margin: 20px 0; border-radius: 2px; }\n"
            + "  .footer-divider { height: 1px; background: #E5E7EB; margin: 28px 0 12px; }\n"

            + "  /* Section title */\n"
            + "  .section-title { font-size: 14px; font-weight: 700; color: #1C1C1E;\n"
            + "                   margin-bottom: 10px; }\n"

            + "  /* Info table */\n"
            + "  .info-table { width: 100%; border-collapse: collapse; font-size: 13px; }\n"
            + "  .info-table td { padding: 10px 12px; border: 1px solid #E5E7EB; }\n"
            + "  .info-table .lbl { font-weight: 600; color: #6B7280;\n"
            + "                     background: #F9FAFB; width: 18%; }\n"
            + "  .info-table tr.alt td { background: #F3F4F6; }\n"
            + "  .info-table tr.alt .lbl { background: #EAECEF; }\n"
            + "  .paid { color: #10B981; font-weight: 700; }\n"

            + "  /* Charge table */\n"
            + "  .charge-table { width: 100%; border-collapse: collapse; font-size: 13px; }\n"
            + "  .charge-table thead tr { background: #4F46E5; color: #fff; }\n"
            + "  .charge-table th { padding: 11px 14px; text-align: left; font-weight: 600; }\n"
            + "  .charge-table th:last-child { text-align: right; }\n"
            + "  .charge-table td { padding: 11px 14px; border-bottom: 1px solid #E5E7EB; }\n"
            + "  .charge-table tr.alt td { background: #F3F4F6; }\n"
            + "  .amt { text-align: right; }\n"

            + "  /* Total */\n"
            + "  .total-row { display: flex; justify-content: space-between; align-items: center;\n"
            + "               background: #EEF2FF; padding: 14px 14px;\n"
            + "               border-top: 2px solid #4F46E5; }\n"
            + "  .total-row span { font-weight: 700; font-size: 14px; color: #4F46E5; }\n"
            + "  .total-amt { font-size: 18px; }\n"

            + "  /* Footer */\n"
            + "  .footer { font-size: 11px; color: #9CA3AF; text-align: center; margin-bottom: 4px; }\n"
            + "  .print-hint { font-size: 12px; color: #6B7280; text-align: center;\n"
            + "                margin-top: 16px; padding: 10px;\n"
            + "                background: #FFFBEB; border-radius: 6px; }\n"

            + "  @media print {\n"
            + "    body { background: none; padding: 0; }\n"
            + "    .page { box-shadow: none; border-radius: 0; padding: 32px; }\n"
            + "    .print-hint { display: none; }\n"
            + "  }\n";
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String formatDate(String iso) {
        try {
            LocalDate d = LocalDate.parse(iso, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return d.format(DateTimeFormatter.ofPattern("d MMM yyyy"));
        } catch (Exception e) {
            return iso;
        }
    }
}