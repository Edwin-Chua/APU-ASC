package views.manager;

import models.dao.AppointmentDAO;
import models.dao.CustomerDAO;
import models.dao.FeedbackDAO;
import models.dao.TechnicianDAO;
import models.dao.ManageEmployeeDAO;
import models.dao.UserDAO;
import models.entities.User;
import views.MainFrame;
import views.shared.AppTheme;
import views.shared.UIComponent;

import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class ViewFeedbacksPage extends JPanel {

    private final FeedbackDAO    feedbackDAO    = new FeedbackDAO();
    private final TechnicianDAO  technicianDAO  = new TechnicianDAO();

    // ─── Stat card labels ─────────────────────────────────────────────────────
    private JLabel lblTotalFeedback;
    private JLabel lblTechRating;
    private JLabel lblStaffRating;

    // ─── Table ────────────────────────────────────────────────────────────────
    private DefaultTableModel tableModel;
    private JTable            table;
    private List<FeedbackDAO.FeedbackSummary> loadedData;

    private static final String[] COLS = {
        "Appt ID", "Customer", "Service", "Technician", "Counter Staff", "Date", "Feedback"
    };

    // ─── Bottom detail panel ──────────────────────────────────────────────────
    private JPanel   detailBodyPanel;   // CardLayout host
    private CardLayout detailCardLayout;

    // Detail labels
    private JLabel lblDetApptId, lblDetCustomer, lblDetService,
                   lblDetVehicle, lblDetDateTime, lblDetTech, lblDetStaff;
    private JLabel lblDetTechRating, lblDetTechComment, lblDetTechDate;
    private JLabel lblDetStaffRating, lblDetStaffComment, lblDetStaffDate;

    // =========================================================================
    // Constructor
    // =========================================================================
    public ViewFeedbacksPage(MainFrame frame) {
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
        header.add(UIComponent.createPageTitle("View Feedbacks"), BorderLayout.WEST);
        return header;
    }

    // =========================================================================
    // Body — scrollable column of sections
    // =========================================================================
    private JScrollPane buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(AppTheme.BG_CONTENT);
        body.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_PAGE, AppTheme.PADDING_PAGE,
            AppTheme.PADDING_PAGE, AppTheme.PADDING_PAGE
        ));

        body.add(buildStatCards());
        body.add(UIComponent.verticalGap(AppTheme.GAP_SECTION));
        body.add(buildTableCard());
        body.add(UIComponent.verticalGap(AppTheme.GAP_ITEM));
        body.add(buildDetailCard());

        loadData();
        return UIComponent.createAppleScrollPane(body);
    }

    // =========================================================================
    // Stat cards  (style matches ManageEmployeesPage)
    // =========================================================================
    private JPanel buildStatCards() {
        JPanel row = new JPanel(new GridLayout(1, 3, AppTheme.GAP_ITEM, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblTotalFeedback = new JLabel("0");
        lblTechRating    = new JLabel("—");
        lblStaffRating   = new JLabel("—");

        row.add(buildStatCard("Total Feedback",         lblTotalFeedback, AppTheme.PRIMARY));
        row.add(buildStatCard("Technician Ratings",     lblTechRating,    AppTheme.SUCCESS));
        row.add(buildStatCard("Counter Staff Ratings",  lblStaffRating,   AppTheme.WARNING));
        return row;
    }

    private JPanel buildStatCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = UIComponent.createCard();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_CARD, AppTheme.PADDING_CARD,
            AppTheme.PADDING_CARD, AppTheme.PADDING_CARD
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(AppTheme.FONT_HEADING_3);
        lblTitle.setForeground(AppTheme.TEXT_SECONDARY);

        valueLabel.setFont(AppTheme.FONT_HEADING_1);
        valueLabel.setForeground(accent);

        card.add(lblTitle,   BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // Table card  (rounded corners — same JLayer pattern as ManageEmployeesPage)
    // =========================================================================
    private JPanel buildTableCard() {
        String[] columns = COLS;
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(AppTheme.ROW_HEIGHT_TABLE);
        table.setFont(AppTheme.FONT_SMALL);
        table.setForeground(AppTheme.TEXT_PRIMARY);
        table.setBackground(AppTheme.BG_CARD);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(AppTheme.PRIMARY_LIGHT);
        table.setSelectionForeground(AppTheme.TEXT_PRIMARY);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // ── Hover tracking ─────────────────────────────────────────────────
        final int[] hoveredRow = {-1};
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int r = table.rowAtPoint(e.getPoint());
                if (r != hoveredRow[0]) { hoveredRow[0] = r; table.repaint(); }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) { hoveredRow[0] = -1; table.repaint(); }
        });

        final Color ROW_ODD   = AppTheme.BG_CARD;
        final Color ROW_EVEN  = new Color(248, 248, 251);
        final Color ROW_HOVER = new Color(235, 242, 255);

        // ── Default renderer ───────────────────────────────────────────────
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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

        // ── ID column — muted ──────────────────────────────────────────────
        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setFont(AppTheme.FONT_LABEL);
                setForeground(AppTheme.TEXT_SECONDARY);
                setBackground(sel ? AppTheme.PRIMARY_LIGHT
                    : hoveredRow[0] == row ? ROW_HOVER
                    : row % 2 == 0 ? ROW_ODD : ROW_EVEN);
                setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 8));
                return this;
            }
        });

        // ── Feedback column — pill badge showing count ─────────────────────
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                Color rowBg = sel ? AppTheme.PRIMARY_LIGHT
                    : hoveredRow[0] == row ? ROW_HOVER
                    : row % 2 == 0 ? ROW_ODD : ROW_EVEN;

                JPanel cell = new JPanel(new GridBagLayout());
                cell.setBackground(rowBg);
                cell.setOpaque(true);

                String text = val == null ? "" : val.toString();
                Color pillBg, pillFg;
                if ("Both".equals(text)) {
                    pillBg = AppTheme.STATUS_COMPLETED_BG; pillFg = AppTheme.STATUS_COMPLETED_FG;
                } else {
                    pillBg = AppTheme.STATUS_PENDING_BG;   pillFg = AppTheme.STATUS_PENDING_FG;
                }

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
                pill.setOpaque(false);
                pill.setFont(AppTheme.FONT_LABEL);
                pill.setForeground(pillFg);
                pill.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));

                GridBagConstraints gbc = new GridBagConstraints();
                gbc.anchor = GridBagConstraints.WEST;
                gbc.insets = new Insets(0, 12, 0, 8);
                cell.add(pill, gbc);
                return cell;
            }
        });

        // ── Header ─────────────────────────────────────────────────────────
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
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
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setBackground(new Color(245, 245, 248));

        // ── Column widths ──────────────────────────────────────────────────
        int[] widths = {80, 140, 160, 160, 160, 110, 110};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // ── Row click → populate detail card ──────────────────────────────
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onRowSelected();
        });

        // ── JLayer rounded card ────────────────────────────────────────────
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(true);
        scroll.setBackground(AppTheme.BG_CARD);
        scroll.getViewport().setOpaque(true);
        scroll.getViewport().setBackground(AppTheme.BG_CARD);
        scroll.getViewport().setBorder(null);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setOpaque(true);
        tableHeader.setBackground(new Color(245, 245, 248));
        scroll.setColumnHeader(null);

        JPanel inner = new JPanel(new BorderLayout(0, 0));
        inner.setOpaque(true);
        inner.setBackground(AppTheme.BG_CARD);
        inner.add(tableHeader, BorderLayout.NORTH);
        inner.add(scroll, BorderLayout.CENTER);

        final int r = AppTheme.RADIUS_CARD * 2;
        LayerUI<JPanel> layerUI = new LayerUI<JPanel>() {
            @Override public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.BG_CARD);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), r, r);
                g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, c.getWidth(), c.getHeight(), r, r));
                super.paint(g2, c);
                g2.setClip(null);
                g2.setColor(AppTheme.SEPARATOR);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, r, r);
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
    // Detail card (below table — CounterStaff pattern)
    // =========================================================================
    private JPanel buildDetailCard() {
        JPanel outer = UIComponent.createCard();
        outer.setLayout(new BorderLayout(0, 0));
        outer.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Section title bar
        JLabel sectionTitle = new JLabel("▼  Feedback Details");
        sectionTitle.setFont(AppTheme.FONT_SMALL_BOLD);
        sectionTitle.setForeground(AppTheme.TEXT_SECONDARY);
        sectionTitle.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.SEPARATOR),
            BorderFactory.createEmptyBorder(10, AppTheme.PADDING_CARD, 10, AppTheme.PADDING_CARD)
        ));
        outer.add(sectionTitle, BorderLayout.NORTH);

        // ── Hint card ──────────────────────────────────────────────────────
        JPanel hintCard = new JPanel(new BorderLayout());
        hintCard.setOpaque(false);
        hintCard.setPreferredSize(new Dimension(0, 260));
        JLabel hint = new JLabel("← Select a row above to view appointment and feedback details.",
            SwingConstants.CENTER);
        hint.setFont(AppTheme.FONT_SMALL);
        hint.setForeground(AppTheme.TEXT_MUTED);
        hintCard.add(hint, BorderLayout.CENTER);

        // ── Detail card ────────────────────────────────────────────────────
        JPanel detailCard = buildDetailContent();

        detailCardLayout = new CardLayout();
        detailBodyPanel  = new JPanel(detailCardLayout);
        detailBodyPanel.setOpaque(false);
        detailBodyPanel.add(hintCard,   "hint");
        detailBodyPanel.add(detailCard, "detail");

        outer.add(detailBodyPanel, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(outer, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildDetailContent() {
        JPanel card = new JPanel(new BorderLayout(24, 0));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_CARD, AppTheme.PADDING_CARD,
            AppTheme.PADDING_CARD, AppTheme.PADDING_CARD
        ));

        // ── LEFT — appointment info grid ───────────────────────────────────
        lblDetApptId   = new JLabel(); lblDetCustomer = new JLabel();
        lblDetService  = new JLabel(); lblDetVehicle  = new JLabel();
        lblDetDateTime = new JLabel(); lblDetTech     = new JLabel();
        lblDetStaff    = new JLabel();

        lblDetService.setFont(AppTheme.FONT_SMALL_BOLD);
        lblDetService.setForeground(AppTheme.PRIMARY);

        String[] leftFields = {
            "Appointment ID:", "Customer:", "Service:",
            "Vehicle:", "Date / Time:", "Technician:", "Counter Staff:"
        };
        JLabel[] leftVals = {
            lblDetApptId, lblDetCustomer, lblDetService,
            lblDetVehicle, lblDetDateTime, lblDetTech, lblDetStaff
        };
        card.add(buildInfoGrid(leftFields, leftVals), BorderLayout.WEST);

        // ── RIGHT — two feedback panels side by side ───────────────────────
        JPanel feedbackArea = new JPanel(new GridLayout(1, 2, 16, 0));
        feedbackArea.setOpaque(false);
        feedbackArea.setPreferredSize(new Dimension(560, 0));

        feedbackArea.add(buildFeedbackPanel("Technician Feedback", true));
        feedbackArea.add(buildFeedbackPanel("Counter Staff Feedback", false));

        card.add(feedbackArea, BorderLayout.CENTER);
        return card;
    }

    /** Builds one feedback sub-panel (tech or staff). isTech drives which labels get wired. */
    private JPanel buildFeedbackPanel(String title, boolean isTech) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER_CARD, 1, true),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        // Title
        JLabel lbl = new JLabel(title);
        lbl.setFont(AppTheme.FONT_SMALL_BOLD);
        lbl.setForeground(AppTheme.TEXT_PRIMARY);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        // Rating row
        JLabel ratingLabel = isTech ? (lblDetTechRating  = new JLabel("—"))
                                    : (lblDetStaffRating = new JLabel("—"));
        ratingLabel.setFont(AppTheme.FONT_BODY_BOLD);
        ratingLabel.setForeground(AppTheme.WARNING);

        JPanel ratingRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        ratingRow.setOpaque(false);
        JLabel ratingLbl = new JLabel("Rating:");
        ratingLbl.setFont(AppTheme.FONT_SMALL);
        ratingLbl.setForeground(AppTheme.TEXT_SECONDARY);
        ratingRow.add(ratingLbl);
        ratingRow.add(ratingLabel);

        // Comment
        JLabel commentLabel = isTech ? (lblDetTechComment  = new JLabel("—"))
                                     : (lblDetStaffComment = new JLabel("—"));
        commentLabel.setFont(AppTheme.FONT_SMALL);
        commentLabel.setForeground(AppTheme.TEXT_PRIMARY);
        commentLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER_DEFAULT, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        // Date
        JLabel dateLabel = isTech ? (lblDetTechDate  = new JLabel(""))
                                  : (lblDetStaffDate = new JLabel(""));
        dateLabel.setFont(AppTheme.FONT_CAPTION);
        dateLabel.setForeground(AppTheme.TEXT_MUTED);

        JPanel top = new JPanel(new BorderLayout(0, 4));
        top.setOpaque(false);
        top.add(lbl,       BorderLayout.NORTH);
        top.add(ratingRow, BorderLayout.SOUTH);

        panel.add(top,          BorderLayout.NORTH);
        panel.add(commentLabel, BorderLayout.CENTER);
        panel.add(dateLabel,    BorderLayout.SOUTH);
        return panel;
    }

    /** Consistent label/value grid — same as CounterStaffViewAppointment.buildInfoGrid */
    private JPanel buildInfoGrid(String[] fields, JLabel[] vals) {
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(3, 0, 3, 16);
        gbc.anchor  = GridBagConstraints.NORTHWEST;
        gbc.fill    = GridBagConstraints.HORIZONTAL;

        for (int i = 0; i < fields.length; i++) {
            JLabel fieldLbl = new JLabel(fields[i]);
            fieldLbl.setFont(AppTheme.FONT_SMALL_BOLD);
            fieldLbl.setForeground(AppTheme.TEXT_SECONDARY);
            fieldLbl.setPreferredSize(new Dimension(170, 22));

            vals[i].setFont(AppTheme.FONT_SMALL);
            vals[i].setForeground(AppTheme.TEXT_PRIMARY);

            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0;
            grid.add(fieldLbl, gbc);
            gbc.gridx = 1; gbc.weightx = 1;
            grid.add(vals[i], gbc);
        }
        return grid;
    }

    // =========================================================================
    // Row selection
    // =========================================================================
    private void onRowSelected() {
        int row = table.getSelectedRow();
        if (row < 0 || loadedData == null || row >= loadedData.size()) {
            detailCardLayout.show(detailBodyPanel, "hint");
            return;
        }

        FeedbackDAO.FeedbackSummary s = loadedData.get(row);

        // ── Appointment fields ─────────────────────────────────────────────
        lblDetApptId.setText(s.appointmentId);
        lblDetCustomer.setText(resolveCustomerName(s.customerId));
        lblDetService.setText("ST001".equalsIgnoreCase(s.serviceTypeId)
            ? "Normal Service — 1 hr" : "Major Service — 3 hrs");
        lblDetVehicle.setText(s.vehicleBrand + " " + s.vehicleModel + " (" + s.vehiclePlate + ")");
        lblDetDateTime.setText(s.appointmentDate + "  at  " + s.timeSlot);
        lblDetTech.setText(resolveName(s.technicianId, true));
        lblDetStaff.setText(resolveName(s.staffId, false));

        // ── Technician feedback ────────────────────────────────────────────
        if (s.techFeedback != null) {
            lblDetTechRating.setText(s.techFeedback.rating + " / 5  " + ratingStars(s.techFeedback.rating));
            lblDetTechRating.setForeground(ratingColor(s.techFeedback.rating));
            lblDetTechComment.setText("<html><body style='width:200px'>"
                + escapeHtml(s.techFeedback.comment) + "</body></html>");
            lblDetTechDate.setText("Submitted: " + s.techFeedback.date);
        } else {
            lblDetTechRating.setText("—");
            lblDetTechRating.setForeground(AppTheme.TEXT_MUTED);
            lblDetTechComment.setText("No feedback provided.");
            lblDetTechDate.setText("");
        }

        // ── Staff feedback ─────────────────────────────────────────────────
        if (s.staffFeedback != null) {
            lblDetStaffRating.setText(s.staffFeedback.rating + " / 5  " + ratingStars(s.staffFeedback.rating));
            lblDetStaffRating.setForeground(ratingColor(s.staffFeedback.rating));
            lblDetStaffComment.setText("<html><body style='width:200px'>"
                + escapeHtml(s.staffFeedback.comment) + "</body></html>");
            lblDetStaffDate.setText("Submitted: " + s.staffFeedback.date);
        } else {
            lblDetStaffRating.setText("—");
            lblDetStaffRating.setForeground(AppTheme.TEXT_MUTED);
            lblDetStaffComment.setText("No feedback provided.");
            lblDetStaffDate.setText("");
        }

        detailCardLayout.show(detailBodyPanel, "detail");
    }

    // =========================================================================
    // Data loading
    // =========================================================================
    public void loadData() {
        loadedData = feedbackDAO.getFeedbackSummaries();
        tableModel.setRowCount(0);

        for (FeedbackDAO.FeedbackSummary s : loadedData) {
            String customerName = resolveCustomerName(s.customerId);
            String service      = "ST001".equalsIgnoreCase(s.serviceTypeId)
                ? "Normal Service" : "Major Service";
            String techName     = resolveName(s.technicianId, true);
            String staffName    = resolveName(s.staffId, false);
            String fbBadge      = (s.techFeedback != null && s.staffFeedback != null) ? "Both"
                                : (s.techFeedback != null) ? "Tech Only"
                                : "Staff Only";

            tableModel.addRow(new Object[]{
                s.appointmentId,
                customerName,
                service,
                techName,
                staffName,
                s.appointmentDate,
                fbBadge
            });
        }

        // Update stat cards
        int total = feedbackDAO.getTotalFeedbackCount();
        lblTotalFeedback.setText(String.valueOf(total));

        double techAvg  = feedbackDAO.getAverageRating("TECHNICIAN");
        double staffAvg = feedbackDAO.getAverageRating("STAFF");
        lblTechRating.setText(techAvg  < 0 ? "N/A" : String.format("%.1f / 5", techAvg));
        lblStaffRating.setText(staffAvg < 0 ? "N/A" : String.format("%.1f / 5", staffAvg));

        detailCardLayout.show(detailBodyPanel, "hint");
    }

    // =========================================================================
    // Helpers
    // =========================================================================
    private String resolveCustomerName(String customerId) {
        if (customerId == null || customerId.isEmpty()) return "—";
        try {
            CustomerDAO cDao = new CustomerDAO();
            for (String[] c : cDao.getAllCustomers()) {
                if (c[0].trim().equals(customerId)) return c[2].trim();
            }
        } catch (Exception ignored) {}
        return customerId;
    }

    private String resolveName(String id, boolean isTech) {
        if (id == null || id.isEmpty()
                || "null".equalsIgnoreCase(id) || "N/A".equalsIgnoreCase(id)) return "—";
        try {
            if (isTech) {
                return technicianDAO.getAllTechnicians().stream()
                    .filter(t -> t.getTechnicianId().equals(id))
                    .map(t -> t.getUsername())
                    .findFirst().orElse(id);
            } else {
                ManageEmployeeDAO staffDao = new ManageEmployeeDAO();
                return staffDao.getAllCounterStaff().stream()
                    .filter(s -> s.getId().equals(id))
                    .map(s -> s.getUsername())
                    .findFirst()
                    .orElse(id);
            }
        } catch (Exception ignored) {}
        return id;
    }

    private String ratingStars(String ratingStr) {
        try {
            int r = Integer.parseInt(ratingStr.trim());
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= 5; i++) sb.append(i <= r ? "★" : "☆");
            return sb.toString();
        } catch (NumberFormatException e) { return ""; }
    }

    private Color ratingColor(String ratingStr) {
        try {
            int r = Integer.parseInt(ratingStr.trim());
            if (r >= 4) return AppTheme.SUCCESS;
            if (r == 3) return AppTheme.WARNING;
            return AppTheme.DANGER;
        } catch (NumberFormatException e) { return AppTheme.TEXT_MUTED; }
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}