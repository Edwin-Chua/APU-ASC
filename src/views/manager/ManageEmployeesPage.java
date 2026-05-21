package views.manager;

import models.dao.ManageEmployeeDAO;
import models.entities.ManageEmployee;
import models.entities.User;
import views.MainFrame;
import views.shared.AppTheme;
import views.shared.UIComponent;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.LayerUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.stream.Collectors;

public class ManageEmployeesPage extends JPanel {

    private static final int ROWS_PER_PAGE = 8;
    private static final String[] PROTECTED_IDS = {"M001"};

    private final MainFrame frame;
    private final ManageEmployeeDAO dao = new ManageEmployeeDAO();

    // Stat card labels
    private JLabel lblManagerCount;
    private JLabel lblStaffCount;
    private JLabel lblTechCount;

    // Table
    private DefaultTableModel tableModel;
    private JTable table;

    // Controls
    private JTextField txtSearch;
    private JComboBox<String> cmbRoleFilter;

    // Pagination
    private int currentPage = 0;
    private List<ManageEmployee> filteredList;
    private JLabel lblPagination;
    private JButton btnPrev;
    private JButton btnNext;

    public ManageEmployeesPage(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_CONTENT);
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
    }

    // ─── Header ───────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppTheme.BG_HEADER);
        header.setPreferredSize(new Dimension(0, AppTheme.HEADER_HEIGHT));
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER_HEADER),
            BorderFactory.createEmptyBorder(0, AppTheme.PADDING_PAGE, 0, AppTheme.PADDING_PAGE)
        ));
        header.add(UIComponent.createPageTitle("Manage Employees"), BorderLayout.WEST);
        return header;
    }

    // ─── Body ─────────────────────────────────────────────────────────────────

    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(AppTheme.BG_CONTENT);
        body.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_PAGE, AppTheme.PADDING_PAGE,
            AppTheme.PADDING_PAGE, AppTheme.PADDING_PAGE
        ));

        body.add(buildStatCards());
        body.add(UIComponent.verticalGap(AppTheme.GAP_SECTION));
        body.add(buildToolbar());
        body.add(UIComponent.verticalGap(AppTheme.GAP_ITEM));
        body.add(buildTablePanel());
        body.add(UIComponent.verticalGap(AppTheme.GAP_ITEM));
        body.add(buildPaginationBar());

        return body;
    }

    // ─── Stat cards ───────────────────────────────────────────────────────────

    private JPanel buildStatCards() {
        JPanel row = new JPanel(new GridLayout(1, 3, AppTheme.GAP_ITEM, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblManagerCount = new JLabel("0");
        lblStaffCount   = new JLabel("0");
        lblTechCount    = new JLabel("0");

        row.add(buildStatCard("Managers",     lblManagerCount, AppTheme.PRIMARY));
        row.add(buildStatCard("Counter Staff", lblStaffCount,  AppTheme.WARNING));
        row.add(buildStatCard("Technicians",  lblTechCount,   AppTheme.SUCCESS));

        return row;
    }

    private JPanel buildStatCard(String title, JLabel countLabel, Color accentColor) {
        JPanel card = UIComponent.createCard();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_CARD, AppTheme.PADDING_CARD,
            AppTheme.PADDING_CARD, AppTheme.PADDING_CARD
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(AppTheme.FONT_HEADING_3);
        lblTitle.setForeground(AppTheme.TEXT_SECONDARY);

        countLabel.setFont(AppTheme.FONT_HEADING_1);
        countLabel.setForeground(accentColor);

        card.add(lblTitle,    BorderLayout.NORTH);
        card.add(countLabel,  BorderLayout.CENTER);
        return card;
    }

    // ─── Toolbar ──────────────────────────────────────────────────────────────

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(AppTheme.GAP_ITEM, 0));
        toolbar.setOpaque(false);
        toolbar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        toolbar.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Left: search field + role filter
        JPanel leftSide = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftSide.setOpaque(false);

        // Rounded search field
        txtSearch = new JTextField() {
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
                g2.setColor(new Color(0, 0, 0, 50)); // light black
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
                g2.dispose();
            }
        };
        txtSearch.putClientProperty("caretWidth", 2);
        txtSearch.setFont(AppTheme.FONT_SMALL);
        txtSearch.setForeground(AppTheme.TEXT_PRIMARY);
        txtSearch.setBackground(AppTheme.BG_CARD);
        txtSearch.setOpaque(false);
        txtSearch.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        txtSearch.setPreferredSize(new Dimension(240, 38));
        // Placeholder text
        txtSearch.setText("Search by name or ID...");
        txtSearch.setForeground(AppTheme.TEXT_MUTED);
        txtSearch.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (txtSearch.getText().equals("Search by name or ID...")) {
                    txtSearch.setText("");
                    txtSearch.setForeground(AppTheme.TEXT_PRIMARY);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setText("Search by name or ID...");
                    txtSearch.setForeground(AppTheme.TEXT_MUTED);
                }
            }
        });
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                String text = txtSearch.getText();
                if (!text.equals("Search by name or ID...")) { currentPage = 0; applyFilter(); }
            }
        });

        // Rounded role filter combo — fully custom-painted to override L&F
        cmbRoleFilter = new JComboBox<>(new String[]{"All Roles", "Manager", "CounterStaff", "Technician"});
        cmbRoleFilter.setFont(AppTheme.FONT_SMALL);
        cmbRoleFilter.setBackground(AppTheme.BG_CARD);
        cmbRoleFilter.setForeground(AppTheme.TEXT_PRIMARY);
        cmbRoleFilter.setPreferredSize(new Dimension(160, 38));
        cmbRoleFilter.setFocusable(false);
        cmbRoleFilter.setOpaque(false);
        // Strip every visual from the default L&F
        cmbRoleFilter.setBorder(BorderFactory.createEmptyBorder());
        cmbRoleFilter.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton arrow = new JButton("▾") {
                    @Override protected void paintComponent(Graphics g) {
                        // transparent — arrow is drawn by the combo's own paintComponent below
                    }
                    @Override protected void paintBorder(Graphics g) {}
                };
                arrow.setOpaque(false);
                arrow.setContentAreaFilled(false);
                arrow.setBorderPainted(false);
                arrow.setFocusPainted(false);
                arrow.setFont(new Font("SansSerif", Font.PLAIN, 11));
                arrow.setForeground(AppTheme.TEXT_SECONDARY);
                return arrow;
            }
            @Override public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                // do nothing — background painted below
            }
            @Override public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Background fill
                g2.setColor(AppTheme.BG_CARD);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 22, 22);
                // Border
                g2.setColor(AppTheme.SEPARATOR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 22, 22);
                // Selected text
                String selected = cmbRoleFilter.getSelectedItem() == null ? "" : cmbRoleFilter.getSelectedItem().toString();
                g2.setColor(AppTheme.TEXT_PRIMARY);
                g2.setFont(AppTheme.FONT_SMALL);
                FontMetrics fm = g2.getFontMetrics();
                int textY = (c.getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(selected, 14, textY);
                // Arrow indicator
                g2.setColor(AppTheme.TEXT_SECONDARY);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
                FontMetrics afm = g2.getFontMetrics();
                String arrow = "▾";
                int ax = c.getWidth() - afm.stringWidth(arrow) - 12;
                int ay = (c.getHeight() + afm.getAscent() - afm.getDescent()) / 2;
                g2.drawString(arrow, ax, ay);
                g2.dispose();
            }
        });
        cmbRoleFilter.addActionListener(e -> { currentPage = 0; applyFilter(); });

        leftSide.add(txtSearch);
        leftSide.add(cmbRoleFilter);

        // Right: Add Employee button 
        JButton btnAdd = new JButton("+ Add Employee") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppTheme.RADIUS_BUTTON * 2, AppTheme.RADIUS_BUTTON * 2);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnAdd.setFont(AppTheme.FONT_SMALL_BOLD);
        btnAdd.setForeground(AppTheme.TEXT_ON_PRIMARY);
        btnAdd.setBackground(AppTheme.PRIMARY);
        btnAdd.setPreferredSize(new Dimension(160, 38));
        btnAdd.setFocusPainted(false);
        btnAdd.setBorderPainted(false);
        btnAdd.setContentAreaFilled(false);
        btnAdd.setOpaque(false);
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdd.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnAdd.setBackground(AppTheme.PRIMARY_HOVER); }
            @Override public void mouseExited(MouseEvent e)  { btnAdd.setBackground(AppTheme.PRIMARY); }
        });
        btnAdd.addActionListener(e -> showAddDialog());

        toolbar.add(leftSide, BorderLayout.WEST);
        toolbar.add(btnAdd,   BorderLayout.EAST);
        return toolbar;
    }

    // ─── Table ────────────────────────────────────────────────────────────────

    private JPanel buildTablePanel() {
        String[] columns = {"ID", "Full Name", "Role", "Email", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 4; }
        };

        table = new JTable(tableModel) {
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (!isRowSelected(row) && col != 2 && col != 4) {
                    c.setBackground(AppTheme.BG_CARD);
                    c.setForeground(col == 0 ? AppTheme.TEXT_SECONDARY : AppTheme.TEXT_PRIMARY);
                }
                return c;
            }
            // Paint a subtle divider line between rows instead of full grid
            @Override public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(AppTheme.SEPARATOR);
                for (int i = 0; i < getRowCount(); i++) {
                    int y = (i + 1) * getRowHeight() - 1;
                    g2.drawLine(0, y, getWidth(), y);
                }
                g2.dispose();
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

        // ── Header styling ────────────────────────────────────────────────────
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
                    BorderFactory.createEmptyBorder(0, 14, 0, 8)
                ));
                // "Actions" column centered, rest left-aligned
                lbl.setHorizontalAlignment(col == 4 ? SwingConstants.CENTER : SwingConstants.LEFT);
                return lbl;
            }
        });
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);

        // ── Column widths ─────────────────────────────────────────────────────
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(190);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        table.getColumnModel().getColumn(3).setPreferredWidth(230);
        table.getColumnModel().getColumn(4).setPreferredWidth(160);

        // ── Default text cell renderer ────────────────────────────────────────
        DefaultTableCellRenderer paddedRenderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 8));
                setBackground(AppTheme.BG_CARD);
                if (col == 0) {
                    setFont(AppTheme.FONT_LABEL);
                    setForeground(AppTheme.TEXT_SECONDARY);
                } else {
                    setFont(AppTheme.FONT_SMALL);
                    setForeground(AppTheme.TEXT_PRIMARY);
                }
                return this;
            }
        };
        table.getColumnModel().getColumn(0).setCellRenderer(paddedRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(paddedRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(paddedRenderer);

        // ── Role pill badge renderer  ─────────────
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
        
                String role = val == null ? "" : val.toString();
        
                Color pillBg, pillFg;
                switch (role) {
                    case "Manager":
                        pillBg = new Color(0xE6F1FB); pillFg = new Color(0x0C447C); break;
                    case "CounterStaff":
                        pillBg = new Color(0xFAEEDA); pillFg = new Color(0x633806); break;
                    case "Technician":
                        pillBg = new Color(0xE1F5EE); pillFg = new Color(0x085041); break;
                    default:
                        pillBg = AppTheme.BG_PAGE; pillFg = AppTheme.TEXT_MUTED;
                }
        
                String displayRole = role.equals("CounterStaff") ? "Counter Staff" : role;
        
                // Cell container: fully fills cell, keeps row selection color if needed
                JPanel cell = new JPanel(new GridBagLayout());
                cell.setOpaque(true);
                cell.setBackground(AppTheme.BG_CARD);
        
                final Color bg = pillBg;
                JLabel pill = new JLabel(displayRole) {
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
        
                // Left + vertical center
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.weightx = 1.0;
                gbc.anchor = GridBagConstraints.WEST;   // left
                gbc.insets = new Insets(0, 14, 0, 8);  // left/right padding
                cell.add(pill, gbc);
        
                return cell;
            }
        });

        // ── Actions renderer + editor ─────────────────────────────────────────
        table.getColumnModel().getColumn(4).setCellRenderer(new ActionsCellRenderer());
        table.getColumnModel().getColumn(4).setCellEditor(new ActionsCellEditor());

        // ── Scroll pane — no scrollbars since pagination handles overflow ─────
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(true);
        scroll.setBackground(AppTheme.BG_CARD);
        scroll.getViewport().setOpaque(true);
        scroll.getViewport().setBackground(AppTheme.BG_CARD);
        scroll.getViewport().setBorder(null);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // ── Pull header out and place manually so corners clip cleanly ────────
        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setOpaque(true);
        tableHeader.setBackground(AppTheme.BG_CARD);
        scroll.setColumnHeader(null);

        JPanel inner = new JPanel(new BorderLayout(0, 0));
        inner.setOpaque(true);
        inner.setBackground(AppTheme.BG_CARD);
        inner.setBorder(BorderFactory.createEmptyBorder());
        inner.add(tableHeader, BorderLayout.NORTH);
        inner.add(scroll, BorderLayout.CENTER);

        // ── JLayer clips everything to rounded rect and draws border on top ───
        final int r = AppTheme.RADIUS_CARD * 2;
        LayerUI<JPanel> cardLayerUI = new LayerUI<JPanel>() {
            @Override
            public void paint(Graphics g, JComponent c) {
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

        JLayer<JPanel> card = new JLayer<>(inner, cardLayerUI);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.setBorder(BorderFactory.createEmptyBorder());
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    // ─── Pagination bar ───────────────────────────────────────────────────────

    private JPanel buildPaginationBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblPagination = new JLabel("Showing 0 of 0 employees");
        lblPagination.setFont(AppTheme.FONT_CAPTION);
        lblPagination.setForeground(AppTheme.TEXT_MUTED);

        btnPrev = makePagButton("← Prev");
        btnNext = makePagButton("Next →");

        btnPrev.addActionListener(e -> { if (currentPage > 0) { currentPage--; renderPage(); } });
        btnNext.addActionListener(e -> {
            int totalPages = (int) Math.ceil((double) filteredList.size() / ROWS_PER_PAGE);
            if (currentPage < totalPages - 1) { currentPage++; renderPage(); }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(btnPrev);
        btnPanel.add(btnNext);

        bar.add(lblPagination, BorderLayout.WEST);
        bar.add(btnPanel, BorderLayout.EAST);
        return bar;
    }

    private JButton makePagButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (!isEnabled()) {
                    g2.setColor(AppTheme.BG_PAGE);
                } else if (getModel().isPressed()) {
                    g2.setColor(AppTheme.PRIMARY_LIGHT);
                } else if (getModel().isRollover()) {
                    g2.setColor(AppTheme.BG_PAGE);
                } else {
                    g2.setColor(AppTheme.BG_CARD);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? AppTheme.SEPARATOR : new Color(AppTheme.SEPARATOR.getRed(),
                        AppTheme.SEPARATOR.getGreen(), AppTheme.SEPARATOR.getBlue(), 80));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight() - 1, getHeight() - 1);
                g2.dispose();
            }
        };
        btn.setFont(AppTheme.FONT_SMALL);
        btn.setForeground(AppTheme.TEXT_PRIMARY);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(100, 36));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ─── Data loading ─────────────────────────────────────────────────────────

    public void loadData() {
        List<ManageEmployee> all = dao.getAllEmployees();

        // Update stat cards
        long mCount  = all.stream().filter(e -> "Manager".equals(e.getRole())).count();
        long csCount = all.stream().filter(e -> "CounterStaff".equals(e.getRole())).count();
        long tCount  = all.stream().filter(e -> "Technician".equals(e.getRole())).count();
        lblManagerCount.setText(String.valueOf(mCount));
        lblStaffCount.setText(String.valueOf(csCount));
        lblTechCount.setText(String.valueOf(tCount));

        currentPage = 0;
        applyFilter();
    }

    private void applyFilter() {
        String raw = txtSearch != null ? txtSearch.getText().trim() : "";
        String search = (raw.equals("Search by name or ID...")) ? "" : raw.toLowerCase();
        String roleFilter = cmbRoleFilter != null ? (String) cmbRoleFilter.getSelectedItem() : "All Roles";

        List<ManageEmployee> all = dao.getAllEmployees();

        filteredList = all.stream().filter(emp -> {
            boolean matchesRole = "All Roles".equals(roleFilter) || emp.getRole().equals(roleFilter);
            boolean matchesSearch = search.isEmpty()
                || emp.getId().toLowerCase().contains(search)
                || emp.getUsername().toLowerCase().contains(search);
            return matchesRole && matchesSearch;
        }).collect(Collectors.toList());

        renderPage();
    }

    private void renderPage() {
        tableModel.setRowCount(0);

        int total = filteredList.size();
        int from  = currentPage * ROWS_PER_PAGE;
        int to    = Math.min(from + ROWS_PER_PAGE, total);

        for (int i = from; i < to; i++) {
            ManageEmployee emp = filteredList.get(i);
            tableModel.addRow(new Object[]{
                emp.getId(), emp.getUsername(), emp.getRole(), emp.getEmail(), emp
            });
        }

        lblPagination.setText("Showing " + Math.min(to, total) + " of " + total + " employees");
        btnPrev.setEnabled(currentPage > 0);
        btnNext.setEnabled(to < total);
    }

    // ─── Add dialog ───────────────────────────────────────────────────────────

    private void showAddDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Add Employee", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(480, 540);
        dialog.setMinimumSize(new Dimension(480, 480));
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
    
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(AppTheme.BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
    
        JLabel titleLbl = UIComponent.createPageTitle("Add Employee");
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleLbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, titleLbl.getPreferredSize().height));
        form.add(titleLbl);
        form.add(UIComponent.verticalGap(16));
    
        JLabel roleLbl = UIComponent.createFieldLabel("ROLE");
        roleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        roleLbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, roleLbl.getPreferredSize().height));
        form.add(roleLbl);
        form.add(UIComponent.verticalGap(4));
        JComboBox<String> cmbRole = new JComboBox<>(new String[]{"Manager", "CounterStaff", "Technician"});
        cmbRole.setFont(AppTheme.FONT_SMALL);
        cmbRole.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cmbRole.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(cmbRole);
        form.add(UIComponent.verticalGap(12));
    
        JLabel usernameLbl = UIComponent.createFieldLabel("USERNAME");
        usernameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameLbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, usernameLbl.getPreferredSize().height));
        form.add(usernameLbl);
        form.add(UIComponent.verticalGap(4));
        JTextField txtUsername = UIComponent.createTextField("Enter username");
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(txtUsername);
        form.add(UIComponent.verticalGap(12));
    
        JLabel emailLbl = UIComponent.createFieldLabel("EMAIL");
        emailLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailLbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, emailLbl.getPreferredSize().height));
        form.add(emailLbl);
        form.add(UIComponent.verticalGap(4));
        JTextField txtEmail = UIComponent.createTextField("Enter email");
        txtEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        txtEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(txtEmail);
        form.add(UIComponent.verticalGap(12));
    
        JLabel passLbl = UIComponent.createFieldLabel("PASSWORD");
        passLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        passLbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, passLbl.getPreferredSize().height));
        form.add(passLbl);
        form.add(UIComponent.verticalGap(4));
        JPasswordField txtPass = UIComponent.createPasswordField();
        txtPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        txtPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(txtPass);
        form.add(UIComponent.verticalGap(12));
    
        JLabel confirmLbl = UIComponent.createFieldLabel("CONFIRM PASSWORD");
        confirmLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        confirmLbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, confirmLbl.getPreferredSize().height));
        form.add(confirmLbl);
        form.add(UIComponent.verticalGap(4));
        JPasswordField txtConfirm = UIComponent.createPasswordField();
        txtConfirm.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        txtConfirm.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(txtConfirm);
        form.add(UIComponent.verticalGap(20));
    
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
    
        JButton btnCancel = UIComponent.createOutlineButton("Cancel");
        btnCancel.setPreferredSize(new Dimension(100, 40));
        JButton btnSave = UIComponent.createPrimaryButton("Add");
        btnSave.setPreferredSize(new Dimension(100, 40));
    
        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            String role     = (String) cmbRole.getSelectedItem();
            String username = txtUsername.getText().trim();
            String email    = txtEmail.getText().trim();
            String pass     = new String(txtPass.getPassword()).trim();
            String confirm  = new String(txtConfirm.getPassword()).trim();
    
            if (username.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!pass.equals(confirm)) {
                JOptionPane.showMessageDialog(dialog, "Passwords do not match.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (pass.length() < 8 || pass.length() > 16) {
                JOptionPane.showMessageDialog(dialog, "Password must be 8–16 characters.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (username.contains("|") || email.contains("|") || pass.contains("|")) {
                JOptionPane.showMessageDialog(dialog, "Fields cannot contain '|'.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (dao.isEmailTaken(email)) {
                JOptionPane.showMessageDialog(dialog, "This email is already in use.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
    
            if (dao.addEmployee(role, username, email, pass)) {
                JOptionPane.showMessageDialog(dialog, "Employee added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadData();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to add employee.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    
        btnRow.add(btnCancel);
        btnRow.add(btnSave);
        form.add(btnRow);
    
        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(BorderFactory.createEmptyBorder());
        formScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        formScroll.getVerticalScrollBar().setUnitIncrement(12);
        dialog.add(formScroll, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    // ─── Edit dialog ──────────────────────────────────────────────────────────

    private void showEditDialog(ManageEmployee emp) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Edit Employee", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(480, 480);
        dialog.setMinimumSize(new Dimension(580, 420));
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(AppTheme.BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JLabel titleLbl = UIComponent.createPageTitle("Edit Employee");
        form.add(titleLbl);
        form.add(UIComponent.verticalGap(4));

        JLabel subLbl = UIComponent.createCaptionLabel(emp.getId() + " · " + emp.getRole());
        form.add(subLbl);
        form.add(UIComponent.verticalGap(16));

        form.add(UIComponent.createFieldLabel("USERNAME"));
        form.add(UIComponent.verticalGap(4));
        JTextField txtUsername = UIComponent.createTextField("Username");
        txtUsername.setText(emp.getUsername());
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        form.add(txtUsername);
        form.add(UIComponent.verticalGap(12));

        form.add(UIComponent.createFieldLabel("EMAIL"));
        form.add(UIComponent.verticalGap(4));
        JTextField txtEmail = UIComponent.createTextField("Email");
        txtEmail.setText(emp.getEmail());
        txtEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        form.add(txtEmail);
        form.add(UIComponent.verticalGap(12));

        form.add(UIComponent.createFieldLabel("NEW PASSWORD (leave blank to keep existing)"));
        form.add(UIComponent.verticalGap(4));
        JPasswordField txtPass = UIComponent.createPasswordField();
        txtPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        form.add(txtPass);
        form.add(UIComponent.verticalGap(20));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnCancel = UIComponent.createOutlineButton("Cancel");
        btnCancel.setPreferredSize(new Dimension(100, 40));
        JButton btnSave = UIComponent.createPrimaryButton("Save");
        btnSave.setPreferredSize(new Dimension(100, 40));

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            String newUsername = txtUsername.getText().trim();
            String newEmail    = txtEmail.getText().trim();
            String newPass     = new String(txtPass.getPassword()).trim();

            if (newUsername.isEmpty() || newEmail.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Username and Email cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (newUsername.contains("|") || newEmail.contains("|") || newPass.contains("|")) {
                JOptionPane.showMessageDialog(dialog, "Fields cannot contain '|'.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!newPass.isEmpty() && (newPass.length() < 8 || newPass.length() > 16)) {
                JOptionPane.showMessageDialog(dialog, "Password must be 8–16 characters.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (dao.isEmailTakenExcluding(newEmail, emp.getId())) {
                JOptionPane.showMessageDialog(dialog, "This email is already in use.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (dao.editEmployee(emp.getRole(), emp.getId(), newUsername, newEmail, newPass)) {
                JOptionPane.showMessageDialog(dialog, "Employee updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadData();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to update employee.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRow.add(btnCancel);
        btnRow.add(btnSave);
        form.add(btnRow);

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(BorderFactory.createEmptyBorder());
        formScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        formScroll.getVerticalScrollBar().setUnitIncrement(12);
        dialog.add(formScroll, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    private void handleDelete(ManageEmployee emp) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete " + emp.getUsername() + " (" + emp.getId() + ")? This action cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        if (dao.deleteEmployee(emp.getRole(), emp.getId())) {
            JOptionPane.showMessageDialog(this, "Employee deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to delete employee.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─── Actions cell renderer ────────────────────────────────────────────────

    private class ActionsCellRenderer implements javax.swing.table.TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean focus, int row, int col) {
            JPanel panel = buildActionsPanel(val instanceof ManageEmployee ? (ManageEmployee) val : null, false);
            panel.setOpaque(false);
            return panel;
        }
    }

    private class ActionsCellEditor extends DefaultCellEditor {
        private ManageEmployee current;

        public ActionsCellEditor() {
            super(new JCheckBox());
            setClickCountToStart(1);
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object val,
                boolean sel, int row, int col) {
            current = val instanceof ManageEmployee ? (ManageEmployee) val : null;
            return buildActionsPanel(current, true);
        }

        @Override public Object getCellEditorValue() { return current; }
    }

    private JPanel buildActionsPanel(ManageEmployee emp, boolean interactive) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        if (emp == null) return panel;

        String loggedInId = getLoggedInManagerId();
        boolean isProtected = isProtectedId(emp.getId());
        boolean isSelf = emp.getId().equals(loggedInId);

        JButton btnEdit   = makeActionButton("Edit",   false);
        JButton btnDelete = makeActionButton("Delete", true);

        if (isProtected || isSelf) {
            if (isProtected) {
                btnEdit.setEnabled(false);
                btnEdit.setForeground(AppTheme.TEXT_MUTED);
            }
            btnDelete.setEnabled(false);
            btnDelete.setForeground(AppTheme.TEXT_MUTED);
        }

        if (interactive) {
            btnEdit.addActionListener(e -> { fireEditingStopped(); showEditDialog(emp); });
            btnDelete.addActionListener(e -> { fireEditingStopped(); handleDelete(emp); });
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.gridy = 0;
        gbc.gridx = 0; panel.add(btnEdit,   gbc);
        gbc.gridx = 1; panel.add(btnDelete, gbc);
        return panel;
    }

    private JButton makeActionButton(String text, boolean danger) {
        final Color activeBorder = danger ? new Color(0xA32D2D) : AppTheme.SEPARATOR;
        final Color activeFg     = danger ? new Color(0xA32D2D) : AppTheme.TEXT_PRIMARY;
        final Color hoverBg      = danger ? new Color(0xFCEBEB) : AppTheme.BG_PAGE;
        final int ARC = 10;

        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isEnabled() && getModel().isRollover()) {
                    g2.setColor(hoverBg);
                    g2.fillRoundRect(0, 1, getWidth(), getHeight() - 2, ARC, ARC);
                }
                g2.dispose();
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color borderCol = isEnabled() ? activeBorder
                    : new Color(AppTheme.SEPARATOR.getRed(), AppTheme.SEPARATOR.getGreen(),
                                AppTheme.SEPARATOR.getBlue(), 100);
                g2.setColor(borderCol);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 1, getWidth() - 1, getHeight() - 2, ARC, ARC);
                g2.dispose();
            }
        };
        btn.setFont(AppTheme.FONT_LABEL);
        btn.setForeground(activeFg);
        btn.setBackground(new Color(0, 0, 0, 0));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false); // we paint it ourselves above
        btn.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        btn.setPreferredSize(new Dimension(74, 32));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void fireEditingStopped() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private String getLoggedInManagerId() {
        User user = frame.getLoggedInUser();
        return user != null ? user.getUserId() : "";
    }

    private boolean isProtectedId(String id) {
        for (String p : PROTECTED_IDS) {
            if (p.equals(id)) return true;
        }
        return false;
    }
}