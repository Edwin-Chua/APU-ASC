package views.staff;

import models.dao.AppointmentDAO;
import models.dao.PaymentDAO;
import models.dao.ServiceItemDAO;
import models.entities.Appointment;
import models.entities.Payment;
import views.MainFrame;
import views.shared.AppTheme;
import views.shared.UIComponent;

import utils.ReceiptGenerator;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * CounterStaffBilling — Staff billing page.
 *
 * Table shows ONLY Completed and Paid appointments loaded from AppointmentDAO.
 *   • Completed → staff can process payment; status updates to "Paid" after confirmation.
 *   • Paid      → read-only; payment details are loaded from PaymentDAO and shown.
 *
 * Schema written to payment.txt via PaymentDAO:
 *   payment_id | appointment_id | amount | payment_method |
 *   payment_date | receipt_no | staff_id | additional_charge | total_amount
 */
public class CounterStaffBilling extends JPanel {

    // ── DAO instances ─────────────────────────────────────────────────────────
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PaymentDAO     paymentDAO     = new PaymentDAO();

    // Table columns — single Status column (Completed → Paid after payment)
    private static final String[] APPT_COLUMNS =
        {"Appt ID", "Customer ID", "Vehicle Plate", "Service Item", "Date", "Status"};

    // ── Filter / table state ──────────────────────────────────────────────────
    private JComboBox<String>        filterStatus;
    private UIComponent.DatePickerField filterDateField; // ← fixed: was plain JTextField
    private JLabel                   countLabel;
    private DefaultTableModel        tableModel;
    private JTable                   apptTable;

    // Live data list (Completed + Paid appointments only)
    private List<Appointment> displayedAppointments;

    // ── UI references ─────────────────────────────────────────────────────────
    private JPanel formPanel;
    private JLabel formApptIdLabel;

    // form fields
    private JTextField amountField;
    private JTextField additionalChargeField;
    private JLabel     totalAmountLabel;
    private JComboBox<String> paymentMethodCombo;

    // selected appointment row data
    private String selectedApptId  = "";
    private String selectedStaffId = "CS001"; // replaced via setStaffId()

    // ── Constructor ───────────────────────────────────────────────────────────

    public CounterStaffBilling(MainFrame frame) {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_PAGE);

        JPanel content = buildContent();
        JScrollPane scroll = UIComponent.createAppleScrollPane(new JScrollPane(content));
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void setStaffId(String staffId) {
        this.selectedStaffId = staffId;
    }

    public void refresh() {
        applyFilter();
    }

    // ── Content builder ───────────────────────────────────────────────────────

    private JPanel buildContent() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(AppTheme.BG_PAGE);
        root.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_PAGE, AppTheme.PADDING_PAGE,
            AppTheme.PADDING_PAGE, AppTheme.PADDING_PAGE));

        // ── Header ────────────────────────────────────────────────────────────
        JLabel title = UIComponent.createPageTitle("Billing");
        JLabel sub   = UIComponent.createCaptionLabel(
            "Select an appointment to process payment");
        sub.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        root.add(title);
        root.add(sub);
        root.add(UIComponent.verticalGap(AppTheme.GAP_SECTION));

        // ── Appointment table ─────────────────────────────────────────────────
        root.add(buildApptSection());
        root.add(UIComponent.verticalGap(AppTheme.GAP_SECTION));

        // ── Payment form (hidden until row selected) ──────────────────────────
        formPanel = buildPaymentForm();
        formPanel.setVisible(false);
        root.add(formPanel);

        return root;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Appointment Table
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildApptSection() {
        JPanel card = UIComponent.createCard();
        card.setLayout(new BorderLayout(0, 0));
        card.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Toolbar ───────────────────────────────────────────────────────────
        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_CARD, AppTheme.PADDING_CARD, 12, AppTheme.PADDING_CARD));

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterRow.setOpaque(false);

        JLabel lblFilter = new JLabel("Filter:");
        lblFilter.setFont(AppTheme.FONT_SMALL);
        lblFilter.setForeground(AppTheme.TEXT_SECONDARY);

        filterStatus = UIComponent.createComboBox(new String[]{"All", "Completed", "Paid"});
        filterStatus.setPreferredSize(new Dimension(142, 38));

        JLabel lblDate = new JLabel("Date:");
        lblDate.setFont(AppTheme.FONT_SMALL);
        lblDate.setForeground(AppTheme.TEXT_SECONDARY);

        // ← Fixed: DatePickerField instead of plain JTextField
        filterDateField = UIComponent.createDatePicker(null);
        filterDateField.setPreferredSize(new Dimension(120, 38));

        JButton btnFilter = UIComponent.createSmallButton("Filter");
        btnFilter.setPreferredSize(new Dimension(72, 34));
        btnFilter.addActionListener(e -> applyFilter());

        JButton btnRefresh = UIComponent.createSmallButton("↺ Refresh");
        btnRefresh.setPreferredSize(new Dimension(95, 34));
        btnRefresh.addActionListener(e -> {
            filterStatus.setSelectedIndex(0);
            filterDateField.setDate(null); // ← fixed: was setText("")
            applyFilter();
        });

        filterRow.add(lblFilter);
        filterRow.add(filterStatus);
        filterRow.add(lblDate);
        filterRow.add(filterDateField);
        filterRow.add(btnFilter);
        filterRow.add(btnRefresh);

        // Count label — populated by applyFilter()
        countLabel = new JLabel("Loading…");
        countLabel.setFont(AppTheme.FONT_SMALL);
        countLabel.setForeground(AppTheme.TEXT_SECONDARY);

        toolbar.add(filterRow,  BorderLayout.WEST);
        toolbar.add(countLabel, BorderLayout.EAST);

        // ── Table ─────────────────────────────────────────────────────────────
        tableModel = new DefaultTableModel(APPT_COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        apptTable = new JTable(tableModel);
        apptTable.setRowHeight(AppTheme.ROW_HEIGHT_TABLE);
        apptTable.setFont(AppTheme.FONT_SMALL);
        apptTable.setForeground(AppTheme.TEXT_PRIMARY);
        apptTable.setBackground(AppTheme.BG_CARD);
        apptTable.setGridColor(AppTheme.SEPARATOR);
        apptTable.setShowVerticalLines(false);
        apptTable.setShowHorizontalLines(true);
        apptTable.setSelectionBackground(AppTheme.PRIMARY);
        apptTable.setSelectionForeground(Color.WHITE);
        apptTable.setFillsViewportHeight(true);
        apptTable.setIntercellSpacing(new Dimension(0, 0));
        apptTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Header
        JTableHeader header = apptTable.getTableHeader();
        header.setFont(AppTheme.FONT_LABEL);
        header.setBackground(AppTheme.BG_PAGE);
        header.setForeground(AppTheme.TEXT_SECONDARY);
        header.setPreferredSize(new Dimension(0, 40));
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER_DEFAULT));

        DefaultTableCellRenderer hdrRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                setFont(AppTheme.FONT_LABEL);
                setForeground(AppTheme.TEXT_SECONDARY);
                setBackground(AppTheme.BG_PAGE);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER_DEFAULT),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)));
                return this;
            }
        };
        for (int i = 0; i < APPT_COLUMNS.length; i++)
            apptTable.getColumnModel().getColumn(i).setHeaderRenderer(hdrRenderer);

        // Column widths
        int[] widths = {80, 100, 120, 100, 110, 110};
        for (int i = 0; i < widths.length; i++)
            apptTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Row renderer — alternating rows + status colour
        apptTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                setFont(AppTheme.FONT_SMALL);
                if (sel) {
                    setBackground(AppTheme.PRIMARY);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(row % 2 == 0 ? AppTheme.BG_CARD : new Color(248, 248, 250));
                    if (col == 5) {
                        String status = tableModel.getValueAt(row, 5).toString();
                        switch (status.toLowerCase()) {
                            case "completed": setForeground(AppTheme.STATUS_INPROGRESS_FG); break;
                            case "paid":      setForeground(AppTheme.STATUS_COMPLETED_FG);  break;
                            default:          setForeground(AppTheme.TEXT_PRIMARY);
                        }
                    } else {
                        setForeground(AppTheme.TEXT_PRIMARY);
                    }
                }
                return this;
            }
        });

        // Row click → open payment form (Completed) or show info (Paid)
        apptTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = apptTable.getSelectedRow();
                if (row >= 0) {
                    String status = tableModel.getValueAt(row, 5).toString();
                    if ("Paid".equalsIgnoreCase(status)) {
                        formPanel.setVisible(false);
                        String apptId = tableModel.getValueAt(row, 0).toString();
                        Payment existingPayment = paymentDAO.getPaymentByAppointmentId(apptId);

                        String[][] infoRows = existingPayment == null ? null : new String[][]{
                            {"Receipt No",  existingPayment.getReceiptNo()},
                            {"Payment ID",  existingPayment.getPaymentId()},
                            {"Total Paid",  String.format("RM %.2f", existingPayment.getTotalAmountDouble())},
                            {"Method",      existingPayment.getPaymentMethod()},
                            {"Date",        existingPayment.getPaymentDate()},
                        };

                        UIComponent.DialogAction[] actions = existingPayment == null
                            ? new UIComponent.DialogAction[]{
                                new UIComponent.DialogAction("Close", UIComponent.DialogActionStyle.OUTLINE, null)
                              }
                            : new UIComponent.DialogAction[]{
                                new UIComponent.DialogAction("Close", UIComponent.DialogActionStyle.OUTLINE, null),
                                new UIComponent.DialogAction("Generate Receipt", UIComponent.DialogActionStyle.PRIMARY,
                                    () -> generateReceipt(existingPayment))
                              };

                        UIComponent.showDialog(
                            CounterStaffBilling.this,
                            "Payment Details",
                            UIComponent.DialogType.INFO,
                            "Appointment " + apptId + " has already been paid.",
                            infoRows,
                            actions);

                        apptTable.clearSelection();
                    } else {
                        openPaymentForm(row);
                    }
                }
            }
        });

        // Status bar
        JLabel statusBar = new JLabel(" Click a row to process payment");
        statusBar.setFont(AppTheme.FONT_CAPTION);
        statusBar.setForeground(AppTheme.TEXT_MUTED);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.SEPARATOR),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        statusBar.setBackground(AppTheme.BG_PAGE);
        statusBar.setOpaque(true);

        apptTable.getSelectionModel().addListSelectionListener(e2 -> {
            if (!e2.getValueIsAdjusting()) {
                int r = apptTable.getSelectedRow();
                if (r >= 0) {
                    String status = tableModel.getValueAt(r, 5).toString();
                    statusBar.setText(" Selected: Appointment " + tableModel.getValueAt(r, 0)
                        + " — " + tableModel.getValueAt(r, 1)
                        + "  |  Status: " + status);
                } else {
                    statusBar.setText(" Click a row to process payment");
                }
            }
        });

        JScrollPane scroll = UIComponent.createAppleScrollPane(apptTable);
        scroll.getViewport().setBackground(AppTheme.BG_CARD);
        scroll.setPreferredSize(new Dimension(0, 260));

        card.add(toolbar,   BorderLayout.NORTH);
        card.add(scroll,    BorderLayout.CENTER);
        card.add(statusBar, BorderLayout.SOUTH);

        // Initial load
        applyFilter();

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Filter logic
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Loads Completed + Paid appointments from AppointmentDAO,
     * applies status and date filters, and repopulates the table.
     */
    private void applyFilter() {
        tableModel.setRowCount(0);

        String statusFilter = (String) filterStatus.getSelectedItem();
        String dateFilter   = filterDateField.getText().trim();

        // ── Normalise date string to yyyy-MM-dd ───────────────────────────────
        String isoDate = "";
        if (!dateFilter.isEmpty()) {
            try {
                LocalDate.parse(dateFilter, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                isoDate = dateFilter; // already correct
            } catch (Exception e1) {
                try {
                    isoDate = LocalDate.parse(dateFilter,
                        DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } catch (Exception e2) {
                    try {
                        isoDate = LocalDate.parse(dateFilter,
                            DateTimeFormatter.ofPattern("MM/dd/yyyy"))
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    } catch (Exception ignored) {}
                }
            }
        }

        // ── Load from DAO ─────────────────────────────────────────────────────
        List<Appointment> completed = appointmentDAO.getAppointmentsByStatus("Completed");
        List<Appointment> paid      = appointmentDAO.getAppointmentsByStatus("Paid");

        displayedAppointments = new java.util.ArrayList<>();
        displayedAppointments.addAll(completed);
        displayedAppointments.addAll(paid);

        int count = 0, completedCount = 0;

        for (Appointment a : displayedAppointments) {
            String status = a.getStatus();
            if ("Completed".equalsIgnoreCase(status)) completedCount++;

            boolean matchStatus = "All".equals(statusFilter) || status.equalsIgnoreCase(statusFilter);
            boolean matchDate   = isoDate.isEmpty() || a.getAppointmentDate().equals(isoDate);

            if (matchStatus && matchDate) {
                tableModel.addRow(new Object[]{
                    a.getAppointmentId(),
                    a.getCustomerId(),
                    a.getVehiclePlate(),
                    a.getServiceItemId(),
                    a.getAppointmentDate(),
                    a.getStatus()
                });
                count++;
            }
        }

        countLabel.setText("Showing " + count + " records  |  " + completedCount + " Awaiting Payment");
        if (formPanel != null) formPanel.setVisible(false);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Payment Form
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildPaymentForm() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Section header row (title + close button)
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        formApptIdLabel = UIComponent.createSectionLabel("Process Payment");
        headerRow.add(formApptIdLabel, BorderLayout.WEST);

        JButton closeBtn = UIComponent.createLinkButton("✕ Close");
        closeBtn.addActionListener(e -> formPanel.setVisible(false));
        headerRow.add(closeBtn, BorderLayout.EAST);

        section.add(headerRow);

        // ── Two-column card layout ─────────────────────────────────────────────
        JPanel twoCol = new JPanel(new GridBagLayout());
        twoCol.setOpaque(false);
        twoCol.setAlignmentX(Component.LEFT_ALIGNMENT);
        twoCol.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets  = new Insets(0, 0, 0, 0);
        gbc.gridx   = 0; gbc.gridy = 0;

        twoCol.add(buildSummaryCard(), gbc);

        gbc.insets = new Insets(0, AppTheme.GAP_ITEM, 0, 0);
        gbc.gridx  = 1;
        twoCol.add(buildPaymentInputCard(), gbc);

        section.add(twoCol);
        return section;
    }

    // ── Left card: appointment summary ────────────────────────────────────────

    private JPanel summaryApptId, summaryCustomer, summaryPlate,
                   summaryService, summaryDate, summaryApptStatus;

    private JPanel buildSummaryCard() {
        JPanel card = UIComponent.createCard();
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(UIComponent.createSectionLabel("Appointment Summary"));

        summaryApptId     = UIComponent.createInfoRow("Appointment ID", "—");
        summaryCustomer   = UIComponent.createInfoRow("Customer ID",    "—");
        summaryPlate      = UIComponent.createInfoRow("Vehicle Plate",  "—");
        summaryService    = UIComponent.createInfoRow("Service Item",   "—");
        summaryDate       = UIComponent.createInfoRow("Date",           "—");
        summaryApptStatus = UIComponent.createInfoRow("Status",         "—");

        card.add(summaryApptId);
        card.add(UIComponent.createRowDivider());
        card.add(summaryCustomer);
        card.add(UIComponent.createRowDivider());
        card.add(summaryPlate);
        card.add(UIComponent.createRowDivider());
        card.add(summaryService);
        card.add(UIComponent.createRowDivider());
        card.add(summaryDate);
        card.add(UIComponent.createRowDivider());
        card.add(summaryApptStatus);

        return card;
    }

    // ── Right card: payment inputs ────────────────────────────────────────────

    private JPanel buildPaymentInputCard() {
        JPanel card = UIComponent.createCard();
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(UIComponent.createSectionLabel("Payment Details"));

        // Amount — pre-filled from service item price (read-only)
        card.add(UIComponent.createFieldLabel("AMOUNT (RM)"));
        card.add(UIComponent.verticalGap(4));
        amountField = UIComponent.createTextField("—");
        amountField.setEditable(false);
        amountField.setBackground(new Color(245, 245, 247));
        amountField.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(amountField);
        card.add(UIComponent.verticalGap(AppTheme.GAP_ITEM));

        // Additional charge
        card.add(UIComponent.createFieldLabel("ADDITIONAL CHARGE (RM)"));
        card.add(UIComponent.verticalGap(4));
        additionalChargeField = UIComponent.createTextField("e.g. 0.00");
        additionalChargeField.setAlignmentX(Component.LEFT_ALIGNMENT);
        additionalChargeField.setText("0.00");
        card.add(additionalChargeField);
        card.add(UIComponent.verticalGap(AppTheme.GAP_ITEM));

        // Auto total
        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        totalRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        totalRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JLabel totalLbl = new JLabel("TOTAL (RM)");
        totalLbl.setFont(AppTheme.FONT_LABEL);
        totalLbl.setForeground(AppTheme.TEXT_SECONDARY);
        totalAmountLabel = new JLabel("0.00");
        totalAmountLabel.setFont(AppTheme.FONT_HEADING_3);
        totalAmountLabel.setForeground(AppTheme.PRIMARY);
        totalAmountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        totalRow.add(totalLbl,         BorderLayout.WEST);
        totalRow.add(totalAmountLabel, BorderLayout.EAST);
        card.add(totalRow);
        card.add(UIComponent.verticalGap(AppTheme.GAP_ITEM));

        // Live total recalculation
        KeyAdapter totalUpdater = new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { recalcTotal(); }
        };
        amountField.addKeyListener(totalUpdater);
        additionalChargeField.addKeyListener(totalUpdater);

        // Payment method
        card.add(UIComponent.createFieldLabel("PAYMENT METHOD"));
        card.add(UIComponent.verticalGap(4));
        paymentMethodCombo = UIComponent.createComboBox(new String[]{
            "Cash", "Online Banking / FPX", "Debit / Credit Card", "E-Wallet"});
        paymentMethodCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(paymentMethodCombo);
        card.add(UIComponent.verticalGap(AppTheme.GAP_SECTION));

        // Confirm + Cancel buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, AppTheme.GAP_ITEM, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton confirmBtn = UIComponent.createPrimaryButton("Confirm Payment");
        confirmBtn.setPreferredSize(AppTheme.BUTTON_SM);
        confirmBtn.setMaximumSize(AppTheme.BUTTON_SM);
        confirmBtn.addActionListener(e -> handleConfirmPayment());

        JButton cancelBtn = UIComponent.createOutlineButton("Cancel");
        cancelBtn.setPreferredSize(AppTheme.BUTTON_SM);
        cancelBtn.setMaximumSize(AppTheme.BUTTON_SM);
        cancelBtn.addActionListener(e -> formPanel.setVisible(false));

        btnRow.add(confirmBtn);
        btnRow.add(cancelBtn);
        card.add(btnRow);

        return card;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Logic helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void recalcTotal() {
        double amt   = parseDouble(amountField.getText());
        double extra = parseDouble(additionalChargeField.getText());
        totalAmountLabel.setText(String.format("%.2f", amt + extra));
    }

    private double parseDouble(String s) {
        try { return Double.parseDouble(s.trim()); }
        catch (NumberFormatException e) { return 0.0; }
    }

    private void handleConfirmPayment() {
        String amtStr   = amountField.getText().trim();
        String extraStr = additionalChargeField.getText().trim();

        if (amtStr.isEmpty() || parseDouble(amtStr) <= 0) {
            showError("Please enter a valid amount.");
            return;
        }

        String method = (String) paymentMethodCombo.getSelectedItem();
        double amount = parseDouble(amtStr);
        double extra  = parseDouble(extraStr.isEmpty() ? "0.00" : extraStr);
        double total  = amount + extra;
        String date   = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        String paymentId = paymentDAO.generateNextPaymentId();
        String receiptNo = paymentDAO.generateNextReceiptNo();

        Payment payment = new Payment(
            paymentId, selectedApptId,
            String.format("%.2f", amount), method, date, receiptNo,
            selectedStaffId,
            String.format("%.2f", extra),
            String.format("%.2f", total)
        );

        boolean paymentSaved = paymentDAO.addPayment(payment);
        if (!paymentSaved) {
            showError("Failed to save payment record. Please try again.");
            return;
        }

        boolean statusUpdated = appointmentDAO.markAppointmentAsPaid(selectedApptId);
        if (!statusUpdated) {
            showError("Payment saved but appointment status could not be updated.");
        }

        // Success dialog with "Generate Receipt" option
        JPanel msg = new JPanel();
        msg.setLayout(new BoxLayout(msg, BoxLayout.Y_AXIS));
        msg.setOpaque(false);

        JLabel l1 = new JLabel("Payment recorded successfully!");
        l1.setFont(AppTheme.FONT_BODY_BOLD);
        l1.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel l2 = new JLabel("Receipt No: " + receiptNo);
        l2.setFont(AppTheme.FONT_SMALL);
        l2.setForeground(AppTheme.TEXT_SECONDARY);

        JLabel l3 = new JLabel("Payment ID: " + paymentId);
        l3.setFont(AppTheme.FONT_SMALL);
        l3.setForeground(AppTheme.TEXT_SECONDARY);

        JLabel l4 = new JLabel(String.format("Total Paid: RM %.2f", total));
        l4.setFont(AppTheme.FONT_SMALL_BOLD);
        l4.setForeground(AppTheme.SUCCESS);

        msg.add(l1);
        msg.add(UIComponent.verticalGap(6));
        msg.add(l2);
        msg.add(l3);
        msg.add(UIComponent.verticalGap(6));
        msg.add(l4);

        String[] options = {"Generate Receipt (PDF)", "Close"};
        int choice = JOptionPane.showOptionDialog(
                this, msg, "Payment Confirmed",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null, options, options[0]);

        if (choice == 0) {
            generateReceipt(payment);
        }

        applyFilter();

        formPanel.setVisible(false);
        amountField.setText("");
        additionalChargeField.setText("0.00");
        totalAmountLabel.setText("0.00");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Generate PDF Receipt
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Generates a PDF receipt for the given {@link Payment} and opens the
     * containing folder in the system file manager.
     *
     * The receipt is saved to:  receipts/Receipt_<receiptNo>_<date>.pdf
     * relative to the application working directory.
     *
     * @param payment The fully-populated Payment whose receipt is to be printed.
     */
    private void generateReceipt(Payment payment) {
        // Resolve appointment data from the currently displayed list
        Appointment appt = null;
        for (Appointment a : displayedAppointments) {
            if (a.getAppointmentId().equals(payment.getAppointmentId())) {
                appt = a;
                break;
            }
        }

        // Fallback values when the appointment is no longer in displayedAppointments
        String vehiclePlate    = appt != null ? appt.getVehiclePlate()    : payment.getAppointmentId();
        String appointmentDate = appt != null ? appt.getAppointmentDate() : payment.getPaymentDate();
        String serviceItemId   = appt != null ? appt.getServiceItemId()   : "—";
        String serviceName     = models.dao.ServiceItemDAO.getServiceName(serviceItemId);

        // customerId doubles as customer display name (replace with customer name lookup if available)
        String customerDisplay = appt != null ? appt.getCustomerId() : "—";

        String outputDir = "receipts";

        try {
            java.io.File pdf = ReceiptGenerator.generate(
                    payment,
                    customerDisplay,
                    vehiclePlate,
                    appointmentDate,
                    serviceName,
                    outputDir);

            // Notify staff and offer to open the file
            int open = JOptionPane.showConfirmDialog(
                    this,
                    "Receipt saved to:\n" + pdf.getAbsolutePath() + "\n\nOpen the file now?",
                    "Receipt Generated",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);

            if (open == JOptionPane.YES_OPTION) {
                try {
                    java.awt.Desktop.getDesktop().open(pdf);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            "Could not open the file automatically.\nPlease navigate to:\n" + pdf.getAbsolutePath(),
                            "Open File", JOptionPane.WARNING_MESSAGE);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to generate receipt PDF:\n" + ex.getMessage(),
                    "Receipt Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error",
            JOptionPane.WARNING_MESSAGE);
    }

    private void openPaymentForm(int row) {
        selectedApptId = tableModel.getValueAt(row, 0).toString();
        formApptIdLabel.setText("Process Payment — " + selectedApptId);

        updateInfoRow(summaryApptId,     tableModel.getValueAt(row, 0).toString());
        updateInfoRow(summaryCustomer,   tableModel.getValueAt(row, 1).toString());
        updateInfoRow(summaryPlate,      tableModel.getValueAt(row, 2).toString());
        updateInfoRow(summaryDate,       tableModel.getValueAt(row, 4).toString());
        updateInfoRow(summaryApptStatus, tableModel.getValueAt(row, 5).toString());

        String serviceItemId = tableModel.getValueAt(row, 3).toString();
        String serviceName   = ServiceItemDAO.getServiceName(serviceItemId);
        String serviceDisplay = serviceItemId.equals(serviceName)
            ? serviceItemId
            : serviceItemId + " - " + serviceName;
        updateInfoRow(summaryService, serviceDisplay);

        String priceStr = "0.00";
        for (String[] item : ServiceItemDAO.getAllServiceItems()) {
            if (item[0].equals(serviceItemId)) { priceStr = item[3]; break; }
        }
        amountField.setText(priceStr);
        amountField.setEditable(false);
        amountField.setBackground(new Color(245, 245, 247));
        additionalChargeField.setText("0.00");
        recalcTotal();
        paymentMethodCombo.setSelectedIndex(0);

        formPanel.setVisible(true);

        SwingUtilities.invokeLater(() -> {
            JScrollPane sp = (JScrollPane) SwingUtilities.getAncestorOfClass(
                JScrollPane.class, formPanel);
            if (sp != null) {
                JScrollBar vsb = sp.getVerticalScrollBar();
                vsb.setValue(vsb.getMaximum());
            }
        });
    }

    /** Updates the value label of an info row panel (BorderLayout CENTER). */
    private void updateInfoRow(JPanel row, String value) {
        if (row == null) return;
        Component center = ((BorderLayout) row.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        if (center instanceof JLabel) ((JLabel) center).setText(value);
    }
}