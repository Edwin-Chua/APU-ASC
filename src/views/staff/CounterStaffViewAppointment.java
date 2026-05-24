package views.staff;

import models.dao.AppointmentDAO;
import models.dao.TechnicianDAO;
import models.entities.Appointment;
import models.entities.Technician;
import views.MainFrame;
import views.shared.AppTheme;
import views.shared.UIComponent;

import javax.swing.*;
import javax.swing.plaf.LayerUI;
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

    private static final String[] COLS = {
        "Appt ID", "Customer Name", "Service Requested", "Vehicle", "Date", "Time", "Status"
    };

    // ─── Toolbar ──────────────────────────────────────────────────────────────
    private JTextField                  searchField;
    private JComboBox<String>           filterStatus;
    private UIComponent.DatePickerField filterDate;
    private JLabel                      countLabel;

    // ─── Table ────────────────────────────────────────────────────────────────
    private DefaultTableModel tableModel;
    private JTable            table;
    private JScrollPane       tableScrollPane;
    private JPanel            tableInnerPanel;  // inner BorderLayout panel inside JLayer

    // ─── Bottom card (original style) ─────────────────────────────────────────
    private JPanel bodyPanel;   // CardLayout: "hint" | "pending" | "approved" | "view"
    private JPanel bottomCard;  // the whole card wrapper — hidden until row selected

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

    // ─── State ────────────────────────────────────────────────────────────────
    private String selectedApptId = null;
    private String staffId        = "CS001";
    private Runnable onDataChanged;    
    public void setOnDataChanged(Runnable callback) {
        this.onDataChanged = callback;
    }

    // ─── Constructor ──────────────────────────────────────────────────────────
    public CounterStaffViewAppointment(MainFrame frame) {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_PAGE);
        setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_PAGE, AppTheme.PADDING_PAGE,
            AppTheme.PADDING_PAGE, AppTheme.PADDING_PAGE));

        add(buildContent(), BorderLayout.CENTER);
        loadTable();
    }

    public void setStaffId(String id) { this.staffId = id; }
    public void refresh()             { loadTable(); }

    // ─── Root content ─────────────────────────────────────────────────────────
    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(AppTheme.BG_PAGE);

        // ── Header (title + subtitle) ─────────────────────────────────────────
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, AppTheme.GAP_SECTION, 0));

        JLabel title = UIComponent.createPageTitle("Appointments");
        JLabel sub   = UIComponent.createCaptionLabel("View, approve, assign and cancel appointments");
        sub.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        header.add(title);
        header.add(sub);

        // ── Centre: table card + bottom card stacked ──────────────────────────
        JPanel centre = new JPanel(new BorderLayout(0, 16));
        centre.setOpaque(false);

        centre.add(buildTableCard(), BorderLayout.CENTER);

        bottomCard = buildBottomCard();
        bottomCard.setVisible(false);
        centre.add(bottomCard, BorderLayout.SOUTH);

        root.add(header,  BorderLayout.NORTH);
        root.add(centre,  BorderLayout.CENTER);
        return root;
    }

    // ─── Table card (Billing-style) ───────────────────────────────────────────
    private JPanel buildTableCard() {

        // ── Toolbar ───────────────────────────────────────────────────────────
        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.setOpaque(false);

        final String PLACEHOLDER = "Search by ID, Name or Plate";
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
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { loadTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { loadTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { loadTable(); }
        });

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterRow.setOpaque(false);

        filterStatus = UIComponent.createComboBox(new String[]{
            "All Status", "Pending", "Approved", "Completed", "Cancelled", "Rejected"
        });
        filterStatus.setPreferredSize(new Dimension(142, 38));
        filterStatus.addActionListener(e -> loadTable());

        filterDate = UIComponent.createDatePicker(null);
        filterDate.setPreferredSize(new Dimension(125, 38));
        filterDate.addDateChangeListener(d -> loadTable());

        JButton btnRefresh = UIComponent.createSmallButton("↺ Refresh");
        btnRefresh.setPreferredSize(new Dimension(95, 34));
        btnRefresh.addActionListener(e -> {
            searchField.setText(PLACEHOLDER);
            searchField.setForeground(AppTheme.TEXT_MUTED);
            filterStatus.setSelectedIndex(0);
            filterDate.setDate(null);
            loadTable();
        });

        filterRow.add(searchField);
        filterRow.add(filterStatus);
        filterRow.add(filterDate);
        filterRow.add(btnRefresh);

        countLabel = new JLabel("Loading…");
        countLabel.setFont(AppTheme.FONT_SMALL);
        countLabel.setForeground(AppTheme.TEXT_SECONDARY);

        toolbar.add(filterRow,  BorderLayout.WEST);
        toolbar.add(countLabel, BorderLayout.EAST);

        // ── Table ─────────────────────────────────────────────────────────────
        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel) {
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
                if (!isRowSelected(row) && col != 6) {
                    c.setBackground(AppTheme.BG_CARD);
                    c.setForeground(col == 0 ? AppTheme.TEXT_SECONDARY : AppTheme.TEXT_PRIMARY);
                }
                return c;
            }
        };

        table.setRowHeight(52);
        table.setFont(AppTheme.FONT_SMALL);
        table.setForeground(AppTheme.TEXT_PRIMARY);
        table.setBackground(AppTheme.BG_CARD);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(AppTheme.PRIMARY_LIGHT);
        table.setSelectionForeground(AppTheme.TEXT_PRIMARY);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // ── Header ────────────────────────────────────────────────────────────
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
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
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);

        // ── Column widths ─────────────────────────────────────────────────────
        int[] widths = {80, 150, 180, 160, 100, 80, 110};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

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
        for (int i = 0; i < 6; i++)
            table.getColumnModel().getColumn(i).setCellRenderer(paddedRenderer);

        // ── Status pill badge (col 6) ─────────────────────────────────────────
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                String status = val == null ? "" : val.toString();
                Color pillBg, pillFg;
                switch (status.toLowerCase()) {
                    case "pending":
                        pillBg = AppTheme.STATUS_PENDING_BG;
                        pillFg = AppTheme.STATUS_PENDING_FG;    break;
                    case "approved":
                        pillBg = AppTheme.STATUS_INPROGRESS_BG;
                        pillFg = AppTheme.STATUS_INPROGRESS_FG; break;
                    case "completed":
                        pillBg = AppTheme.STATUS_COMPLETED_BG;
                        pillFg = AppTheme.STATUS_COMPLETED_FG;  break;
                    case "paid":
                        pillBg = AppTheme.STATUS_COMPLETED_BG;
                        pillFg = AppTheme.STATUS_COMPLETED_FG;  break;
                    case "cancelled":
                        pillBg = new Color(255, 240, 220);
                        pillFg = new Color(150, 100, 50);        break;
                    case "rejected":
                        pillBg = new Color(255, 220, 220);
                        pillFg = AppTheme.DANGER;                break;
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

        // ── Row click ─────────────────────────────────────────────────────────
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onRowSelected();
        });

        // ── Status bar ────────────────────────────────────────────────────────
        JLabel statusBar = new JLabel("  Select a row to view appointment details");
        statusBar.setFont(AppTheme.FONT_CAPTION);
        statusBar.setForeground(AppTheme.TEXT_MUTED);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.SEPARATOR),
            BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        statusBar.setBackground(AppTheme.BG_PAGE);
        statusBar.setOpaque(true);

        table.getSelectionModel().addListSelectionListener(e2 -> {
            if (!e2.getValueIsAdjusting()) {
                int r = table.getSelectedRow();
                statusBar.setText(r >= 0
                    ? "  Selected: " + tableModel.getValueAt(r, 0)
                        + "  ·  " + tableModel.getValueAt(r, 1)
                        + "  ·  Status: " + tableModel.getValueAt(r, 6)
                    : "  Select a row to view appointment details");
            }
        });

        // ── JLayer rounded card ───────────────────────────────────────────────
        tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableScrollPane.setOpaque(true);
        tableScrollPane.setBackground(AppTheme.BG_CARD);
        tableScrollPane.getViewport().setOpaque(true);
        tableScrollPane.getViewport().setBackground(AppTheme.BG_CARD);
        tableScrollPane.getViewport().setBorder(null);
        tableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        tableScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        tableScrollPane.getVerticalScrollBar().setUnitIncrement(14);

        JTableHeader tblHeader = table.getTableHeader();
        tblHeader.setOpaque(true);
        tblHeader.setBackground(AppTheme.BG_PAGE);
        tableScrollPane.setColumnHeader(null);

        tableInnerPanel = new JPanel(new BorderLayout());
        tableInnerPanel.setOpaque(true);
        tableInnerPanel.setBackground(AppTheme.BG_CARD);
        tableInnerPanel.add(tblHeader,       BorderLayout.NORTH);
        tableInnerPanel.add(tableScrollPane, BorderLayout.CENTER);
        tableInnerPanel.add(statusBar,       BorderLayout.SOUTH);

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
                g2.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, rad, rad);
                g2.dispose();
            }
        };
        JLayer<JPanel> cardLayer = new JLayer<>(tableInnerPanel, layerUI);

        // toolbar + card in one wrapper panel
        JPanel section = new JPanel(new BorderLayout(0, 10));
        section.setOpaque(false);
        section.add(toolbar,   BorderLayout.NORTH);
        section.add(cardLayer, BorderLayout.CENTER);

        return section;
    }

    // ─── Compact toggle ───────────────────────────────────────────────────────
    // Constrains tableInnerPanel height so table + detail panel both fit on screen
    private static final int COMPACT_ROWS = 12;

    private void setTableCompact(boolean compact) {
        if (tableInnerPanel == null) return;
        if (compact) {
            int headerH    = table.getTableHeader().getPreferredSize().height;
            int statusBarH = 30;
            int h = headerH + table.getRowHeight() * COMPACT_ROWS + statusBarH;
            tableInnerPanel.setPreferredSize(new Dimension(0, h));
            tableInnerPanel.setMinimumSize  (new Dimension(0, h));
            tableInnerPanel.setMaximumSize  (new Dimension(Integer.MAX_VALUE, h));
        } else {
            tableInnerPanel.setPreferredSize(null);
            tableInnerPanel.setMinimumSize  (null);
            tableInnerPanel.setMaximumSize  (new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        }
        revalidate();
        repaint();
    }

    // ─── Bottom card ─────────────────────────────────────────────────────────
    private JPanel buildBottomCard() {
        JPanel outer = new JPanel(new BorderLayout(0, 0));
        outer.setOpaque(true);
        outer.setBackground(AppTheme.BG_CARD);

        // Section header: white background, accent bar + title (like pic 3)
        JPanel sectionHeader = new JPanel(new BorderLayout());
        sectionHeader.setOpaque(false);
        sectionHeader.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.SEPARATOR),
            BorderFactory.createEmptyBorder(0, AppTheme.PADDING_CARD, 0, AppTheme.PADDING_CARD)));
        sectionHeader.setPreferredSize(new Dimension(0, 46));

        // Left: accent bar + title, vertically centered
        JPanel titleRow = new JPanel();
        titleRow.setLayout(new BoxLayout(titleRow, BoxLayout.X_AXIS));
        titleRow.setOpaque(false);

        JPanel accentBar = new JPanel();
        accentBar.setBackground(AppTheme.PRIMARY);
        accentBar.setOpaque(true);
        accentBar.setMinimumSize(new Dimension(3, 18));
        accentBar.setPreferredSize(new Dimension(3, 18));
        accentBar.setMaximumSize(new Dimension(3, 18));

        JLabel sectionTitle = new JLabel("Appointment Details");
        sectionTitle.setFont(AppTheme.FONT_SMALL_BOLD);
        sectionTitle.setForeground(AppTheme.TEXT_PRIMARY);
        sectionTitle.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));

        titleRow.add(Box.createVerticalGlue());
        titleRow.add(accentBar);
        titleRow.add(sectionTitle);
        titleRow.add(Box.createVerticalGlue());

        // Wrap in a panel that centers titleRow vertically
        JPanel titleWrap = new JPanel(new GridBagLayout());
        titleWrap.setOpaque(false);
        titleWrap.add(titleRow);

        sectionHeader.add(titleWrap, BorderLayout.WEST);

        // Close button: plain link style, no outline
        JButton closeBtn = UIComponent.createLinkButton("✕ Close");
        closeBtn.addActionListener(e -> {
            table.clearSelection();
            setTableCompact(false);
            bottomCard.setVisible(false);
            showCard("hint");
        });
        sectionHeader.add(closeBtn, BorderLayout.EAST);

        outer.add(sectionHeader, BorderLayout.NORTH);

        // CardLayout body
        JPanel pendingCard  = buildPendingCard();
        JPanel approvedCard = buildApprovedCard();
        JPanel viewCard     = buildViewCard();

        JPanel hintCard = new JPanel();
        hintCard.setOpaque(false);

        bodyPanel = new JPanel(new CardLayout());
        bodyPanel.setOpaque(false);
        bodyPanel.add(hintCard,     "hint");
        bodyPanel.add(pendingCard,  "pending");
        bodyPanel.add(approvedCard, "approved");
        bodyPanel.add(viewCard,     "view");

        outer.add(bodyPanel, BorderLayout.CENTER);

        // Wrap in JLayer for rounded corners + border
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
                g2.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, rad, rad);
                g2.dispose();
            }
        };
        JLayer<JPanel> cardLayer = new JLayer<>(outer, layerUI);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(cardLayer, BorderLayout.CENTER);
        return wrapper;
    }

    // ─── Pending card ─────────────────────────────────────────────────────────
    private JPanel buildPendingCard() {
        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setOpaque(false);

        // ── Left: info grid ───────────────────────────────────────────────────
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
        JPanel infoPanel = buildInfoGrid(fields, vals);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_CARD, AppTheme.PADDING_CARD, AppTheme.PADDING_CARD, 12));
        card.add(infoPanel, BorderLayout.CENTER);

        // ── Right: action panel — white, left-border separator ────────────────
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setOpaque(false);
        actionPanel.setPreferredSize(new Dimension(300, 0));
        actionPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, AppTheme.SEPARATOR),
            BorderFactory.createEmptyBorder(AppTheme.PADDING_CARD, 16, AppTheme.PADDING_CARD, 16)));

        JLabel actionTitle = new JLabel("Available Technician");
        actionTitle.setFont(AppTheme.FONT_SMALL_BOLD);
        actionTitle.setForeground(AppTheme.TEXT_SECONDARY);
        actionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        techCombo = UIComponent.createComboBox(new String[]{});
        techCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        techCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Approve + Reject side by side, equal width
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));

        btnApprove = UIComponent.createPrimaryButton("Approve");
        btnReject  = UIComponent.createOutlineButton("Reject");
        btnApprove.addActionListener(e -> handleApprove());
        btnReject.addActionListener(e  -> handleReject());
        btnRow.add(btnApprove);
        btnRow.add(btnReject);
        btnRow.setPreferredSize(new Dimension(Short.MAX_VALUE, 45));

        actionPanel.add(actionTitle);
        actionPanel.add(techCombo);
        actionPanel.add(btnRow);
        actionPanel.add(Box.createVerticalGlue());

        card.add(actionPanel, BorderLayout.EAST);
        return card;
    }

    // ─── Approved card ────────────────────────────────────────────────────────
    private JPanel buildApprovedCard() {
        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setOpaque(false);

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
        JPanel infoPanel = buildInfoGrid(fields, vals);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_CARD, AppTheme.PADDING_CARD, AppTheme.PADDING_CARD, 12));
        card.add(infoPanel, BorderLayout.CENTER);

        // ── Right: action panel ────────────────────────────────────────────────
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setOpaque(false);
        actionPanel.setPreferredSize(new Dimension(300, 0));
        actionPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, AppTheme.SEPARATOR),
            BorderFactory.createEmptyBorder(AppTheme.PADDING_CARD, 16, AppTheme.PADDING_CARD, 16)));

        JLabel actionTitle = new JLabel("Appointment Actions");
        actionTitle.setFont(AppTheme.FONT_SMALL_BOLD);
        actionTitle.setForeground(AppTheme.TEXT_SECONDARY);
        actionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        // Notice box
        JPanel noticeBox = new JPanel(new BorderLayout());
        noticeBox.setBackground(new Color(255, 249, 230));
        noticeBox.setOpaque(true);
        noticeBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        noticeBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        noticeBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(240, 210, 100), 1, true),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JLabel noticeText = new JLabel(
            "<html>Technician has been assigned.<br>"
            + "<span style='color:#a07800'>Cancelling will free the technician.</span></html>");
        noticeText.setFont(AppTheme.FONT_SMALL);
        noticeText.setForeground(new Color(100, 75, 0));
        noticeBox.add(noticeText, BorderLayout.CENTER);

        JSeparator sep = new JSeparator();
        sep.setForeground(AppTheme.SEPARATOR);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnCancel = UIComponent.createDangerButton("Cancel Appointment");
        btnCancel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnCancel.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnCancel.addActionListener(e -> handleCancel());

        actionPanel.add(actionTitle);
        actionPanel.add(noticeBox);
        actionPanel.add(Box.createVerticalStrut(14));
        actionPanel.add(sep);
        actionPanel.add(Box.createVerticalStrut(14));
        actionPanel.add(btnCancel);
        actionPanel.add(Box.createVerticalGlue());

        card.add(actionPanel, BorderLayout.EAST);
        return card;
    }

    // ─── View-only card ───────────────────────────────────────────────────────
    private JPanel buildViewCard() {
        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setOpaque(false);

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
        JPanel infoPanel = buildInfoGrid(fields, vals);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_CARD, AppTheme.PADDING_CARD, AppTheme.PADDING_CARD, 12));
        card.add(infoPanel, BorderLayout.CENTER);

        // ── Right: view-only notice panel ─────────────────────────────────────
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setOpaque(false);
        actionPanel.setPreferredSize(new Dimension(300, 0));
        actionPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, AppTheme.SEPARATOR),
            BorderFactory.createEmptyBorder(AppTheme.PADDING_CARD, 16, AppTheme.PADDING_CARD, 16)));

        JLabel viewTitle = new JLabel("🔒  Read Only");
        viewTitle.setFont(AppTheme.FONT_SMALL_BOLD);
        viewTitle.setForeground(AppTheme.TEXT_SECONDARY);
        viewTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        viewTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        JPanel noticeBox = new JPanel(new BorderLayout());
        noticeBox.setBackground(new Color(245, 245, 250));
        noticeBox.setOpaque(true);
        noticeBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        noticeBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        noticeBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER_DEFAULT, 1, true),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        JLabel noticeText = new JLabel(
            "<html><b>View Only</b><br><br>"
            + "This appointment is in a final state<br>and cannot be modified.</html>");
        noticeText.setFont(AppTheme.FONT_SMALL);
        noticeText.setForeground(AppTheme.TEXT_SECONDARY);
        noticeBox.add(noticeText, BorderLayout.CENTER);

        actionPanel.add(viewTitle);
        actionPanel.add(noticeBox);
        actionPanel.add(Box.createVerticalGlue());

        card.add(actionPanel, BorderLayout.EAST);
        return card;
    }

    // ─── Row selection ────────────────────────────────────────────────────────
    private void onRowSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        String apptId   = (String) tableModel.getValueAt(row, 0);
        String custName = (String) tableModel.getValueAt(row, 1);
        String service  = (String) tableModel.getValueAt(row, 2);
        String vehicle  = (String) tableModel.getValueAt(row, 3);
        String date     = (String) tableModel.getValueAt(row, 4);
        String time     = (String) tableModel.getValueAt(row, 5);
        String status   = (String) tableModel.getValueAt(row, 6);

        selectedApptId = apptId;

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

                techCombo.removeAllItems();
                techCombo.addItem("-- Select Technician --");
                List<Technician> available = getAvailableTechniciansForSlot(date, time);
                if (available.isEmpty()) {
                    techCombo.addItem("(No technicians free for this slot)");
                } else {
                    for (Technician t : available)
                        techCombo.addItem(t.getTechnicianId() + " — " + t.getUsername());
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

            default:
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

        setTableCompact(true);
        bottomCard.setVisible(true);
    }

    private void showCard(String name) {
        ((CardLayout) bodyPanel.getLayout()).show(bodyPanel, name);
    }

    // ─── Load / reload table ──────────────────────────────────────────────────
    private void loadTable() {
        tableModel.setRowCount(0);
        selectedApptId = null;
        setTableCompact(false);
        if (bottomCard != null) bottomCard.setVisible(false);

        String statusFilter = (String) filterStatus.getSelectedItem();
        String dateFilter   = filterDate.getText().trim();
        String searchRaw    = searchField.getText().trim();
        String searchQuery  = (searchRaw.startsWith("Search by ID") || searchRaw.isEmpty()) ? "" : searchRaw.toLowerCase();

        String isoDate = "";
        if (!dateFilter.isEmpty()) {
            try { LocalDate.parse(dateFilter, DateTimeFormatter.ofPattern("yyyy-MM-dd")); isoDate = dateFilter; }
            catch (Exception e1) {
                try { isoDate = LocalDate.parse(dateFilter, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                          .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); }
                catch (Exception e2) {
                    try { isoDate = LocalDate.parse(dateFilter, DateTimeFormatter.ofPattern("MM/dd/yyyy"))
                              .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); }
                    catch (Exception ignored) {}
                }
            }
        }
        final String finalIso = isoDate;

        List<Appointment> list = appointmentDAO.getAllAppointments();
        int count = 0;

        for (Appointment a : list) {
            boolean matchStatus = statusFilter.equals("All Status") || a.getStatus().equalsIgnoreCase(statusFilter);
            boolean matchDate   = finalIso.isEmpty() || a.getAppointmentDate().equals(finalIso);
            if (!matchStatus || !matchDate) continue;

            String customerName = resolveCustomerName(a.getCustomerId());
            String vehicle      = a.getVehicleBrand() + " " + a.getVehicleModel() + " (" + a.getVehiclePlate() + ")";
            String serviceItemId = a.getServiceItemId();
            String serviceName   = models.dao.ServiceItemDAO.getServiceName(serviceItemId);
            String service       = serviceName;

            if (!searchQuery.isEmpty()) {
                boolean match =
                    a.getAppointmentId().toLowerCase().contains(searchQuery)
                    || customerName.toLowerCase().contains(searchQuery)
                    || a.getVehiclePlate().toLowerCase().contains(searchQuery);
                if (!match) continue;
            }

            tableModel.addRow(new Object[]{
                a.getAppointmentId(), customerName, service, vehicle,
                a.getAppointmentDate(), a.getTimeSlot(), a.getStatus()
            });
            count++;
        }

        int pending = (int) list.stream().filter(a -> a.getStatus().equalsIgnoreCase("Pending")).count();
        countLabel.setText("Showing " + count + " records  |  " + pending + " Pending");
        
        if (onDataChanged != null) onDataChanged.run();
    }

    // ─── Action handlers ──────────────────────────────────────────────────────
    private void handleApprove() {
        if (selectedApptId == null) return;
        String techSelected = (String) techCombo.getSelectedItem();
        if (techSelected == null || techSelected.startsWith("--") || techSelected.startsWith("(No")) {
            UIComponent.showDialog(this, "No Technician Selected",
                UIComponent.DialogType.WARNING,
                "Please select a technician before approving.",
                null, null);
            return;
        }
        String techId = techSelected.split(" — ")[0].trim();
        boolean ok = appointmentDAO.approveAppointment(selectedApptId, techId, staffId);
        if (ok) {
            technicianDAO.updateWorkStatus(techId, "Busy");
            UIComponent.showDialog(this, "Appointment Approved",
                UIComponent.DialogType.SUCCESS,
                "Assigned to " + techSelected + ".",
                new String[][]{
                    {"Appointment", selectedApptId},
                    {"Technician",  techSelected},
                },
                new UIComponent.DialogAction[]{
                    new UIComponent.DialogAction("Close", UIComponent.DialogActionStyle.OUTLINE, () -> loadTable())
                });
        } else {
            UIComponent.showDialog(this, "Approval Failed",
                UIComponent.DialogType.DANGER,
                "Failed to approve appointment. Please try again.",
                null, null);
        }
    }

    private void handleReject() {
        if (selectedApptId == null) return;
        UIComponent.showDialog(this, "Confirm Reject",
            UIComponent.DialogType.WARNING,
            "Reject appointment " + selectedApptId + "? This cannot be undone.",
            null,
            new UIComponent.DialogAction[]{
                new UIComponent.DialogAction("Reject", UIComponent.DialogActionStyle.DANGER, () -> {
                    boolean ok = appointmentDAO.rejectAppointment(selectedApptId);
                    if (ok) {
                        UIComponent.showDialog(this, "Appointment Rejected",
                            UIComponent.DialogType.INFO,
                            "Appointment " + selectedApptId + " has been rejected.",
                            null, new UIComponent.DialogAction[]{
                                new UIComponent.DialogAction("Close", UIComponent.DialogActionStyle.OUTLINE, () -> loadTable())
                            });
                    } else {
                        UIComponent.showDialog(this, "Reject Failed",
                            UIComponent.DialogType.DANGER,
                            "Failed to reject appointment. Please try again.",
                            null, null);
                    }
                }),
                new UIComponent.DialogAction("Cancel", UIComponent.DialogActionStyle.OUTLINE, null),
            });
    }

    private void handleCancel() {
        if (selectedApptId == null) return;
        Appointment appt = appointmentDAO.getAllAppointments().stream()
            .filter(a -> a.getAppointmentId().equals(selectedApptId))
            .findFirst().orElse(null);
        UIComponent.showDialog(this, "Confirm Cancellation",
            UIComponent.DialogType.WARNING,
            "Cancel appointment " + selectedApptId + "? The assigned technician will be marked Available again.",
            null,
            new UIComponent.DialogAction[]{
                new UIComponent.DialogAction("Cancel Appointment", UIComponent.DialogActionStyle.DANGER, () -> {
                    boolean ok = appointmentDAO.cancelAppointment(selectedApptId);
                    if (ok) {
                        if (appt != null && appt.getTechnicianId() != null
                                && !appt.getTechnicianId().equalsIgnoreCase("null")
                                && !appt.getTechnicianId().trim().isEmpty())
                            technicianDAO.updateWorkStatus(appt.getTechnicianId(), "Available");
                        UIComponent.showDialog(this, "Appointment Cancelled",
                            UIComponent.DialogType.INFO,
                            "Appointment " + selectedApptId + " has been cancelled.",
                            null, new UIComponent.DialogAction[]{
                                new UIComponent.DialogAction("Close", UIComponent.DialogActionStyle.OUTLINE, () -> loadTable())
                            });
                    } else {
                        UIComponent.showDialog(this, "Cancellation Failed",
                            UIComponent.DialogType.DANGER,
                            "Failed to cancel appointment. Please try again.",
                            null, null);
                    }
                }),
                new UIComponent.DialogAction("Go Back", UIComponent.DialogActionStyle.OUTLINE, null),
            });
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────
    private List<Technician> getAvailableTechniciansForSlot(String date, String timeSlot) {
        List<Appointment> allAppts = appointmentDAO.getAllAppointments();
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

    private String resolveCustomerName(String customerId) {
        try {
            models.dao.CustomerDAO cDao = new models.dao.CustomerDAO();
            for (String[] c : cDao.getAllCustomers())
                if (c[0].trim().equals(customerId)) return c[2].trim();
            return customerId;
        } catch (Exception e) { return customerId; }
    }

    private String resolveTechDisplay(String techId) {
        if (techId == null || techId.equalsIgnoreCase("null") || techId.trim().isEmpty()) return "—";
        try {
            return technicianDAO.getAllTechnicians().stream()
                .filter(t -> t.getTechnicianId().equals(techId))
                .map(t -> t.getTechnicianId() + " — " + t.getUsername())
                .findFirst().orElse(techId);
        } catch (Exception e) { return techId; }
    }

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
            if (i < fieldNames.length - 1) {
                JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
                sep.setForeground(AppTheme.SEPARATOR);
                sep.setPreferredSize(new Dimension(0, 1));
                GridBagConstraints sg = new GridBagConstraints();
                sg.gridx = 0; sg.gridy = i * 2 + 1; sg.gridwidth = 2;
                sg.fill = GridBagConstraints.HORIZONTAL; sg.weightx = 1;
                sg.insets = new Insets(6, 0, 0, 0);
                grid.add(sep, sg);
            }
        }
        return grid;
    }
}