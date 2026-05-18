package models.dao;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import models.entities.Payment;

public class PaymentDAO {

    private static final String FILE_PATH = "src/txtFiles/payment.txt";

    // ─── Column index reference (pipe-delimited) ───────────────────────────────
    // 0  payment_id
    // 1  appointment_id
    // 2  amount
    // 3  payment_method
    // 4  payment_date
    // 5  receipt_no
    // 6  staff_id
    // 7  additional_charge
    // 8  total_amount

    // ─── Read all ──────────────────────────────────────────────────────────────
    public List<Payment> getAllPayments() {
        List<Payment> list = new ArrayList<>();
        Path path = Paths.get(FILE_PATH);
        if (!Files.exists(path)) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (first) { first = false; continue; } // skip header
                String[] p = line.split("\\|", -1);
                if (p.length < 9) continue;

                list.add(new Payment(
                    p[0].trim(),  // payment_id
                    p[1].trim(),  // appointment_id
                    p[2].trim(),  // amount
                    p[3].trim(),  // payment_method
                    p[4].trim(),  // payment_date
                    p[5].trim(),  // receipt_no
                    p[6].trim(),  // staff_id
                    p[7].trim(),  // additional_charge
                    p[8].trim()   // total_amount
                ));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ─── Get by appointment ID ────────────────────────────────────────────────
    public Payment getPaymentByAppointmentId(String appointmentId) {
        for (Payment p : getAllPayments()) {
            if (p.getAppointmentId().equals(appointmentId)) return p;
        }
        return null;
    }

    // ─── Get by payment ID ────────────────────────────────────────────────────
    public Payment getPaymentById(String paymentId) {
        for (Payment p : getAllPayments()) {
            if (p.getPaymentId().equals(paymentId)) return p;
        }
        return null;
    }

    // ─── Add new payment ───────────────────────────────────────────────────────
    public boolean addPayment(Payment payment) {
        try {
            List<String> lines = new ArrayList<>();
            Path path = Paths.get(FILE_PATH);

            if (Files.exists(path)) {
                lines = new ArrayList<>(Files.readAllLines(path));
            } else {
                lines.add("payment_id|appointment_id|amount|payment_method"
                    + "|payment_date|receipt_no|staff_id|additional_charge|total_amount");
            }

            String newLine = String.join("|",
                payment.getPaymentId(),
                payment.getAppointmentId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getPaymentDate(),
                payment.getReceiptNo(),
                payment.getStaffId(),
                payment.getAdditionalCharge(),
                payment.getTotalAmount()
            );
            lines.add(newLine);
            Files.write(path, lines);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ─── Generate next payment ID (P001, P002, …) ─────────────────────────────
    public String generateNextPaymentId() {
        int max = 0;
        for (Payment p : getAllPayments()) {
            String id = p.getPaymentId();
            if (id != null && id.startsWith("P")) {
                try {
                    int num = Integer.parseInt(id.substring(1));
                    if (num > max) max = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("P%03d", max + 1);
    }

    // ─── Generate next receipt number (RCP1001, RCP1002, …) ──────────────────
    public String generateNextReceiptNo() {
        int max = 1000;
        for (Payment p : getAllPayments()) {
            String rcp = p.getReceiptNo();
            if (rcp != null && rcp.startsWith("RCP")) {
                try {
                    int num = Integer.parseInt(rcp.substring(3));
                    if (num > max) max = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("RCP%04d", max + 1);
    }
}