package views.manager;

import models.dao.*;
import models.entities.*;
import views.MainFrame;
import views.shared.AppTheme;
import views.shared.UIComponent;

import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

public class MonthlyReportsPage extends JPanel {

    // ── DAOs ─────────────────────────────────────────────────────────────────
    private final AppointmentDAO  appointmentDAO  = new AppointmentDAO();
    private final PaymentDAO      paymentDAO      = new PaymentDAO();
    private final CustomerDAO     customerDAO     = new CustomerDAO();
    private final TechnicianDAO   technicianDAO   = new TechnicianDAO();
    private final ManageEmployeeDAO staffDAO       = new ManageEmployeeDAO();
    private final FeedbackDAO     feedbackDAO     = new FeedbackDAO();

    // ── Filter ────────────────────────────────────────────────────────────────
    private JComboBox<String> monthCombo;
    private JComboBox<String> yearCombo;

    // ── Stat card labels ──────────────────────────────────────────────────────
    private JLabel lblTotalAppt, lblTotalRevenue, lblTechRating,
                   lblStaffRating, lblTotalCustomers, lblNewCustomers;

    // ── Status panel labels ───────────────────────────────────────────────────
    private JLabel lblCompleted, lblPending, lblInProgress,
    			   lblPaid, lblFreqCustomer;

    // ── Revenue bars ──────────────────────────────────────────────────────────
    private JPanel revenueBarsPanel;

    // ── Employee table ────────────────────────────────────────────────────────
    private DefaultTableModel empTableModel;
    private JTable            empTable;

    private static final String[] EMP_COLS = {
        "Name", "Role", "Appts Served", "Avg Rating", "Stars"
    };

    // =========================================================================
    // Constructor
    // =========================================================================
    public MonthlyReportsPage(MainFrame frame) {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_CONTENT);
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
    }

    // =========================================================================
    // Header
    // =========================================================================
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppTheme.BG_HEADER);
        header.setPreferredSize(new Dimension(0, AppTheme.HEADER_HEIGHT));
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER_HEADER),
            BorderFactory.createEmptyBorder(0, AppTheme.PADDING_PAGE, 0, AppTheme.PADDING_PAGE)
        ));
        header.add(UIComponent.createPageTitle("Monthly Reports"), BorderLayout.WEST);
        return header;
    }

    // =========================================================================
    // Body
    // =========================================================================
    private JScrollPane buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(AppTheme.BG_CONTENT);
        body.setBorder(BorderFactory.createEmptyBorder(20, AppTheme.PADDING_PAGE,
                                                        20, AppTheme.PADDING_PAGE));

        body.add(buildFilterBar());
        body.add(UIComponent.verticalGap(16));
        body.add(buildStatCards());
        body.add(UIComponent.verticalGap(14));
        body.add(buildMidRow());
        body.add(UIComponent.verticalGap(14));
        body.add(buildEmployeeTable());

        loadData();
        return UIComponent.createAppleScrollPane(body);
    }

    // =========================================================================
    // Filter bar
    // =========================================================================
    private JPanel buildFilterBar() {
        JPanel card = UIComponent.createCard();
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
        card.setBorder(BorderFactory.createEmptyBorder(10, AppTheme.PADDING_CARD,
                                                        10, AppTheme.PADDING_CARD));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        JLabel lblMonth = new JLabel("Month:");
        lblMonth.setFont(AppTheme.FONT_SMALL);
        lblMonth.setForeground(AppTheme.TEXT_SECONDARY);

        String[] months = {"January","February","March","April","May","June",
                           "July","August","September","October","November","December"};
        monthCombo = UIComponent.createComboBox(months);
        monthCombo.setPreferredSize(new Dimension(130, 34));
        monthCombo.setSelectedIndex(LocalDate.now().getMonthValue() - 1);

        JLabel lblYear = new JLabel("Year:");
        lblYear.setFont(AppTheme.FONT_SMALL);
        lblYear.setForeground(AppTheme.TEXT_SECONDARY);

        int currentYear = LocalDate.now().getYear();
        String[] years = new String[5];
        for (int i = 0; i < 5; i++) years[i] = String.valueOf(currentYear - i);
        yearCombo = UIComponent.createComboBox(years);
        yearCombo.setPreferredSize(new Dimension(120, 34));

        JButton btnGenerate = UIComponent.createSmallButton("Generate");
        btnGenerate.setPreferredSize(new Dimension(90, 34));
        btnGenerate.addActionListener(e -> loadData());

        JButton btnExport = UIComponent.createSmallButton("Export Document");
        btnExport.setPreferredSize(new Dimension(140, 34));
        btnExport.setBackground(new Color(34, 139, 34));
        btnExport.setForeground(Color.BLACK);
        btnExport.addActionListener(e -> exportToWord());

        card.add(lblMonth);
        card.add(monthCombo);
        card.add(lblYear);
        card.add(yearCombo);
        card.add(btnGenerate);
        card.add(btnExport);

        return card;
    }

    // =========================================================================
    // Stat cards  (2 rows × 3)
    // =========================================================================
    private JPanel buildStatCards() {
        JPanel grid = new JPanel(new GridLayout(1, 6, AppTheme.GAP_ITEM, 0));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblTotalAppt      = new JLabel("—");
        lblTotalRevenue   = new JLabel("—");
        lblTechRating     = new JLabel("—");
        lblStaffRating    = new JLabel("—");
        lblTotalCustomers = new JLabel("—");
        lblNewCustomers   = new JLabel("—");

        grid.add(buildStatCard("Total Appointments",      lblTotalAppt,      AppTheme.PRIMARY));
        grid.add(buildStatCard("Revenue (RM)",            lblTotalRevenue,   AppTheme.SUCCESS));
        grid.add(buildStatCard("Avg Tech Rating",         lblTechRating,     AppTheme.WARNING));
        grid.add(buildStatCard("Avg Staff Rating",        lblStaffRating,    new Color(180, 80, 180)));
        grid.add(buildStatCard("Total Customers",         lblTotalCustomers, AppTheme.TEXT_SECONDARY));
        grid.add(buildStatCard("New Customers (Month)",   lblNewCustomers,   AppTheme.PRIMARY));

        return grid;
    }

    private JPanel buildStatCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = UIComponent.createCard();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(12, AppTheme.PADDING_CARD,
                                                        12, AppTheme.PADDING_CARD));
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(AppTheme.FONT_LABEL);
        lblTitle.setForeground(AppTheme.TEXT_SECONDARY);

        valueLabel.setFont(AppTheme.FONT_HEADING_2);
        valueLabel.setForeground(accent);

        card.add(lblTitle,   BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // Mid row — Appointments by Status  |  Revenue by Service
    // =========================================================================
    private JPanel buildMidRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, AppTheme.GAP_ITEM, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));

        row.add(buildStatusCard());
        row.add(buildRevenueCard());
        return row;
    }

    // ── Left: appointments by status ─────────────────────────────────────────
    private JPanel buildStatusCard() {
        JPanel card = UIComponent.createCard();
        card.setLayout(new BorderLayout(0, 0));

        JLabel title = new JLabel("Appointments by Status");
        title.setFont(AppTheme.FONT_SMALL_BOLD);
        title.setForeground(AppTheme.TEXT_SECONDARY);
        title.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.SEPARATOR),
            BorderFactory.createEmptyBorder(10, AppTheme.PADDING_CARD,
                                             10, AppTheme.PADDING_CARD)
        ));
        card.add(title, BorderLayout.NORTH);

        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setOpaque(false);
        rows.setBorder(BorderFactory.createEmptyBorder(8, AppTheme.PADDING_CARD,
                                                        8, AppTheme.PADDING_CARD));

        lblCompleted  = new JLabel("0"); lblPending    = new JLabel("0");
        lblInProgress = new JLabel("0"); lblPaid  = new JLabel("0");
    	lblFreqCustomer = new JLabel("—");

        rows.add(buildStatusRow("Completed",   lblCompleted,  AppTheme.STATUS_COMPLETED_BG,  AppTheme.STATUS_COMPLETED_FG));
        rows.add(buildStatusRow("Paid",        lblPaid,       new Color(220, 240, 220),       new Color(30, 120, 30)));
        rows.add(buildStatusRow("Pending",     lblPending,    AppTheme.STATUS_PENDING_BG,    AppTheme.STATUS_PENDING_FG));
        rows.add(buildStatusRow("In Progress", lblInProgress, AppTheme.STATUS_INPROGRESS_BG, AppTheme.STATUS_INPROGRESS_FG));

        // Divider + most frequent customer
        JPanel divider = new JPanel();
        divider.setOpaque(false);
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        divider.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.SEPARATOR));
        rows.add(UIComponent.verticalGap(6));
        rows.add(divider);
        rows.add(UIComponent.verticalGap(6));

        JPanel freqRow = new JPanel(new BorderLayout());
        freqRow.setOpaque(false);
        freqRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        JLabel freqLbl = new JLabel("Most frequent customer");
        freqLbl.setFont(AppTheme.FONT_SMALL);
        freqLbl.setForeground(AppTheme.TEXT_SECONDARY);
        lblFreqCustomer.setFont(AppTheme.FONT_SMALL_BOLD);
        lblFreqCustomer.setForeground(AppTheme.TEXT_PRIMARY);
        freqRow.add(freqLbl,          BorderLayout.WEST);
        freqRow.add(lblFreqCustomer,  BorderLayout.EAST);
        rows.add(freqRow);

        card.add(rows, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildStatusRow(String label, JLabel countLabel, Color pillBg, Color pillFg) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JLabel lbl = new JLabel(label);
        lbl.setFont(AppTheme.FONT_SMALL);
        lbl.setForeground(AppTheme.TEXT_PRIMARY);

        final Color bg = pillBg;
        final Color fg = pillFg;
        JLabel pill = new JLabel("0", SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pill.setFont(AppTheme.FONT_LABEL);
        pill.setForeground(fg);
        pill.setOpaque(false);
        pill.setPreferredSize(new Dimension(42, 20));
        pill.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));

        // wire the external label into our pill display
        countLabel.setFont(AppTheme.FONT_LABEL);
        countLabel.setForeground(fg);

        // replace pill text dynamically — we'll update countLabel text in loadData
        // and sync pill in refreshStatusPills(); store pill ref in countLabel's client property
        countLabel.putClientProperty("pill", pill);

        row.add(lbl,  BorderLayout.WEST);
        row.add(pill, BorderLayout.EAST);
        return row;
    }

    // ── Right: revenue by service ─────────────────────────────────────────────
    private JPanel buildRevenueCard() {
        JPanel card = UIComponent.createCard();
        card.setLayout(new BorderLayout(0, 0));

        JLabel title = new JLabel("Revenue by Service Item");
        title.setFont(AppTheme.FONT_SMALL_BOLD);
        title.setForeground(AppTheme.TEXT_SECONDARY);
        title.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.SEPARATOR),
            BorderFactory.createEmptyBorder(10, AppTheme.PADDING_CARD,
                                             10, AppTheme.PADDING_CARD)
        ));
        card.add(title, BorderLayout.NORTH);

        revenueBarsPanel = new JPanel();
        revenueBarsPanel.setLayout(new BoxLayout(revenueBarsPanel, BoxLayout.Y_AXIS));
        revenueBarsPanel.setOpaque(false);
        revenueBarsPanel.setBorder(BorderFactory.createEmptyBorder(10, AppTheme.PADDING_CARD,
                                                                     10, AppTheme.PADDING_CARD));
        card.add(revenueBarsPanel, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // Employee performance table  (JLayer rounded corners)
    // =========================================================================
    private JPanel buildEmployeeTable() {
        empTableModel = new DefaultTableModel(EMP_COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        empTable = new JTable(empTableModel);
        empTable.setRowHeight(32);
        empTable.setFont(AppTheme.FONT_SMALL);
        empTable.setForeground(AppTheme.TEXT_PRIMARY);
        empTable.setBackground(AppTheme.BG_CARD);
        empTable.setShowGrid(false);
        empTable.setIntercellSpacing(new Dimension(0, 0));
        empTable.setFillsViewportHeight(true);
        empTable.setSelectionBackground(AppTheme.PRIMARY_LIGHT);
        empTable.setSelectionForeground(AppTheme.TEXT_PRIMARY);
        empTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        final int[] hoveredRow = {-1};
        empTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int r = empTable.rowAtPoint(e.getPoint());
                if (r != hoveredRow[0]) { hoveredRow[0] = r; empTable.repaint(); }
            }
        });
        empTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) { hoveredRow[0] = -1; empTable.repaint(); }
        });

        final Color ROW_ODD   = AppTheme.BG_CARD;
        final Color ROW_EVEN  = new Color(248, 248, 251);
        final Color ROW_HOVER = new Color(235, 242, 255);

        // Default renderer
        empTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setFont(AppTheme.FONT_SMALL);
                setForeground(AppTheme.TEXT_PRIMARY);
                setBackground(sel ? AppTheme.PRIMARY_LIGHT
                    : hoveredRow[0] == row ? ROW_HOVER
                    : row % 2 == 0 ? ROW_ODD : ROW_EVEN);
                setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 8));
                return this;
            }
        });

        // Role pill renderer (col 1)
        empTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                Color rowBg = sel ? AppTheme.PRIMARY_LIGHT
                    : hoveredRow[0] == row ? ROW_HOVER
                    : row % 2 == 0 ? ROW_ODD : ROW_EVEN;

                JPanel cell = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
                cell.setBackground(rowBg);
                cell.setOpaque(true);

                String text = val == null ? "" : val.toString();
                boolean isTech = text.equals("Technician");
                Color pillBg = isTech ? new Color(230, 241, 251) : new Color(250, 238, 218);
                Color pillFg = isTech ? new Color(24, 95, 165)   : new Color(133, 79, 11);
                final Color bg = pillBg;

                JLabel pill = new JLabel(text) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(bg);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                pill.setFont(AppTheme.FONT_LABEL);
                pill.setForeground(pillFg);
                pill.setOpaque(false);
                pill.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
                cell.add(pill);
                return cell;
            }
        });

        // Stars renderer (col 4)
        empTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setText(val == null ? "" : val.toString());
                setFont(AppTheme.FONT_SMALL);
                setForeground(AppTheme.WARNING);
                setBackground(sel ? AppTheme.PRIMARY_LIGHT
                    : hoveredRow[0] == row ? ROW_HOVER
                    : row % 2 == 0 ? ROW_ODD : ROW_EVEN);
                setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 8));
                return this;
            }
        });

        // Avg Rating color (col 3)
        empTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                String s = val == null ? "—" : val.toString();
                setText(s);
                setFont(AppTheme.FONT_SMALL_BOLD);
                Color fg = AppTheme.TEXT_MUTED;
                if (!s.equals("—")) {
                    try {
                        double r2 = Double.parseDouble(s.replace(" / 5", "").trim());
                        fg = r2 >= 4 ? AppTheme.SUCCESS : r2 >= 3 ? AppTheme.WARNING : AppTheme.DANGER;
                    } catch (NumberFormatException ignored) {}
                }
                setForeground(fg);
                setBackground(sel ? AppTheme.PRIMARY_LIGHT
                    : hoveredRow[0] == row ? ROW_HOVER
                    : row % 2 == 0 ? ROW_ODD : ROW_EVEN);
                setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 8));
                return this;
            }
        });

        // Header
        empTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                JLabel lbl = new JLabel(val == null ? "" : val.toString().toUpperCase());
                lbl.setFont(AppTheme.FONT_LABEL);
                lbl.setForeground(AppTheme.TEXT_SECONDARY);
                lbl.setBackground(new Color(245, 245, 248));
                lbl.setOpaque(true);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.SEPARATOR),
                    BorderFactory.createEmptyBorder(0, 16, 0, 8)
                ));
                return lbl;
            }
        });
        empTable.getTableHeader().setPreferredSize(new Dimension(0, 32));
        empTable.getTableHeader().setReorderingAllowed(false);
        empTable.getTableHeader().setResizingAllowed(false);

        // Column widths
        int[] widths = {220, 130, 130, 120, 120};
        for (int i = 0; i < widths.length; i++)
            empTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // JLayer rounded corners
        JScrollPane scroll = new JScrollPane(empTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(true);
        scroll.setBackground(AppTheme.BG_CARD);
        scroll.getViewport().setOpaque(true);
        scroll.getViewport().setBackground(AppTheme.BG_CARD);
        scroll.getViewport().setBorder(null);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JTableHeader tableHeader = empTable.getTableHeader();
        tableHeader.setOpaque(true);
        tableHeader.setBackground(new Color(245, 245, 248));
        scroll.setColumnHeader(null);

        JPanel inner = new JPanel(new BorderLayout(0, 0));
        inner.setOpaque(true);
        inner.setBackground(AppTheme.BG_CARD);

        // Section title bar
        JLabel sectionTitle = new JLabel("Employee Performance");
        sectionTitle.setFont(AppTheme.FONT_SMALL_BOLD);
        sectionTitle.setForeground(AppTheme.TEXT_SECONDARY);
        sectionTitle.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.SEPARATOR),
            BorderFactory.createEmptyBorder(10, AppTheme.PADDING_CARD,
                                             10, AppTheme.PADDING_CARD)
        ));

        JPanel tableTop = new JPanel(new BorderLayout());
        tableTop.setOpaque(false);
        tableTop.add(sectionTitle,  BorderLayout.NORTH);
        tableTop.add(tableHeader,   BorderLayout.CENTER);

        inner.add(tableTop,  BorderLayout.NORTH);
        inner.add(scroll,    BorderLayout.CENTER);

        final int rad = AppTheme.RADIUS_CARD * 2;
        LayerUI<JPanel> layerUI = new LayerUI<JPanel>() {
            @Override public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.BG_CARD);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), rad, rad);
                g2.setClip(new java.awt.geom.RoundRectangle2D.Float(
                    0, 0, c.getWidth(), c.getHeight(), rad, rad));
                super.paint(g2, c);
                g2.setClip(null);
                g2.setColor(AppTheme.SEPARATOR);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, rad, rad);
                g2.dispose();
            }
        };

        JLayer<JPanel> card = new JLayer<>(inner, layerUI);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    // =========================================================================
    // loadData  — public so ManagerDashboard can call on nav
    // =========================================================================
    public void loadData() {
        String monthName = (String) monthCombo.getSelectedItem();
        String yearStr   = (String) yearCombo.getSelectedItem();

        int month = Arrays.asList("January","February","March","April","May","June",
                                  "July","August","September","October","November","December")
                          .indexOf(monthName) + 1;
        int year  = Integer.parseInt(yearStr);
        String prefix = String.format("%04d-%02d", year, month); // e.g. "2026-05"

        // ── Load raw data ──────────────────────────────────────────────────
        List<Appointment> allAppts    = appointmentDAO.getAllAppointments();
        List<Payment>     allPayments = paymentDAO.getAllPayments();
        List<String[]>    allCustomers= customerDAO.getAllCustomers();

        // Filter appointments to selected month
        List<Appointment> monthAppts = allAppts.stream()
            .filter(a -> a.getAppointmentDate() != null &&
                         a.getAppointmentDate().startsWith(prefix))
            .collect(Collectors.toList());

        // Filter payments to selected month
        List<Payment> monthPayments = allPayments.stream()
            .filter(p -> p.getPaymentDate() != null &&
                         p.getPaymentDate().startsWith(prefix))
            .collect(Collectors.toList());

        // ── Stat cards ─────────────────────────────────────────────────────

        // Total appointments this month
        lblTotalAppt.setText(String.valueOf(monthAppts.size()));

        // Total revenue this month
        double totalRev = monthPayments.stream()
            .mapToDouble(p -> { try { return Double.parseDouble(p.getTotalAmount()); }
                                catch (Exception e) { return 0; } })
            .sum();
        lblTotalRevenue.setText(String.format("%.0f", totalRev));

        // Avg ratings (all-time from feedback, filtered by month appointments)
        Set<String> monthApptIds = monthAppts.stream()
            .map(Appointment::getAppointmentId)
            .collect(Collectors.toSet());
        List<FeedbackDAO.FeedbackRecord> allFeedback = feedbackDAO.getAllFeedbacks();
        List<FeedbackDAO.FeedbackRecord> monthFeedback = allFeedback.stream()
            .filter(f -> monthApptIds.contains(f.appointmentId))
            .collect(Collectors.toList());

        double techSum = 0; int techCount = 0;
        double staffSum = 0; int staffCount = 0;
        for (FeedbackDAO.FeedbackRecord fb : monthFeedback) {
            try {
                double r = Double.parseDouble(fb.rating);
                if ("TECHNICIAN".equalsIgnoreCase(fb.targetType)) { techSum += r; techCount++; }
                else if ("STAFF".equalsIgnoreCase(fb.targetType)) { staffSum += r; staffCount++; }
            } catch (NumberFormatException ignored) {}
        }
        lblTechRating.setText(techCount  == 0 ? "N/A" : String.format("%.1f / 5", techSum  / techCount));
        lblStaffRating.setText(staffCount == 0 ? "N/A" : String.format("%.1f / 5", staffSum / staffCount));

        // Total customers (all-time)
        lblTotalCustomers.setText(String.valueOf(allCustomers.size()));

        // New customers this month (register_date col 6)
        long newCust = allCustomers.stream()
            .filter(c -> c.length >= 7 && c[6] != null && c[6].trim().startsWith(prefix))
            .count();
        lblNewCustomers.setText(String.valueOf(newCust));

        // ── Status counts ──────────────────────────────────────────────────
        Map<String, Long> statusCounts = monthAppts.stream()
            .collect(Collectors.groupingBy(a -> a.getStatus().trim(), Collectors.counting()));

        updatePill(lblCompleted,  statusCounts.getOrDefault("Completed",  0L));
        updatePill(lblPaid,       statusCounts.getOrDefault("Paid",       0L));
        updatePill(lblPending,    statusCounts.getOrDefault("Pending",    0L));
        updatePill(lblInProgress, statusCounts.getOrDefault("Approved",   0L));

        // Most frequent customer this month
        Map<String, Long> custFreq = monthAppts.stream()
            .filter(a -> a.getCustomerId() != null && !a.getCustomerId().isEmpty())
            .collect(Collectors.groupingBy(Appointment::getCustomerId, Collectors.counting()));
        if (!custFreq.isEmpty()) {
            String topId = Collections.max(custFreq.entrySet(), Map.Entry.comparingByValue()).getKey();
            long   topCount = custFreq.get(topId);
            String topName = allCustomers.stream()
                .filter(c -> c[0].trim().equals(topId))
                .map(c -> c[2].trim())
                .findFirst().orElse(topId);
            lblFreqCustomer.setText(topName + " (" + topCount + ")");
        } else {
            lblFreqCustomer.setText("—");
        }

        // ── Revenue by service ─────────────────────────────────────────────
        // Map appointment_id → service_item_id for this month
        Map<String, String> apptToService = monthAppts.stream()
            .collect(Collectors.toMap(Appointment::getAppointmentId,
                                      Appointment::getServiceItemId, (a, b) -> a));

        Map<String, Double> revenueByService = new LinkedHashMap<>();
        for (Payment p : monthPayments) {
            String svcId = apptToService.get(p.getAppointmentId());
            if (svcId == null) svcId = "Unknown";
            String svcName = ServiceItemDAO.getServiceName(svcId);
            String display = svcName.equals(svcId) ? svcId : svcName;
            try {
                double amt = Double.parseDouble(p.getTotalAmount());
                revenueByService.merge(display, amt, Double::sum);
            } catch (NumberFormatException ignored) {}
        }
        refreshRevenueBars(revenueByService.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(5)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                      (a, b) -> a, LinkedHashMap::new)));

        // ── Employee performance ───────────────────────────────────────────
        empTableModel.setRowCount(0);

        // Technicians
        for (Technician tech : technicianDAO.getAllTechnicians()) {
            long served = monthAppts.stream()
        		.filter(a -> tech.getTechnicianId().equals(a.getTechnicianId())
        		          && (   "Completed".equalsIgnoreCase(a.getStatus())
        		              || "Paid".equalsIgnoreCase(a.getStatus())))
                .count();
            if (served == 0) continue;

            double rSum = 0; int rCount = 0;
            for (FeedbackDAO.FeedbackRecord fb : monthFeedback) {
                if ("TECHNICIAN".equalsIgnoreCase(fb.targetType)
                        && tech.getTechnicianId().equals(fb.targetId)) {
                    try { rSum += Double.parseDouble(fb.rating); rCount++; }
                    catch (NumberFormatException ignored) {}
                }
            }
            String avgStr = rCount == 0 ? "—" : String.format("%.1f / 5", rSum / rCount);
            String stars  = rCount == 0 ? "—" : makeStars(rSum / rCount);

            empTableModel.addRow(new Object[]{
                tech.getUsername(), "Technician", served, avgStr, stars
            });
        }

        // Counter Staff
        for (ManageEmployee staff : staffDAO.getAllCounterStaff()) {
            long served = monthAppts.stream()
        		.filter(a -> staff.getId().equals(a.getStaffId())
        		          && (   "Completed".equalsIgnoreCase(a.getStatus())
        		              || "Paid".equalsIgnoreCase(a.getStatus())))
                .count();
            if (served == 0) continue;

            double rSum = 0; int rCount = 0;
            for (FeedbackDAO.FeedbackRecord fb : monthFeedback) {
                if ("STAFF".equalsIgnoreCase(fb.targetType)
                        && staff.getId().equals(fb.targetId)) {
                    try { rSum += Double.parseDouble(fb.rating); rCount++; }
                    catch (NumberFormatException ignored) {}
                }
            }
            String avgStr = rCount == 0 ? "—" : String.format("%.1f / 5", rSum / rCount);
            String stars  = rCount == 0 ? "—" : makeStars(rSum / rCount);

            empTableModel.addRow(new Object[]{
                staff.getUsername(), "Counter Staff", served, avgStr, stars
            });
        }

        // Sort by appointments served descending
        sortEmpTable();
    }

    // =========================================================================
    // Revenue bar chart refresh
    // =========================================================================
    private void refreshRevenueBars(Map<String, Double> data) {
        revenueBarsPanel.removeAll();

        if (data.isEmpty()) {
            JLabel empty = new JLabel("No payment data for this month.");
            empty.setFont(AppTheme.FONT_SMALL);
            empty.setForeground(AppTheme.TEXT_MUTED);
            revenueBarsPanel.add(empty);
            revenueBarsPanel.revalidate();
            revenueBarsPanel.repaint();
            return;
        }

        double maxVal = data.values().stream().mapToDouble(Double::doubleValue).max().orElse(1);

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            JPanel barRow = new JPanel(new BorderLayout(8, 0));
            barRow.setOpaque(false);
            barRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

            JLabel nameLbl = new JLabel(entry.getKey());
            nameLbl.setFont(AppTheme.FONT_SMALL);
            nameLbl.setForeground(AppTheme.TEXT_SECONDARY);
            nameLbl.setPreferredSize(new Dimension(120, 20));

            double pct = entry.getValue() / maxVal;
            JPanel track = new JPanel(new BorderLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    // track
                    g2.setColor(new Color(235, 240, 248));
                    g2.fillRoundRect(0, 3, getWidth(), 8, 8, 8);
                    // fill
                    int fillW = (int)(getWidth() * pct);
                    g2.setColor(AppTheme.PRIMARY);
                    if (fillW > 0) g2.fillRoundRect(0, 3, fillW, 8, 8, 8);
                    g2.dispose();
                }
            };
            track.setOpaque(false);

            JLabel valLbl = new JLabel(String.format("RM %.0f", entry.getValue()));
            valLbl.setFont(AppTheme.FONT_LABEL);
            valLbl.setForeground(AppTheme.TEXT_SECONDARY);
            valLbl.setPreferredSize(new Dimension(72, 20));
            valLbl.setHorizontalAlignment(SwingConstants.RIGHT);

            barRow.add(nameLbl, BorderLayout.WEST);
            barRow.add(track,   BorderLayout.CENTER);
            barRow.add(valLbl,  BorderLayout.EAST);

            revenueBarsPanel.add(barRow);
            revenueBarsPanel.add(Box.createVerticalStrut(4));
        }

        revenueBarsPanel.revalidate();
        revenueBarsPanel.repaint();
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /** Syncs pill text from the countLabel's stored count. */
    private void updatePill(JLabel countLabel, long count) {
        countLabel.setText(String.valueOf(count));
        Object pillObj = countLabel.getClientProperty("pill");
        if (pillObj instanceof JLabel) {
            ((JLabel) pillObj).setText(String.valueOf(count));
        }
    }

    private String makeStars(double rating) {
        int r = (int) Math.round(rating);
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) sb.append(i <= r ? "★" : "☆");
        return sb.toString();
    }

    private void sortEmpTable() {
        int rows = empTableModel.getRowCount();
        // Simple insertion sort on col 2 (Appts Served) descending
        for (int i = 1; i < rows; i++) {
            Object[] row = new Object[EMP_COLS.length];
            for (int c = 0; c < EMP_COLS.length; c++) row[c] = empTableModel.getValueAt(i, c);
            long key = (Long) row[2];
            int j = i - 1;
            while (j >= 0 && (Long) empTableModel.getValueAt(j, 2) < key) {
                for (int c = 0; c < EMP_COLS.length; c++)
                    empTableModel.setValueAt(empTableModel.getValueAt(j, c), j + 1, c);
                j--;
            }
            for (int c = 0; c < EMP_COLS.length; c++) empTableModel.setValueAt(row[c], j + 1, c);
        }
    }

    // =========================================================================
    // Export to Word Document
    // =========================================================================
    private void exportToWord() {
        try {
            // ── Locate template ──────────────────────────────────────────────
            String templatePath = "src/template/ReportTemplate.docx";
            File templateFile = new File(templatePath);
            if (!templateFile.exists()) {
                // Fallback: look relative to classpath root
                URL url = getClass().getClassLoader().getResource("template/ReportTemplate.docx");
                if (url != null) {
                    try { templateFile = new File(url.toURI()); }
                    catch (URISyntaxException ex) { templateFile = new File(url.getPath()); }
                }
            }
            if (!templateFile.exists()) {
                JOptionPane.showMessageDialog(this,
                    "Template not found at: " + templatePath,
                    "Export Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // ── Collect current data from UI labels ──────────────────────────
            String reportMonth = (String) monthCombo.getSelectedItem();
            String reportYear  = (String) yearCombo.getSelectedItem();
            String genDate     = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));

            // Build flat placeholder map
            Map<String, String> replacements = new LinkedHashMap<>();
            replacements.put("@reportMonth",      reportMonth);
            replacements.put("@reportYear",       reportYear);
            replacements.put("@generationDate",   genDate);
            replacements.put("@totalAppt",        lblTotalAppt.getText());
            replacements.put("@totalRevenue",     lblTotalRevenue.getText());
            replacements.put("@techRating",       lblTechRating.getText());
            replacements.put("@staffRating",      lblStaffRating.getText());
            replacements.put("@totalCustomers",   lblTotalCustomers.getText());
            replacements.put("@newCustomers",     lblNewCustomers.getText());
            replacements.put("@statusCompleted",  lblCompleted.getText());
            replacements.put("@statusPaid",       lblPaid.getText());
            replacements.put("@statusPending",    lblPending.getText());
            replacements.put("@statusInProgress", lblInProgress.getText());
            replacements.put("@freqCustomer",     lblFreqCustomer.getText());

            // ── Open template with Apache POI ────────────────────────────────
            XWPFDocument doc;
            try (FileInputStream fis = new FileInputStream(templateFile)) {
                doc = new XWPFDocument(fis);
            }

            // ── Replace placeholders in all paragraphs ───────────────────────
            for (XWPFParagraph para : doc.getParagraphs()) {
                replacePlaceholdersInParagraph(para, replacements);
            }

            // ── Replace placeholders in tables ───────────────────────────────
            for (XWPFTable table : doc.getTables()) {
                for (XWPFTableRow tableRow : table.getRows()) {
                    for (XWPFTableCell cell : tableRow.getTableCells()) {
                        for (XWPFParagraph para : cell.getParagraphs()) {
                            replacePlaceholdersInParagraph(para, replacements);
                        }
                    }
                }
            }

            // ── Expand repeating table rows for Revenue by Service ────────────
            expandTableRows(doc, "@serviceName", "@serviceRevenue", buildServiceRows());

            // ── Expand repeating table rows for Employee Performance ──────────
            expandTableRows(doc, "@empName", "@empAvgRating", buildEmployeeRows());

            // ── Ask user where to save ────────────────────────────────────────
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File(
                "MonthlyReport_" + reportMonth + "_" + reportYear + ".docx"));
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Word Document (*.docx)", "docx"));
            int result = chooser.showSaveDialog(this);
            if (result != JFileChooser.APPROVE_OPTION) {
                doc.close();
                return;
            }

            File outFile = chooser.getSelectedFile();
            if (!outFile.getName().toLowerCase().endsWith(".docx")) {
                outFile = new File(outFile.getAbsolutePath() + ".docx");
            }

            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                doc.write(fos);
            }
            doc.close();

            JOptionPane.showMessageDialog(this,
                "Report exported successfully:\n" + outFile.getAbsolutePath(),
                "Export Complete", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Failed to export report:\n" + ex.getMessage(),
                "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Replaces @placeholder text within a single paragraph, preserving run formatting.
     * Consolidates all runs into one to handle cases where POI splits text across runs.
     */
    private void replacePlaceholdersInParagraph(XWPFParagraph para, Map<String, String> replacements) {
        List<XWPFRun> runs = para.getRuns();
        if (runs == null || runs.isEmpty()) return;

        // Merge all run text to detect split placeholders
        StringBuilder full = new StringBuilder();
        for (XWPFRun r : runs) {
            String t = r.getText(0);
            if (t != null) full.append(t);
        }

        String merged = full.toString();
        boolean changed = false;
        for (Map.Entry<String, String> e : replacements.entrySet()) {
            if (merged.contains(e.getKey())) {
                merged = merged.replace(e.getKey(), e.getValue() != null ? e.getValue() : "");
                changed = true;
            }
        }

        if (changed) {
            // Write merged text back into first run, clear the rest
            runs.get(0).setText(merged, 0);
            for (int i = 1; i < runs.size(); i++) {
                runs.get(i).setText("", 0);
            }
        }
    }

    /**
     * Finds the first table whose template row contains anchorStart placeholder,
     * duplicates that row for each data entry, then removes the template row.
     */
    private void expandTableRows(XWPFDocument doc, String anchorStart, String anchorEnd,
                                  List<Map<String, String>> dataRows) {
        for (XWPFTable table : doc.getTables()) {
            int templateRowIdx = -1;
            for (int i = 0; i < table.getRows().size(); i++) {
                XWPFTableRow row = table.getRow(i);
                String rowText = row.getTableCells().stream()
                    .flatMap(c -> c.getParagraphs().stream())
                    .map(p -> p.getRuns().stream()
                        .map(r -> r.getText(0) != null ? r.getText(0) : "")
                        .reduce("", String::concat))
                    .reduce("", String::concat);
                if (rowText.contains(anchorStart)) {
                    templateRowIdx = i;
                    break;
                }
            }
            if (templateRowIdx < 0) continue;

            XWPFTableRow templateRow = table.getRow(templateRowIdx);

            if (dataRows.isEmpty()) {
                // Replace placeholders with em-dash if no data
                Map<String, String> emptyMap = new LinkedHashMap<>();
                emptyMap.put(anchorStart, "—");
                emptyMap.put(anchorEnd,   "—");
                for (XWPFTableCell cell : templateRow.getTableCells()) {
                    for (XWPFParagraph para : cell.getParagraphs()) {
                        replacePlaceholdersInParagraph(para, emptyMap);
                    }
                }
            } else {
                // Insert cloned rows above the template row for each data entry
                for (int d = dataRows.size() - 1; d >= 0; d--) {
                    XWPFTableRow newRow = table.insertNewTableRow(templateRowIdx);
                    // Copy cells from template
                    for (int c = 0; c < templateRow.getTableCells().size(); c++) {
                        XWPFTableCell srcCell = templateRow.getTableCells().get(c);
                        XWPFTableCell dstCell = newRow.getCell(c);
                        if (dstCell == null) dstCell = newRow.addNewTableCell();
                        // Copy paragraph text and run formatting
                        for (int p = 0; p < srcCell.getParagraphs().size(); p++) {
                            XWPFParagraph srcPara = srcCell.getParagraphs().get(p);
                            XWPFParagraph dstPara = (p == 0)
                                ? dstCell.getParagraphs().get(0)
                                : dstCell.addParagraph();
                            for (XWPFRun srcRun : srcPara.getRuns()) {
                                XWPFRun dstRun = dstPara.createRun();
                                String txt = srcRun.getText(0);
                                dstRun.setText(txt != null ? txt : "", 0);
                                dstRun.setBold(srcRun.isBold());
                                dstRun.setItalic(srcRun.isItalic());
                                
                                int fontSize = srcRun.getFontSize();
                                if (fontSize != -1) {
                                    dstRun.setFontSize(fontSize);
                                }
                                
                                if (srcRun.getFontFamily() != null)
                                    dstRun.setFontFamily(srcRun.getFontFamily());
                                if (srcRun.getColor() != null)
                                    dstRun.setColor(srcRun.getColor());
                            }
                        }
                        // Replace placeholders in the new cell
                        for (XWPFParagraph para : dstCell.getParagraphs()) {
                            replacePlaceholdersInParagraph(para, dataRows.get(d));
                        }
                    }
                }
                // Remove original template row
                table.removeRow(templateRowIdx + dataRows.size());
            }
            break; // Only process first matching table
        }
    }

    /** Builds service revenue data from the revenue bars panel's current state. */
    private List<Map<String, String>> buildServiceRows() {
        // Re-derive from revenue bars panel labels — iterate its components
        List<Map<String, String>> rows = new ArrayList<>();
        Component[] comps = revenueBarsPanel.getComponents();
        for (Component comp : comps) {
            if (comp instanceof JPanel) {
                JPanel barRow = (JPanel) comp;
                Component[] inner = barRow.getComponents();
                if (inner.length >= 2) {
                    String name = inner[0] instanceof JLabel ? ((JLabel) inner[0]).getText() : "";
                    String rev  = "";
                    // Last component is the value label (e.g. "RM 350")
                    Component last = inner[inner.length - 1];
                    if (last instanceof JLabel) {
                        rev = ((JLabel) last).getText().replace("RM ", "").trim();
                    }
                    if (!name.isEmpty()) {
                        Map<String, String> row = new LinkedHashMap<>();
                        row.put("@serviceName",    name);
                        row.put("@serviceRevenue", rev);
                        rows.add(row);
                    }
                }
            }
        }
        return rows;
    }

    /** Builds employee performance data from the employee table model. */
    private List<Map<String, String>> buildEmployeeRows() {
        List<Map<String, String>> rows = new ArrayList<>();
        for (int r = 0; r < empTableModel.getRowCount(); r++) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("@empName",      String.valueOf(empTableModel.getValueAt(r, 0)));
            row.put("@empRole",      String.valueOf(empTableModel.getValueAt(r, 1)));
            row.put("@empServed",    String.valueOf(empTableModel.getValueAt(r, 2)));
            row.put("@empAvgRating", String.valueOf(empTableModel.getValueAt(r, 3)));
            rows.add(row);
        }
        return rows;
    }
}