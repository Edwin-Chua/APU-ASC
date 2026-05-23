package views.staff;

import models.dao.AppointmentDAO;
import models.dao.TechnicianDAO;
import models.entities.Appointment;
import models.entities.Technician;
import views.MainFrame;
import views.shared.AppTheme;
import views.shared.UIComponent;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class CounterStaffViewAppointment extends JPanel {

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final TechnicianDAO  technicianDAO  = new TechnicianDAO();

    // ─── Toolbar ──────────────────────────────────────────────────────────────
    private JTextField         searchField;
    private JComboBox<String>  filterStatus;
    private UIComponent.DatePickerField filterDate;
    private JLabel             countLabel;

    // ─── Table ────────────────────────────────────────────────────────────────
    private DefaultTableModel tableModel;
    private JTable            table;

    private static final String[] COLS = {
        "Appt ID", "Customer Name", "Service Requested", "Vehicle", "Date", "Time", "Status"
    };

    // ─── Bottom card ──────────────────────────────────────────────────────────
    // Four cards: "hint" | "pending" | "approved" | "view"
    private JPanel bodyPanel; // CardLayout container

    // Pending card labels
    private JLabel            lblApptId, lblCustomer, lblService, lblVehicle, lblDateTime, lblStatus;
    private JComboBox<String> techCombo;
    private JButton           btnApprove, btnReject;

    // Approved card labels
    private JLabel lblApprovedApptId, lblApprovedCustomer, lblApprovedService,
                   lblApprovedVehicle, lblApprovedDateTime, lblApprovedTech, lblApprovedStatus;
    private JButton btnCancel;

    // View-only card labels (Completed / Rejected / Cancelled)
    private JLabel lblViewApptId, lblViewCustomer, lblViewService,
                   lblViewVehicle, lblViewDateTime, lblViewTech, lblViewStatus;

    // currently selected appointment ID
    private String selectedApptId = null;

    // logged-in staff ID
    private String staffId = "CS001";

    public CounterStaffViewAppointment(MainFrame frame) {
        setLayout(new BorderLayout(0, 16));
        setBackground(AppTheme.BG_PAGE);
        setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_PAGE, AppTheme.PADDING_PAGE,
            AppTheme.PADDING_PAGE, AppTheme.PADDING_PAGE
        ));

        add(buildHeader(),      BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);

        loadTable();
    }

    public void setStaffId(String id) { this.staffId = id; }

    // ─── Page title ───────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JLabel title = UIComponent.createPageTitle("Appointment Management");
        JLabel sub   = UIComponent.createCaptionLabel("View, approve, assign and cancel appointments");
        sub.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.add(title);
        left.add(sub);

        p.add(left, BorderLayout.WEST);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        return p;
    }

    // ─── Main content ─────────────────────────────────────────────────────────
    private JPanel buildMainContent() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        p.add(buildTableCard());
        p.add(Box.createRigidArea(new Dimension(0, 16)));
        p.add(buildBottomCard());

        return p;
    }

    // ─── Table card ───────────────────────────────────────────────────────────
    private JPanel buildTableCard() {
        JPanel card = UIComponent.createCard();
        card.setLayout(new BorderLayout(0, 0));
        card.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Toolbar — single row: [search] [status] [date] [refresh]  |  count
        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_CARD, AppTheme.PADDING_CARD, AppTheme.PADDING_CARD, AppTheme.PADDING_CARD
        ));

        // Search field — same custom-painted rounded style as ManageEmployeesPage
        final String SEARCH_PLACEHOLDER = "🔍  Search by ID, Customer or Plate";
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
        searchField.setText(SEARCH_PLACEHOLDER);
        searchField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (searchField.getText().equals(SEARCH_PLACEHOLDER)) {
                    searchField.setText("");
                    searchField.setForeground(AppTheme.TEXT_PRIMARY);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (searchField.getText().trim().isEmpty()) {
                    searchField.setText(SEARCH_PLACEHOLDER);
                    searchField.setForeground(AppTheme.TEXT_MUTED);
                }
            }
        });
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { loadTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { loadTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { loadTable(); }
        });

        // Filter controls — fixed width, sit to the right of the search field
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        filterRow.setOpaque(false);

        filterStatus = UIComponent.createComboBox(new String[]{
            "All Status", "Pending", "Approved", "Completed", "Cancelled", "Rejected"
        });
        filterStatus.setPreferredSize(new Dimension(140, 36));
        filterStatus.setToolTipText("Filter by status");
        filterStatus.addActionListener(e -> loadTable());

        filterDate = UIComponent.createDatePicker(null);
        filterDate.setPreferredSize(new Dimension(125, 36));
        filterDate.setToolTipText("Filter by date");
        filterDate.addDateChangeListener(d -> loadTable());

        JButton btnRefresh = UIComponent.createSmallButton("↺  Refresh");
        btnRefresh.setPreferredSize(new Dimension(100, 34));
        btnRefresh.addActionListener(e -> {
            searchField.setText(SEARCH_PLACEHOLDER);
            searchField.setForeground(AppTheme.TEXT_MUTED);
            filterStatus.setSelectedIndex(0);
            filterDate.setDate(null);
            loadTable();
        });

        filterRow.add(searchField);
        filterRow.add(filterStatus);
        filterRow.add(filterDate);
        filterRow.add(btnRefresh);

        countLabel = new JLabel("Loading...");
        countLabel.setFont(AppTheme.FONT_SMALL);
        countLabel.setForeground(AppTheme.TEXT_SECONDARY);

        toolbar.add(filterRow,  BorderLayout.WEST);
        toolbar.add(countLabel, BorderLayout.EAST);

        // Table
        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(AppTheme.ROW_HEIGHT_TABLE);
        table.setFont(AppTheme.FONT_SMALL);
        table.setForeground(AppTheme.TEXT_PRIMARY);
        table.setBackground(AppTheme.BG_CARD);
        table.setGridColor(AppTheme.SEPARATOR);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setSelectionBackground(AppTheme.PRIMARY);
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = table.getTableHeader();
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
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)
                ));
                return this;
            }
        };
        for (int i = 0; i < COLS.length; i++)
            table.getColumnModel().getColumn(i).setHeaderRenderer(hdrRenderer);

        int[] widths = {80, 160, 190, 160, 110, 80, 100};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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
                    if (col == 6) {
                        String status = (String) tableModel.getValueAt(row, 6);
                        if (status != null) {
                            switch (status.toLowerCase()) {
                                case "completed": setForeground(AppTheme.STATUS_COMPLETED_FG);   break;
                                case "approved":  setForeground(AppTheme.STATUS_INPROGRESS_FG);  break;
                                case "pending":   setForeground(AppTheme.STATUS_PENDING_FG);     break;
                                case "cancelled": setForeground(new Color(150, 100, 50));        break;
                                case "rejected":  setForeground(AppTheme.DANGER);                break;
                                default:          setForeground(AppTheme.TEXT_PRIMARY);
                            }
                        }
                    } else {
                        setForeground(AppTheme.TEXT_PRIMARY);
                    }
                }
                return this;
            }
        });

        // Single listener — drives the bottom card
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onRowSelected();
        });

        JLabel statusBar = new JLabel("  Select a row to view appointment details");
        statusBar.setFont(AppTheme.FONT_CAPTION);
        statusBar.setForeground(AppTheme.TEXT_MUTED);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.SEPARATOR),
            BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        statusBar.setBackground(AppTheme.BG_PAGE);
        statusBar.setOpaque(true);

        table.getSelectionModel().addListSelectionListener(e2 -> {
            if (!e2.getValueIsAdjusting()) {
                int r = table.getSelectedRow();
                statusBar.setText(r >= 0
                    ? "  " + tableModel.getValueAt(r, 0) + "  ·  " + tableModel.getValueAt(r, 1) + "  ·  " + tableModel.getValueAt(r, 2)
                    : "  Select a row to view appointment details");
            }
        });

        JScrollPane scroll = UIComponent.createAppleScrollPane(table);
        scroll.getViewport().setBackground(AppTheme.BG_CARD);
        scroll.setPreferredSize(new Dimension(0, 210));

        card.add(toolbar,   BorderLayout.NORTH);
        card.add(scroll,    BorderLayout.CENTER);
        card.add(statusBar, BorderLayout.SOUTH);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    // ─── Bottom card ──────────────────────────────────────────────────────────
    private JPanel buildBottomCard() {
        JPanel outer = UIComponent.createCard();
        outer.setLayout(new BorderLayout(0, 0));
        outer.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        outer.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel sectionHeader = new JPanel(new BorderLayout());
        sectionHeader.setOpaque(false);
        sectionHeader.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.SEPARATOR),
            BorderFactory.createEmptyBorder(10, AppTheme.PADDING_CARD, 10, AppTheme.PADDING_CARD)
        ));

        // Left accent bar + title in a row
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);

        JPanel accentBar = new JPanel();
        accentBar.setBackground(AppTheme.PRIMARY);
        accentBar.setPreferredSize(new Dimension(3, 16));
        accentBar.setOpaque(true);

        JLabel sectionTitle = new JLabel("Appointment Details");
        sectionTitle.setFont(AppTheme.FONT_SMALL_BOLD);
        sectionTitle.setForeground(AppTheme.TEXT_PRIMARY);

        titleRow.add(accentBar);
        titleRow.add(sectionTitle);
        sectionHeader.add(titleRow, BorderLayout.WEST);
        outer.add(sectionHeader, BorderLayout.NORTH);

        // ── hint ──────────────────────────────────────────────────────────────
        JPanel hintCard = new JPanel();
        hintCard.setLayout(new BoxLayout(hintCard, BoxLayout.Y_AXIS));
        hintCard.setOpaque(false);
        hintCard.setPreferredSize(new Dimension(0, 200));

        JLabel hintIcon = new JLabel("☰", SwingConstants.CENTER);
        hintIcon.setFont(hintIcon.getFont().deriveFont(28f));
        hintIcon.setForeground(AppTheme.TEXT_MUTED);
        hintIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        hintIcon.setBorder(BorderFactory.createEmptyBorder(32, 0, 8, 0));

        JLabel hint = new JLabel("Select an appointment from the table above to view its details", SwingConstants.CENTER);
        hint.setFont(AppTheme.FONT_SMALL);
        hint.setForeground(AppTheme.TEXT_MUTED);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        hintCard.add(hintIcon);
        hintCard.add(hint);

        // Build the three detail views
        JPanel pendingCard  = buildPendingCard();
        JPanel approvedCard = buildApprovedCard();
        JPanel viewCard     = buildViewCard();

        bodyPanel = new JPanel(new CardLayout());
        bodyPanel.setOpaque(false);
        bodyPanel.setPreferredSize(new Dimension(0, 240));
        bodyPanel.add(hintCard,     "hint");
        bodyPanel.add(pendingCard,  "pending");
        bodyPanel.add(approvedCard, "approved");
        bodyPanel.add(viewCard,     "view");

        outer.add(bodyPanel, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(outer, BorderLayout.CENTER);
        return wrapper;
    }

    // ─── Pending card (approve / reject) ─────────────────────────────────────
    private JPanel buildPendingCard() {
        JPanel card = new JPanel(new BorderLayout(24, 0));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_CARD, AppTheme.PADDING_CARD,
            AppTheme.PADDING_CARD, AppTheme.PADDING_CARD
        ));

        // Info labels
        lblApptId   = new JLabel(); lblApptId.setFont(AppTheme.FONT_SMALL);
        lblCustomer = new JLabel(); lblCustomer.setFont(AppTheme.FONT_SMALL);
        lblService  = new JLabel(); lblService.setFont(AppTheme.FONT_SMALL_BOLD);
        lblService.setForeground(AppTheme.PRIMARY);
        lblVehicle  = new JLabel(); lblVehicle.setFont(AppTheme.FONT_SMALL);
        lblDateTime = new JLabel(); lblDateTime.setFont(AppTheme.FONT_SMALL);
        lblStatus   = new JLabel("Pending");
        lblStatus.setFont(AppTheme.FONT_SMALL_BOLD);
        lblStatus.setForeground(AppTheme.STATUS_PENDING_FG);

        JLabel[] vals   = {lblApptId, lblCustomer, lblService, lblVehicle, lblDateTime, lblStatus};
        String[] fields = {"Appointment ID:", "Customer Name:", "Service Requested:",
                           "Vehicle:", "Scheduled Date/Time:", "Current Status:"};

        card.add(buildInfoGrid(fields, vals), BorderLayout.CENTER);

        // Right panel
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(320, 0));

        // Technician combo — clean labelled section, no TitledBorder
        JLabel techLabel = new JLabel("Assign Technician");
        techLabel.setFont(AppTheme.FONT_SMALL_BOLD);
        techLabel.setForeground(AppTheme.TEXT_SECONDARY);
        techLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        techLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        techCombo = UIComponent.createComboBox(new String[]{});
        techCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        techCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Buttons
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        btnApprove = UIComponent.createPrimaryButton("✔  Approve");
        btnReject  = UIComponent.createDangerButton("✖  Reject");
        btnApprove.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnReject.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnApprove.addActionListener(e -> handleApprove());
        btnReject.addActionListener(e  -> handleReject());

        btnRow.add(btnApprove);
        btnRow.add(btnReject);

        right.add(techLabel);
        right.add(techCombo);
        right.add(btnRow);

        card.add(right, BorderLayout.EAST);
        return card;
    }

    // ─── Approved card (view + cancel) ────────────────────────────────────────
    private JPanel buildApprovedCard() {
        JPanel card = new JPanel(new BorderLayout(24, 0));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_CARD, AppTheme.PADDING_CARD,
            AppTheme.PADDING_CARD, AppTheme.PADDING_CARD
        ));

        lblApprovedApptId   = new JLabel(); lblApprovedApptId.setFont(AppTheme.FONT_SMALL);
        lblApprovedCustomer = new JLabel(); lblApprovedCustomer.setFont(AppTheme.FONT_SMALL);
        lblApprovedService  = new JLabel(); lblApprovedService.setFont(AppTheme.FONT_SMALL_BOLD);
        lblApprovedService.setForeground(AppTheme.PRIMARY);
        lblApprovedVehicle  = new JLabel(); lblApprovedVehicle.setFont(AppTheme.FONT_SMALL);
        lblApprovedDateTime = new JLabel(); lblApprovedDateTime.setFont(AppTheme.FONT_SMALL);
        lblApprovedTech     = new JLabel(); lblApprovedTech.setFont(AppTheme.FONT_SMALL);
        lblApprovedStatus   = new JLabel("Approved");
        lblApprovedStatus.setFont(AppTheme.FONT_SMALL_BOLD);
        lblApprovedStatus.setForeground(AppTheme.STATUS_INPROGRESS_FG);

        JLabel[] vals   = {lblApprovedApptId, lblApprovedCustomer, lblApprovedService,
                           lblApprovedVehicle, lblApprovedDateTime, lblApprovedTech, lblApprovedStatus};
        String[] fields = {"Appointment ID:", "Customer Name:", "Service Requested:",
                           "Vehicle:", "Scheduled Date/Time:", "Assigned Technician:", "Current Status:"};

        card.add(buildInfoGrid(fields, vals), BorderLayout.CENTER);

        // Right: notice + cancel button
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(320, 0));

        JPanel notice = new JPanel(new BorderLayout());
        notice.setBackground(new Color(255, 249, 230));
        notice.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(240, 210, 100), 1, true),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        notice.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        notice.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel noticeText = new JLabel(
            "<html><b>Approved</b> — Technician assigned.<br>"
            + "<span style='color:#a07800'>Cancelling will free the technician automatically.</span></html>");
        noticeText.setFont(AppTheme.FONT_SMALL);
        noticeText.setForeground(new Color(100, 75, 0));
        notice.add(noticeText, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new GridLayout(1, 1));
        btnRow.setOpaque(false);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        btnCancel = UIComponent.createDangerButton("✖  Cancel Appointment");
        btnCancel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnCancel.addActionListener(e -> handleCancel());
        btnRow.add(btnCancel);

        right.add(notice);
        right.add(btnRow);
        card.add(right, BorderLayout.EAST);
        return card;
    }

    // ─── View-only card (Completed / Rejected / Cancelled) ───────────────────
    private JPanel buildViewCard() {
        JPanel card = new JPanel(new BorderLayout(24, 0));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_CARD, AppTheme.PADDING_CARD,
            AppTheme.PADDING_CARD, AppTheme.PADDING_CARD
        ));

        lblViewApptId   = new JLabel(); lblViewApptId.setFont(AppTheme.FONT_SMALL);
        lblViewCustomer = new JLabel(); lblViewCustomer.setFont(AppTheme.FONT_SMALL);
        lblViewService  = new JLabel(); lblViewService.setFont(AppTheme.FONT_SMALL_BOLD);
        lblViewService.setForeground(AppTheme.PRIMARY);
        lblViewVehicle  = new JLabel(); lblViewVehicle.setFont(AppTheme.FONT_SMALL);
        lblViewDateTime = new JLabel(); lblViewDateTime.setFont(AppTheme.FONT_SMALL);
        lblViewTech     = new JLabel(); lblViewTech.setFont(AppTheme.FONT_SMALL);
        lblViewStatus   = new JLabel(); lblViewStatus.setFont(AppTheme.FONT_SMALL_BOLD);

        JLabel[] vals   = {lblViewApptId, lblViewCustomer, lblViewService,
                           lblViewVehicle, lblViewDateTime, lblViewTech, lblViewStatus};
        String[] fields = {"Appointment ID:", "Customer Name:", "Service Requested:",
                           "Vehicle:", "Scheduled Date/Time:", "Assigned Technician:", "Status:"};

        card.add(buildInfoGrid(fields, vals), BorderLayout.CENTER);

        // Right: read-only notice
        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(320, 0));

        JPanel noticeBox = new JPanel(new BorderLayout());
        noticeBox.setBackground(new Color(245, 245, 250));
        noticeBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER_DEFAULT, 1, true),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        JLabel noticeText = new JLabel(
            "<html><b>View Only</b><br><br>"
            + "This appointment is in a final state<br>and cannot be modified.</html>");
        noticeText.setFont(AppTheme.FONT_SMALL);
        noticeText.setForeground(AppTheme.TEXT_SECONDARY);
        noticeBox.add(noticeText, BorderLayout.CENTER);
        right.add(noticeBox, BorderLayout.NORTH);

        card.add(right, BorderLayout.EAST);
        return card;
    }

    // ─── Row selection → swap bottom card ─────────────────────────────────────
    private void onRowSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { showCard("hint"); return; }

        String apptId   = (String) tableModel.getValueAt(row, 0);
        String custName = (String) tableModel.getValueAt(row, 1);
        String service  = (String) tableModel.getValueAt(row, 2);
        String vehicle  = (String) tableModel.getValueAt(row, 3);
        String date     = (String) tableModel.getValueAt(row, 4);
        String time     = (String) tableModel.getValueAt(row, 5);
        String status   = (String) tableModel.getValueAt(row, 6);

        selectedApptId = apptId;

        // Find full appointment record (for technician ID etc.)
        Appointment appt = appointmentDAO.getAllAppointments().stream()
            .filter(a -> a.getAppointmentId().equals(apptId))
            .findFirst().orElse(null);

        String techDisplay = resolveTechDisplay(appt != null ? appt.getTechnicianId() : null);

        switch (status.toLowerCase()) {

            case "pending":
                lblApptId.setText(apptId);
                lblCustomer.setText(custName);
                lblService.setText(service);
                lblVehicle.setText(vehicle);
                lblDateTime.setText(date + " at " + time);
                lblStatus.setText("Pending");

                // Populate technician combo — availability checked against appointments
                techCombo.removeAllItems();
                techCombo.addItem("-- Select Technician --");
                List<Technician> available = getAvailableTechniciansForSlot(date, time);
                if (available.isEmpty()) {
                    techCombo.addItem("(No technicians free for this slot)");
                } else {
                    for (Technician t : available) {
                        techCombo.addItem(t.getTechnicianId() + " — " + t.getUsername());
                    }
                }

                showCard("pending");
                break;

            case "approved":
                lblApprovedApptId.setText(apptId);
                lblApprovedCustomer.setText(custName);
                lblApprovedService.setText(service);
                lblApprovedVehicle.setText(vehicle);
                lblApprovedDateTime.setText(date + " at " + time);
                lblApprovedTech.setText(techDisplay);
                lblApprovedStatus.setText("Approved");
                showCard("approved");
                break;

            default: // completed, rejected, cancelled
                lblViewApptId.setText(apptId);
                lblViewCustomer.setText(custName);
                lblViewService.setText(service);
                lblViewVehicle.setText(vehicle);
                lblViewDateTime.setText(date + " at " + time);
                lblViewTech.setText(techDisplay.isEmpty() ? "—" : techDisplay);
                lblViewStatus.setText(status);
                switch (status.toLowerCase()) {
                    case "completed": lblViewStatus.setForeground(AppTheme.STATUS_COMPLETED_FG); break;
                    case "rejected":  lblViewStatus.setForeground(AppTheme.DANGER);               break;
                    case "cancelled": lblViewStatus.setForeground(new Color(150, 100, 50));       break;
                    default:          lblViewStatus.setForeground(AppTheme.TEXT_PRIMARY);
                }
                showCard("view");
                break;
        }
    }

    // Directly flip the CardLayout — no tree walking, no visibility tricks
    private void showCard(String name) {
        ((CardLayout) bodyPanel.getLayout()).show(bodyPanel, name);
    }

    // ─── Technician availability — slot-based check ────────────────────────────
    /**
     * Returns technicians who have NO Approved or Pending appointment on the
     * same date AND time slot. This is the accurate availability check —
     * workStatus alone can be stale; we use the booked appointments as source
     * of truth.
     */
    private List<Technician> getAvailableTechniciansForSlot(String date, String timeSlot) {
        List<Appointment> allAppts = appointmentDAO.getAllAppointments();

        // IDs of technicians already committed to this exact slot
        java.util.Set<String> busyIds = allAppts.stream()
            .filter(a -> (a.getStatus().equalsIgnoreCase("Approved")
                       || a.getStatus().equalsIgnoreCase("Pending"))
                      && date.equals(a.getAppointmentDate())
                      && timeSlot.equals(a.getTimeSlot())
                      && a.getTechnicianId() != null
                      && !a.getTechnicianId().equalsIgnoreCase("null")
                      && !a.getTechnicianId().trim().isEmpty())
            .map(Appointment::getTechnicianId)
            .collect(Collectors.toSet());

        return technicianDAO.getAllTechnicians().stream()
            .filter(t -> !busyIds.contains(t.getTechnicianId()))
            .collect(Collectors.toList());
    }

    // ─── Approve & Assign ─────────────────────────────────────────────────────
    private void handleApprove() {
        if (selectedApptId == null) return;

        String techSelected = (String) techCombo.getSelectedItem();
        if (techSelected == null || techSelected.startsWith("--") || techSelected.startsWith("(No")) {
            JOptionPane.showMessageDialog(this,
                "Please select a technician before approving.",
                "No Technician Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String techId = techSelected.split(" — ")[0].trim();

        boolean ok = appointmentDAO.approveAppointment(selectedApptId, techId, staffId);
        if (ok) {
            technicianDAO.updateWorkStatus(techId, "Busy");
            JOptionPane.showMessageDialog(this,
                "Appointment " + selectedApptId + " approved and assigned to " + techSelected + ".",
                "Approved", JOptionPane.INFORMATION_MESSAGE);
            loadTable();
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to approve appointment. Please try again.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─── Reject ───────────────────────────────────────────────────────────────
    private void handleReject() {
        if (selectedApptId == null) return;

        int c = JOptionPane.showConfirmDialog(this,
            "Reject appointment " + selectedApptId + "? This cannot be undone.",
            "Confirm Reject", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c != JOptionPane.YES_OPTION) return;

        boolean ok = appointmentDAO.rejectAppointment(selectedApptId);
        if (ok) {
            JOptionPane.showMessageDialog(this,
                "Appointment " + selectedApptId + " has been rejected.",
                "Rejected", JOptionPane.INFORMATION_MESSAGE);
            loadTable();
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to reject appointment. Please try again.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─── Cancel (Approved → Cancelled, frees technician) ─────────────────────
    private void handleCancel() {
        if (selectedApptId == null) return;

        int c = JOptionPane.showConfirmDialog(this,
            "Cancel appointment " + selectedApptId + "?\n"
            + "The assigned technician will be marked Available again.",
            "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c != JOptionPane.YES_OPTION) return;

        // Grab tech ID before writing the cancel
        Appointment appt = appointmentDAO.getAllAppointments().stream()
            .filter(a -> a.getAppointmentId().equals(selectedApptId))
            .findFirst().orElse(null);

        boolean ok = appointmentDAO.cancelAppointment(selectedApptId);
        if (ok) {
            if (appt != null && appt.getTechnicianId() != null
                    && !appt.getTechnicianId().equalsIgnoreCase("null")
                    && !appt.getTechnicianId().trim().isEmpty()) {
                technicianDAO.updateWorkStatus(appt.getTechnicianId(), "Available");
            }
            JOptionPane.showMessageDialog(this,
                "Appointment " + selectedApptId + " has been cancelled.",
                "Cancelled", JOptionPane.INFORMATION_MESSAGE);
            loadTable();
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to cancel appointment. Please try again.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─── Load / reload table ──────────────────────────────────────────────────
    private void loadTable() {
        tableModel.setRowCount(0);
        selectedApptId = null;
        showCard("hint"); // always reset to hint on reload

        String statusFilter = (String) filterStatus.getSelectedItem();
        String dateFilter   = filterDate.getText().trim();
        String searchRaw   = searchField.getText().trim();
        String searchQuery = (searchRaw.startsWith("🔍") || searchRaw.isEmpty()) ? "" : searchRaw.toLowerCase();

        // Normalise whatever the picker returns → yyyy-MM-dd for comparison
        String dateFilterISO = "";
        if (!dateFilter.isEmpty()) {
            // Try yyyy-MM-dd first (picker native format)
            try {
                LocalDate.parse(dateFilter, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                dateFilterISO = dateFilter; // already correct format
            } catch (Exception e1) {
                // Try dd/MM/yyyy fallback
                try {
                    LocalDate picked = LocalDate.parse(dateFilter,
                        DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    dateFilterISO = picked.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } catch (Exception e2) {
                    // Try MM/dd/yyyy fallback
                    try {
                        LocalDate picked = LocalDate.parse(dateFilter,
                            DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                        dateFilterISO = picked.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    } catch (Exception ignored) {}
                }
            }
        }
        final String isoDate = dateFilterISO;

        List<Appointment> list = appointmentDAO.getAllAppointments();
        int count = 0;

        for (Appointment a : list) {
            boolean matchStatus = statusFilter.equals("All Status")
                || a.getStatus().equalsIgnoreCase(statusFilter);
            boolean matchDate = isoDate.isEmpty()
                || a.getAppointmentDate().equals(isoDate);
            if (!matchStatus || !matchDate) continue;

            String customerName = resolveCustomerName(a.getCustomerId());
            String vehicle = a.getVehicleBrand() + " " + a.getVehicleModel()
                + " (" + a.getVehiclePlate() + ")";
            String service = "ST001".equals(a.getServiceTypeId())
                ? "Normal Service — 1hour"
                : "Major Service — 3hours";

            // Search: match Appt ID, Customer Name, or Vehicle Plate (case-insensitive)
            if (!searchQuery.isEmpty()) {
                boolean matchSearch =
                    a.getAppointmentId().toLowerCase().contains(searchQuery)
                    || customerName.toLowerCase().contains(searchQuery)
                    || a.getVehiclePlate().toLowerCase().contains(searchQuery);
                if (!matchSearch) continue;
            }

            tableModel.addRow(new Object[]{
                a.getAppointmentId(),
                customerName,
                service,
                vehicle,
                a.getAppointmentDate(),
                a.getTimeSlot(),
                a.getStatus()
            });
            count++;
        }

        int pending = (int) list.stream()
            .filter(a -> a.getStatus().equalsIgnoreCase("Pending")).count();
        countLabel.setText("Showing " + count + " records  |  " + pending + " Pending");
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private String resolveCustomerName(String customerId) {
        try {
            models.dao.CustomerDAO cDao = new models.dao.CustomerDAO();
            for (String[] c : cDao.getAllCustomers()) {
                if (c[0].trim().equals(customerId)) {
                    return c[2].trim(); // index 2 = customer_name
                }
            }
            return customerId; // fallback if not found
        } catch (Exception e) {
            return customerId;
        }
    }

    private String resolveTechDisplay(String techId) {
        if (techId == null || techId.equalsIgnoreCase("null") || techId.trim().isEmpty()) return "—";
        try {
            return technicianDAO.getAllTechnicians().stream()
                .filter(t -> t.getTechnicianId().equals(techId))
                .map(t -> t.getTechnicianId() + " — " + t.getUsername())
                .findFirst().orElse(techId);
        } catch (Exception e) {
            return techId;
        }
    }

    // Build a consistent 2-column label/value grid with row separators
    private JPanel buildInfoGrid(String[] fieldNames, JLabel[] valueLabels) {
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        for (int i = 0; i < fieldNames.length; i++) {
            JLabel lbl = new JLabel(fieldNames[i]);
            lbl.setFont(AppTheme.FONT_SMALL_BOLD);
            lbl.setForeground(AppTheme.TEXT_SECONDARY);
            lbl.setPreferredSize(new Dimension(180, 26));

            valueLabels[i].setFont(AppTheme.FONT_SMALL);
            valueLabels[i].setForeground(AppTheme.TEXT_PRIMARY);

            gbc.gridx = 0; gbc.gridy = i * 2; gbc.weightx = 0;
            gbc.insets = new Insets(i == 0 ? 0 : 10, 0, 2, 16);
            grid.add(lbl, gbc);

            gbc.gridx = 1; gbc.weightx = 1;
            grid.add(valueLabels[i], gbc);

            // Separator line between rows (not after the last)
            if (i < fieldNames.length - 1) {
                JSeparator rowSep = new JSeparator(SwingConstants.HORIZONTAL);
                rowSep.setForeground(AppTheme.SEPARATOR);
                rowSep.setPreferredSize(new Dimension(0, 1));
                GridBagConstraints sepGbc = new GridBagConstraints();
                sepGbc.gridx = 0; sepGbc.gridy = i * 2 + 1;
                sepGbc.gridwidth = 2;
                sepGbc.fill    = GridBagConstraints.HORIZONTAL;
                sepGbc.weightx = 1;
                sepGbc.insets  = new Insets(6, 0, 0, 0);
                grid.add(rowSep, sepGbc);
            }
        }
        return grid;
    }


}