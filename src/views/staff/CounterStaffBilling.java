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
import javax.swing.plaf.LayerUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CounterStaffBilling extends JPanel {

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PaymentDAO     paymentDAO     = new PaymentDAO();

    private static final String[] APPT_COLUMNS =
        {"Appt ID", "Customer ID", "Vehicle Plate", "Service Item", "Date", "Status"};

    private JTextField                  searchField;
    private JComboBox<String>           filterStatus;
    private UIComponent.DatePickerField filterDateField;
    private JLabel                      countLabel;
    private DefaultTableModel           tableModel;
    private JTable                      apptTable;

    private List<Appointment> displayedAppointments;

    private JScrollPane tableScrollPane;   // inner scroll pane for the appt table

    private JPanel formPanel;
    private JLabel formApptIdLabel;

    private JTextField        amountField;
    private JTextField        additionalChargeField;
    private JLabel            totalAmountLabel;
    private JComboBox<String> paymentMethodCombo;

    private String selectedApptId  = "";
    private String selectedStaffId = "CS001";

    public CounterStaffBilling(MainFrame frame) {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_PAGE);
        add(buildContent(), BorderLayout.CENTER);
    }

    public void setStaffId(String staffId) { this.selectedStaffId = staffId; }
    private Runnable onDataChanged;    
    public void setOnDataChanged(Runnable callback) {
        this.onDataChanged = callback;
    }
    public void refresh()                  { applyFilter(); }

    // ─── Content ──────────────────────────────────────────────────────────────

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(AppTheme.BG_PAGE);
        setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_PAGE, AppTheme.PADDING_PAGE,
            AppTheme.PADDING_PAGE, AppTheme.PADDING_PAGE));

        // ── Header ────────────────────────────────────────────────────────────
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, AppTheme.GAP_SECTION, 0));

        JLabel title = UIComponent.createPageTitle("Billing");
        JLabel sub   = UIComponent.createCaptionLabel("Select an appointment to process payment");
        sub.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        header.add(title);
        header.add(sub);

        // ── Centre: table card + form panel stacked ───────────────────────────
        JPanel centre = new JPanel(new BorderLayout(0, 16));
        centre.setOpaque(false);
        centre.add(buildApptSection(), BorderLayout.CENTER);

        formPanel = buildPaymentForm();
        formPanel.setVisible(false);
        centre.add(formPanel, BorderLayout.SOUTH);

        root.add(header, BorderLayout.NORTH);
        root.add(centre, BorderLayout.CENTER);
        return root;
    }

    // ─── Appointment section ──────────────────────────────────────────────────

    private JPanel buildApptSection() {

        // ── Toolbar ───────────────────────────────────────────────────────────
        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.setOpaque(false);

        final String PLACEHOLDER = "Search by ID, Customer or Plate";
        searchField = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 50));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
                g2.dispose();
            }
        };
        searchField.putClientProperty("caretWidth", 2);
        searchField.setFont(AppTheme.FONT_SMALL);
        searchField.setForeground(AppTheme.TEXT_MUTED);
        searchField.setBackground(AppTheme.BG_CARD);
        searchField.setOpaque(false);
        searchField.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        searchField.setPreferredSize(new Dimension(240, 38));
        searchField.setText(PLACEHOLDER);
        searchField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (searchField.getText().equals(PLACEHOLDER)) {
                    searchField.setText("");
                    searchField.setForeground(AppTheme.TEXT_PRIMARY);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (searchField.getText().trim().isEmpty()) {
                    searchField.setText(PLACEHOLDER);
                    searchField.setForeground(AppTheme.TEXT_MUTED);
                }
            }
        });
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterRow.setOpaque(false);

        filterStatus = UIComponent.createComboBox(new String[]{"All Status", "Completed", "Paid"});
        filterStatus.setPreferredSize(new Dimension(142, 38));
        filterStatus.addActionListener(e -> applyFilter());

        filterDateField = UIComponent.createDatePicker(null);
        filterDateField.setPreferredSize(new Dimension(120, 38));
        filterDateField.addDateChangeListener(d -> applyFilter());

        JButton btnRefresh = UIComponent.createSmallButton("↺ Refresh");
        btnRefresh.setPreferredSize(new Dimension(95, 34));
        btnRefresh.addActionListener(e -> {
            searchField.setText(PLACEHOLDER);
            searchField.setForeground(AppTheme.TEXT_MUTED);
            filterStatus.setSelectedIndex(0);
            filterDateField.setDate(null);
            applyFilter();
        });

        filterRow.add(searchField);
        filterRow.add(filterStatus);
        filterRow.add(filterDateField);
        filterRow.add(btnRefresh);

        countLabel = new JLabel("Loading…");
        countLabel.setFont(AppTheme.FONT_SMALL);
        countLabel.setForeground(AppTheme.TEXT_SECONDARY);

        toolbar.add(filterRow,  BorderLayout.WEST);
        toolbar.add(countLabel, BorderLayout.EAST);

        // ── Table — consistent with ManageEmployeesPage ───────────────────────
        tableModel = new DefaultTableModel(APPT_COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        apptTable = new JTable(tableModel) {
            @Override public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(AppTheme.SEPARATOR);
                for (int i = 0; i < getRowCount() - 1; i++)
                    g2.drawLine(0, (i + 1) * getRowHeight() - 1, getWidth(), (i + 1) * getRowHeight() - 1);
                g2.dispose();
            }
            @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (!isRowSelected(row) && col != 5) {
                    c.setBackground(AppTheme.BG_CARD);
                    c.setForeground(col == 0 ? AppTheme.TEXT_SECONDARY : AppTheme.TEXT_PRIMARY);
                }
                return c;
            }
        };

        apptTable.setRowHeight(52);
        apptTable.setFont(AppTheme.FONT_SMALL);
        apptTable.setForeground(AppTheme.TEXT_PRIMARY);
        apptTable.setBackground(AppTheme.BG_CARD);
        apptTable.setShowGrid(false);
        apptTable.setIntercellSpacing(new Dimension(0, 0));
        apptTable.setFillsViewportHeight(true);
        apptTable.setSelectionBackground(AppTheme.PRIMARY_LIGHT);
        apptTable.setSelectionForeground(AppTheme.TEXT_PRIMARY);
        apptTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // ── Header ────────────────────────────────────────────────────────────
        apptTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                JLabel lbl = new JLabel(val == null ? "" : val.toString());
                lbl.setFont(AppTheme.FONT_LABEL);
                lbl.setForeground(AppTheme.TEXT_SECONDARY);
                lbl.setBackground(AppTheme.BG_PAGE);
                lbl.setOpaque(true);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.SEPARATOR),
                    BorderFactory.createEmptyBorder(0, 14, 0, 8)));
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                return lbl;
            }
        });
        apptTable.getTableHeader().setPreferredSize(new Dimension(0, 38));
        apptTable.getTableHeader().setReorderingAllowed(false);
        apptTable.getTableHeader().setResizingAllowed(false);

        // ── Column widths ─────────────────────────────────────────────────────
        int[] widths = {90, 110, 120, 150, 110, 120};
        for (int i = 0; i < widths.length; i++)
            apptTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // ── Text cell renderer ────────────────────────────────────────────────
        DefaultTableCellRenderer paddedRenderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 8));
                if (!sel) {
                    setBackground(AppTheme.BG_CARD);
                    setForeground(col == 0 ? AppTheme.TEXT_SECONDARY : AppTheme.TEXT_PRIMARY);
                }
                setFont(col == 0 ? AppTheme.FONT_LABEL : AppTheme.FONT_SMALL);
                return this;
            }
        };
        for (int i = 0; i < 5; i++)
            apptTable.getColumnModel().getColumn(i).setCellRenderer(paddedRenderer);

        // ── Status pill badge (col 5) — same pattern as ManageEmployeesPage ───
        apptTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                String status = val == null ? "" : val.toString();
                Color pillBg, pillFg;
                switch (status.toLowerCase()) {
                    case "completed":
                        pillBg = AppTheme.STATUS_INPROGRESS_BG;
                        pillFg = AppTheme.STATUS_INPROGRESS_FG; break;
                    case "paid":
                        pillBg = AppTheme.STATUS_COMPLETED_BG;
                        pillFg = AppTheme.STATUS_COMPLETED_FG;  break;
                    default:
                        pillBg = AppTheme.BG_PAGE;
                        pillFg = AppTheme.TEXT_MUTED;
                }
                JPanel cell = new JPanel(new GridBagLayout());
                cell.setOpaque(true);
                cell.setBackground(sel ? AppTheme.PRIMARY_LIGHT : AppTheme.BG_CARD);
                final Color bg = pillBg;
                JLabel pill = new JLabel(status) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(bg);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                pill.setOpaque(false);
                pill.setFont(AppTheme.FONT_LABEL);
                pill.setForeground(pillFg);
                pill.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0;
                gbc.anchor = GridBagConstraints.WEST;
                gbc.insets = new Insets(0, 14, 0, 8);
                cell.add(pill, gbc);
                return cell;
            }
        });

        // ── Row click: open form (Completed) or show info (Paid) ──────────────
        apptTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = apptTable.getSelectedRow();
                if (row >= 0) {
                    String status = tableModel.getValueAt(row, 5).toString();
                    if ("Paid".equalsIgnoreCase(status)) {
                        setTableCompact(false);
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
                                new UIComponent.DialogAction("Close", UIComponent.DialogActionStyle.OUTLINE, null)}
                            : new UIComponent.DialogAction[]{
                                new UIComponent.DialogAction("Close", UIComponent.DialogActionStyle.OUTLINE, null),
                                new UIComponent.DialogAction("Get Receipt", UIComponent.DialogActionStyle.PRIMARY,
                                    () -> generateReceipt(existingPayment))};
                        UIComponent.showDialog(CounterStaffBilling.this, "Payment Details",
                            UIComponent.DialogType.INFO,
                            "Appointment " + apptId + " has already been paid.",
                            infoRows, actions);
                        apptTable.clearSelection();
                    } else {
                        openPaymentForm(row);
                    }
                }
            }
        });

        // ── Status bar ────────────────────────────────────────────────────────
        JLabel statusBar = new JLabel(" Click a row to process payment");
        statusBar.setFont(AppTheme.FONT_CAPTION);
        statusBar.setForeground(AppTheme.TEXT_MUTED);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.SEPARATOR),
            BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        statusBar.setBackground(AppTheme.BG_PAGE);
        statusBar.setOpaque(true);

        apptTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int r = apptTable.getSelectedRow();
                statusBar.setText(r >= 0
                    ? " Selected: " + tableModel.getValueAt(r, 0)
                        + "  ·  " + tableModel.getValueAt(r, 1)
                        + "  ·  Status: " + tableModel.getValueAt(r, 5)
                    : " Click a row to process payment");
            }
        });

        // ── JLayer rounded card: header + scroll + status bar ─────────────────
        JScrollPane scroll = new JScrollPane(apptTable);
        tableScrollPane = scroll;
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(true);
        scroll.setBackground(AppTheme.BG_CARD);
        scroll.getViewport().setOpaque(true);
        scroll.getViewport().setBackground(AppTheme.BG_CARD);
        scroll.getViewport().setBorder(null);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(14);

        JTableHeader tblHeader = apptTable.getTableHeader();
        tblHeader.setOpaque(true);
        tblHeader.setBackground(AppTheme.BG_PAGE);
        scroll.setColumnHeader(null);

        JPanel inner = new JPanel(new BorderLayout());
        inner.setOpaque(true);
        inner.setBackground(AppTheme.BG_CARD);
        inner.add(tblHeader, BorderLayout.NORTH);
        inner.add(scroll,    BorderLayout.CENTER);
        inner.add(statusBar, BorderLayout.SOUTH);

        final int rad = AppTheme.RADIUS_CARD * 2;
        LayerUI<JPanel> layerUI = new LayerUI<JPanel>() {
            @Override public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.BG_CARD);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), rad, rad);
                g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, c.getWidth(), c.getHeight(), rad, rad));
                super.paint(g2, c);
                g2.setClip(null);
                g2.setColor(AppTheme.SEPARATOR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, c.getWidth()-1, c.getHeight()-1, rad, rad);
                g2.dispose();
            }
        };
        JLayer<JPanel> cardLayer = new JLayer<>(inner, layerUI);

        // ── Section: toolbar above card ───────────────────────────────────────
        JPanel section = new JPanel(new BorderLayout(0, 10));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(toolbar,   BorderLayout.NORTH);
        section.add(cardLayer, BorderLayout.CENTER);

        applyFilter();
        return section;
    }

    // ─── Filter ───────────────────────────────────────────────────────────────

    private void applyFilter() {
        tableModel.setRowCount(0);
        String statusFilter = (String) filterStatus.getSelectedItem();
        String dateFilter   = filterDateField.getText().trim();
        String searchRaw    = searchField != null ? searchField.getText().trim() : "";
        String searchQuery  = (searchRaw.equals("Search by ID, Customer or Plate") || searchRaw.isEmpty())
                              ? "" : searchRaw.toLowerCase();

        String isoDate = "";
        if (!dateFilter.isEmpty()) {
            try { LocalDate.parse(dateFilter, DateTimeFormatter.ofPattern("yyyy-MM-dd")); isoDate = dateFilter; }
            catch (Exception e1) {
                try { isoDate = LocalDate.parse(dateFilter, DateTimeFormatter.ofPattern("dd/MM/yyyy")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); }
                catch (Exception e2) {
                    try { isoDate = LocalDate.parse(dateFilter, DateTimeFormatter.ofPattern("MM/dd/yyyy")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); }
                    catch (Exception ignored) {}
                }
            }
        }

        List<Appointment> completed = appointmentDAO.getAppointmentsByStatus("Completed");
        List<Appointment> paid      = appointmentDAO.getAppointmentsByStatus("Paid");
        displayedAppointments = new java.util.ArrayList<>();
        displayedAppointments.addAll(completed);
        displayedAppointments.addAll(paid);

        int count = 0, completedCount = 0;
        for (Appointment a : displayedAppointments) {
            String status = a.getStatus();
            if ("Completed".equalsIgnoreCase(status)) completedCount++;
            boolean matchStatus = "All Status".equals(statusFilter) || status.equalsIgnoreCase(statusFilter);
            boolean matchDate   = isoDate.isEmpty() || a.getAppointmentDate().equals(isoDate);
            if (!matchStatus || !matchDate) continue;

            if (!searchQuery.isEmpty()) {
                boolean match =
                    a.getAppointmentId().toLowerCase().contains(searchQuery)
                    || a.getCustomerId().toLowerCase().contains(searchQuery)
                    || a.getVehiclePlate().toLowerCase().contains(searchQuery);
                if (!match) continue;
            }

            tableModel.addRow(new Object[]{
                a.getAppointmentId(), a.getCustomerId(), a.getVehiclePlate(),
                a.getServiceItemId(), a.getAppointmentDate(), a.getStatus()
            });
            count++;
        }
        countLabel.setText("Showing " + count + " records  |  " + completedCount + " Awaiting Payment");
        if (formPanel != null) { setTableCompact(false); formPanel.setVisible(false); }
        if (onDataChanged != null) onDataChanged.run();
    }

    // ─── Payment form ─────────────────────────────────────────────────────────

    private JPanel buildPaymentForm() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        formApptIdLabel = UIComponent.createSectionLabel("Process Payment");
        headerRow.add(formApptIdLabel, BorderLayout.WEST);
        JButton closeBtn = UIComponent.createLinkButton("✕ Close");
        closeBtn.addActionListener(e -> { setTableCompact(false); formPanel.setVisible(false); });
        headerRow.add(closeBtn, BorderLayout.EAST);
        section.add(headerRow);

        JPanel twoCol = new JPanel(new GridBagLayout());
        twoCol.setOpaque(false);
        twoCol.setAlignmentX(Component.LEFT_ALIGNMENT);
        twoCol.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 0; gbc.gridy = 0;
        twoCol.add(buildSummaryCard(), gbc);

        gbc.insets = new Insets(0, AppTheme.GAP_ITEM, 0, 0);
        gbc.weightx = 0.6;
        gbc.gridx = 1;
        twoCol.add(buildPaymentInputCard(), gbc);
        section.add(twoCol);
        return section;
    }

    private JPanel summaryApptId, summaryCustomer, summaryPlate,
                   summaryService, summaryDate, summaryApptStatus;

private JPanel buildSummaryCard() {
    JPanel card = UIComponent.createCard();
    card.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel sectionLbl = UIComponent.createSectionLabel("Appointment Summary");
    sectionLbl.setHorizontalAlignment(SwingConstants.LEFT);
    sectionLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
    sectionLbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, sectionLbl.getPreferredSize().height));
    card.add(sectionLbl);

    summaryApptId     = UIComponent.createInfoRow("Appointment ID", "—");
    summaryCustomer   = UIComponent.createInfoRow("Customer ID",    "—");
    summaryPlate      = UIComponent.createInfoRow("Vehicle Plate",  "—");
    summaryService    = UIComponent.createInfoRow("Service Item",   "—");
    summaryDate       = UIComponent.createInfoRow("Date",           "—");
    summaryApptStatus = UIComponent.createInfoRow("Status",         "—");

    // Left-align BOTH the field name (WEST) and value (CENTER) labels in every row
    for (JPanel row : new JPanel[]{summaryApptId, summaryCustomer, summaryPlate,
                                   summaryService, summaryDate, summaryApptStatus}) {
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height + 8));

        BorderLayout bl = (BorderLayout) row.getLayout();

        Component west = bl.getLayoutComponent(BorderLayout.WEST);
        if (west instanceof JLabel) {
            ((JLabel) west).setHorizontalAlignment(SwingConstants.LEFT);
        }

        Component center = bl.getLayoutComponent(BorderLayout.CENTER);
        if (center instanceof JLabel) {
            ((JLabel) center).setHorizontalAlignment(SwingConstants.LEFT);
        }
    }

    card.add(summaryApptId);     card.add(UIComponent.createRowDivider());
    card.add(summaryCustomer);   card.add(UIComponent.createRowDivider());
    card.add(summaryPlate);      card.add(UIComponent.createRowDivider());
    card.add(summaryService);    card.add(UIComponent.createRowDivider());
    card.add(summaryDate);       card.add(UIComponent.createRowDivider());
    card.add(summaryApptStatus);
    return card;
}

private JPanel buildPaymentInputCard() {
    JPanel card = UIComponent.createCard();
    card.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel sectionLbl = UIComponent.createSectionLabel("Payment Details");
    sectionLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
    sectionLbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, sectionLbl.getPreferredSize().height));
    card.add(sectionLbl);

    // ── Amount (read-only) ────────────────────────────────────────────────
    JLabel amountLbl = UIComponent.createFieldLabel("AMOUNT (RM)");
    amountLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
    amountLbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, amountLbl.getPreferredSize().height));
    card.add(amountLbl);
    card.add(UIComponent.verticalGap(4));
    amountField = UIComponent.createTextField("—");
    amountField.setEditable(false);
    amountField.setBackground(new Color(245, 245, 247));
    amountField.setAlignmentX(Component.LEFT_ALIGNMENT);
    amountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
    card.add(amountField);
    card.add(UIComponent.verticalGap(8));

    // ── Additional charge ─────────────────────────────────────────────────
    JLabel extraLbl = UIComponent.createFieldLabel("ADDITIONAL CHARGE (RM)");
    extraLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
    extraLbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, extraLbl.getPreferredSize().height));
    card.add(extraLbl);
    card.add(UIComponent.verticalGap(4));
    additionalChargeField = UIComponent.createTextField("0.00");
    additionalChargeField.setText("0.00");
    additionalChargeField.setAlignmentX(Component.LEFT_ALIGNMENT);
    additionalChargeField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
    card.add(additionalChargeField);
    card.add(UIComponent.verticalGap(10));

    // ── Total row ─────────────────────────────────────────────────────────
    JPanel totalRow = new JPanel();
    totalRow.setLayout(new BoxLayout(totalRow, BoxLayout.X_AXIS));
    totalRow.setOpaque(false);
    totalRow.setAlignmentX(Component.LEFT_ALIGNMENT);
    totalRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

    JLabel totalLbl = new JLabel("TOTAL (RM)");
    totalLbl.setFont(AppTheme.FONT_LABEL);
    totalLbl.setForeground(AppTheme.TEXT_SECONDARY);

    totalAmountLabel = new JLabel("0.00");
    totalAmountLabel.setFont(AppTheme.FONT_HEADING_3);
    totalAmountLabel.setForeground(AppTheme.PRIMARY);
    totalAmountLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));

    totalRow.add(totalLbl);
    totalRow.add(Box.createHorizontalGlue());
    totalRow.add(totalAmountLabel);
    card.add(totalRow);
    card.add(UIComponent.verticalGap(10));

    KeyAdapter totalUpdater = new KeyAdapter() {
        @Override public void keyReleased(KeyEvent e) { recalcTotal(); }
    };
    amountField.addKeyListener(totalUpdater);
    additionalChargeField.addKeyListener(totalUpdater);

    // ── Separator ─────────────────────────────────────────────────────────
    JSeparator sep = UIComponent.createSeparator();
    sep.setAlignmentX(Component.LEFT_ALIGNMENT);
    sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
    card.add(sep);
    card.add(UIComponent.verticalGap(10));

    // ── Payment method ────────────────────────────────────────────────────
    JLabel methodLbl = UIComponent.createFieldLabel("PAYMENT METHOD");
    methodLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
    methodLbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, methodLbl.getPreferredSize().height));
    card.add(methodLbl);
    card.add(UIComponent.verticalGap(4));
    paymentMethodCombo = UIComponent.createComboBox(
        new String[]{"Cash", "Online Banking / FPX", "Debit / Credit Card", "E-Wallet"});
    paymentMethodCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
    paymentMethodCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
    card.add(paymentMethodCombo);
    card.add(UIComponent.verticalGap(AppTheme.GAP_SECTION));

    // ── Action buttons — full width, equal size ───────────────────────────
    JPanel btnRow = new JPanel(new GridLayout(1, 2, AppTheme.GAP_ITEM, 0));
    btnRow.setOpaque(false);
    btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
    btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

    JButton confirmBtn = UIComponent.createPrimaryButton("Confirm Payment");
    confirmBtn.addActionListener(e -> handleConfirmPayment());

    JButton cancelBtn = UIComponent.createOutlineButton("Cancel");
    cancelBtn.addActionListener(e -> { setTableCompact(false); formPanel.setVisible(false); });

    btnRow.add(confirmBtn);
    btnRow.add(cancelBtn);
    card.add(btnRow);
    return card;
}

    // ─── Logic ────────────────────────────────────────────────────────────────

    private void recalcTotal() {
        totalAmountLabel.setText(String.format("%.2f",
            parseDouble(amountField.getText()) + parseDouble(additionalChargeField.getText())));
    }

    private double parseDouble(String s) {
        try { return Double.parseDouble(s.trim()); }
        catch (NumberFormatException e) { return 0.0; }
    }

    private void handleConfirmPayment() {
        String amtStr   = amountField.getText().trim();
        String extraStr = additionalChargeField.getText().trim();
        if (amtStr.isEmpty() || parseDouble(amtStr) <= 0) { showError("Please enter a valid amount."); return; }

        String method    = (String) paymentMethodCombo.getSelectedItem();
        double amount    = parseDouble(amtStr);
        double extra     = parseDouble(extraStr.isEmpty() ? "0.00" : extraStr);
        double total     = amount + extra;
        String date      = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String paymentId = paymentDAO.generateNextPaymentId();
        String receiptNo = paymentDAO.generateNextReceiptNo();

        Payment payment = new Payment(
            paymentId, selectedApptId,
            String.format("%.2f", amount), method, date, receiptNo, selectedStaffId,
            String.format("%.2f", extra), String.format("%.2f", total));

        if (!paymentDAO.addPayment(payment)) { showError("Failed to save payment record. Please try again."); return; }
        if (!appointmentDAO.markAppointmentAsPaid(selectedApptId))
            showError("Payment saved but appointment status could not be updated.");

        final Payment paymentFinal = payment;
        UIComponent.showDialog(this, "Payment Confirmed", UIComponent.DialogType.SUCCESS,
            "Payment recorded successfully!",
            new String[][]{
                {"Receipt No",  receiptNo},
                {"Payment ID",  paymentId},
                {"Total Paid",  String.format("RM %.2f", total)},
                {"Method",      method},
            },
            new UIComponent.DialogAction[]{
                new UIComponent.DialogAction("Generate Receipt (PDF)", UIComponent.DialogActionStyle.PRIMARY,
                    () -> generateReceipt(paymentFinal), new Dimension(190, 38)),
                new UIComponent.DialogAction("Close", UIComponent.DialogActionStyle.OUTLINE, null,
                    new Dimension(80, 34)),
            });

        applyFilter();
        formPanel.setVisible(false);
        amountField.setText("");
        additionalChargeField.setText("0.00");
        totalAmountLabel.setText("0.00");
    }

    private void generateReceipt(Payment payment) {
        Appointment appt = null;
        for (Appointment a : displayedAppointments) {
            if (a.getAppointmentId().equals(payment.getAppointmentId())) { appt = a; break; }
        }
        String vehiclePlate    = appt != null ? appt.getVehiclePlate()    : payment.getAppointmentId();
        String appointmentDate = appt != null ? appt.getAppointmentDate() : payment.getPaymentDate();
        String serviceItemId   = appt != null ? appt.getServiceItemId()   : "—";
        String serviceName     = models.dao.ServiceItemDAO.getServiceName(serviceItemId);
        String customerDisplay = appt != null ? appt.getCustomerId()      : "—";

        try {
            java.io.File pdf = ReceiptGenerator.generate(payment, customerDisplay,
                vehiclePlate, appointmentDate, serviceName, "receipts");
            final java.io.File pdfFinal = pdf;
            UIComponent.showDialog(this, "Receipt Generated", UIComponent.DialogType.SUCCESS,
                "Receipt saved to: " + pdf.getAbsolutePath(), null,
                new UIComponent.DialogAction[]{
                    new UIComponent.DialogAction("Open File", UIComponent.DialogActionStyle.PRIMARY, () -> {
                        try { java.awt.Desktop.getDesktop().open(pdfFinal); }
                        catch (Exception ex) {
                            UIComponent.showDialog(CounterStaffBilling.this, "Open File",
                                UIComponent.DialogType.WARNING,
                                "Could not open automatically. Path:\n" + pdfFinal.getAbsolutePath(),
                                null, null);
                        }
                    }),
                    new UIComponent.DialogAction("Close", UIComponent.DialogActionStyle.OUTLINE, null),
                });
        } catch (Exception ex) {
            ex.printStackTrace();
            UIComponent.showDialog(this, "Receipt Error", UIComponent.DialogType.DANGER,
                "Failed to generate receipt PDF:\n" + ex.getMessage(), null, null);
        }
    }

    private void showError(String message) {
        UIComponent.showDialog(this, "Validation Error",
            UIComponent.DialogType.WARNING, message, null, null);
    }

    /** Shrink the table to ~4 visible rows when the payment form is open, restore when closed. */
    private void setTableCompact(boolean compact) {
        if (tableScrollPane == null) return;
        int rowH  = apptTable.getRowHeight();   // e.g. 52
        int hdrH  = apptTable.getTableHeader().getPreferredSize().height; // 38
        int sbarH = 30; // approximate status-bar height
        if (compact) {
            int compactH = hdrH + rowH * 4 + sbarH; // show 4 rows
            tableScrollPane.setPreferredSize(new Dimension(tableScrollPane.getWidth(), compactH));
            tableScrollPane.setMinimumSize  (new Dimension(0,                          compactH));
            tableScrollPane.setMaximumSize  (new Dimension(Integer.MAX_VALUE,          compactH));
        } else {
            tableScrollPane.setPreferredSize(null);
            tableScrollPane.setMinimumSize  (null);
            tableScrollPane.setMaximumSize  (new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        }
        // Revalidate the whole page so BoxLayout recalculates sizes
        SwingUtilities.invokeLater(() -> {
            Container top = SwingUtilities.getAncestorOfClass(JPanel.class, tableScrollPane);
            if (top != null) { top.revalidate(); top.repaint(); }
        });
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
        updateInfoRow(summaryService, serviceItemId.equals(serviceName)
            ? serviceItemId : serviceItemId + " - " + serviceName);
        String priceStr = "0.00";
        for (String[] item : ServiceItemDAO.getAllServiceItems())
            if (item[0].equals(serviceItemId)) { priceStr = item[3]; break; }
        amountField.setText(priceStr);
        amountField.setEditable(false);
        amountField.setBackground(new Color(245, 245, 247));
        additionalChargeField.setText("0.00");
        recalcTotal();
        paymentMethodCombo.setSelectedIndex(0);
        setTableCompact(true);
        formPanel.setVisible(true);
    }

    private void updateInfoRow(JPanel row, String value) {
        if (row == null) return;
        Component center = ((BorderLayout) row.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        if (center instanceof JLabel) ((JLabel) center).setText(value);
    }
}