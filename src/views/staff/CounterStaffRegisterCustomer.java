package views.staff;

import models.dao.CustomerDAO;
import models.entities.CustomerRegister;
import views.MainFrame;
import views.shared.AppTheme;
import views.shared.UIComponent;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.plaf.LayerUI;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class CounterStaffRegisterCustomer extends JPanel {

    private final MainFrame frame;
    private final CustomerDAO customerDAO = new CustomerDAO();

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    // customer.txt: customer_id|password|customer_name|customer_email|customer_phone|status|register_date
    // Table cols:   [0]chk [1]ID [2]Username [3]Email [4]Contact [5]Status [6]RegDate [7]Edit [8]Approve
    private static final String[] COLS = {
        "", "Customer ID", "Username", "Email", "Contact No.", "Status", "Registered Date", "Action", ""
    };

    public CounterStaffRegisterCustomer(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(0, 0));
        setBackground(AppTheme.BG_PAGE);
        setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_PAGE, AppTheme.PADDING_PAGE,
            AppTheme.PADDING_PAGE, AppTheme.PADDING_PAGE
        ));

        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);

        loadTableData("");
    }

    // ─── Top bar ──────────────────────────────────────────────────────────────

    private JPanel buildTopBar() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);

        JLabel title = UIComponent.createPageTitle("Register Customer");
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(title);

        JPanel actions = new JPanel(new BorderLayout(8, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        // Left: search
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setOpaque(false);

        searchField = UIComponent.createTextField("Search by name, email or ID…");
        searchField.setPreferredSize(new Dimension(260, 38));
        searchField.addActionListener(e -> loadTableData(searchField.getText().trim()));

        JButton btnSearch = UIComponent.createSmallButton("Search");
        btnSearch.setPreferredSize(new Dimension(80, 38));
        btnSearch.addActionListener(e -> loadTableData(searchField.getText().trim()));

        leftPanel.add(searchField);
        leftPanel.add(Box.createRigidArea(new Dimension(8, 0)));
        leftPanel.add(btnSearch);

        // Right: Refresh | Delete | Add
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightPanel.setOpaque(false);

        JButton btnRefresh = UIComponent.createSmallButton("↺ Refresh");
        btnRefresh.setPreferredSize(new Dimension(110, 38));

        JButton btnDelete = UIComponent.createDangerButton("Delete");
        btnDelete.setPreferredSize(new Dimension(100, 38));
        btnDelete.setMaximumSize(new Dimension(150, 38));

        JButton btnAdd = UIComponent.createPrimaryButton("＋ Add Customer");
        btnAdd.setPreferredSize(new Dimension(160, 38));
        btnAdd.setMaximumSize(new Dimension(160, 38));

        btnRefresh.addActionListener(e -> { searchField.setText(""); loadTableData(""); });
        btnDelete.addActionListener(e -> handleDeleteSelected());
        btnAdd.addActionListener(e -> showAddCustomerDialog());

        rightPanel.add(btnRefresh);
        rightPanel.add(btnDelete);
        rightPanel.add(btnAdd);

        actions.add(leftPanel,  BorderLayout.WEST);
        actions.add(rightPanel, BorderLayout.EAST);

        wrapper.add(actions);
        wrapper.add(Box.createRigidArea(new Dimension(0, 16)));
        return wrapper;
    }

    // ─── Table card ───────────────────────────────────────────────────────────

    private JPanel buildTableCard() {
        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public Class<?> getColumnClass(int col) {
                return col == 0 ? Boolean.class : String.class;
            }
            @Override public boolean isCellEditable(int row, int col) {
                return col == 0;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(AppTheme.ROW_HEIGHT_TABLE);
        table.setFont(AppTheme.FONT_SMALL);
        table.setForeground(AppTheme.TEXT_PRIMARY);
        table.setBackground(AppTheme.BG_CARD);
        table.setGridColor(AppTheme.SEPARATOR);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(false);
        table.setSelectionBackground(AppTheme.PRIMARY_LIGHT);
        table.setSelectionForeground(AppTheme.TEXT_PRIMARY);
        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Header
        JTableHeader header = table.getTableHeader();
        header.setFont(AppTheme.FONT_LABEL);
        header.setBackground(AppTheme.BG_PAGE);
        header.setForeground(AppTheme.TEXT_SECONDARY);
        header.setPreferredSize(new Dimension(0, 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER_DEFAULT));
        header.setOpaque(true);
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
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
        for (int i = 0; i < COLS.length; i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        // Column widths
        TableColumnModel cm = table.getColumnModel();
        cm.getColumn(0).setMinWidth(38);  cm.getColumn(0).setMaxWidth(38);
        cm.getColumn(1).setPreferredWidth(110);
        cm.getColumn(2).setPreferredWidth(140);
        cm.getColumn(3).setPreferredWidth(180);
        cm.getColumn(4).setPreferredWidth(130);
        cm.getColumn(5).setPreferredWidth(100);
        cm.getColumn(6).setPreferredWidth(130);
        cm.getColumn(7).setPreferredWidth(60);  cm.getColumn(7).setMinWidth(60);  cm.getColumn(7).setMaxWidth(60);
        cm.getColumn(8).setPreferredWidth(90);  cm.getColumn(8).setMinWidth(90);  cm.getColumn(8).setMaxWidth(90);

        Color ROW_SEPARATOR = new Color(220, 220, 225);

        // Alternating row renderer
        DefaultTableCellRenderer rowRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, ROW_SEPARATOR),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)
                ));
                setFont(AppTheme.FONT_SMALL);
                if (sel) {
                    setBackground(AppTheme.PRIMARY_LIGHT);
                    setForeground(AppTheme.TEXT_PRIMARY);
                } else {
                    setBackground(row % 2 == 0 ? AppTheme.BG_CARD : new Color(248, 248, 250));
                    setForeground(AppTheme.TEXT_PRIMARY);
                }
                return this;
            }
        };

        // Status renderer
        DefaultTableCellRenderer statusRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                JLabel lbl = new JLabel(val != null ? val.toString() : "");
                lbl.setOpaque(true);
                lbl.setFont(AppTheme.FONT_SMALL);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, ROW_SEPARATOR),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)
                ));
                lbl.setBackground(sel ? AppTheme.PRIMARY_LIGHT
                    : (row % 2 == 0 ? AppTheme.BG_CARD : new Color(248, 248, 250)));
                String status = val != null ? val.toString().toLowerCase() : "";
                switch (status) {
                    case "approved": lbl.setForeground(AppTheme.STATUS_COMPLETED_FG); break;
                    case "pending":  lbl.setForeground(AppTheme.STATUS_PENDING_FG);   break;
                    default:         lbl.setForeground(AppTheme.TEXT_SECONDARY);
                }
                return lbl;
            }
        };

        // Apply base renderers
        for (int i = 1; i < COLS.length; i++) {
            if (i == 7 || i == 8) continue; // action columns set separately below
            table.getColumnModel().getColumn(i).setCellRenderer(
                i == 5 ? statusRenderer : rowRenderer
            );
        }

        // Hover tracking for action columns
        int[] hoveredRow = { -1 };
        int[] hoveredCol = { -1 };
        table.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row != hoveredRow[0] || col != hoveredCol[0]) {
                    hoveredRow[0] = row;
                    hoveredCol[0] = col;
                    if (row >= 0) {
                        table.repaint(table.getCellRect(row, 7, true));
                        table.repaint(table.getCellRect(row, 8, true));
                    }
                }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) {
                hoveredRow[0] = -1;
                hoveredCol[0] = -1;
                table.repaint();
            }
        });

        // Edit renderer (col 7)
        TableCellRenderer editRenderer = new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                boolean hovered = (row == hoveredRow[0] && hoveredCol[0] == 7);
                JLabel lbl = new JLabel(hovered ? "<html><u>Edit</u></html>" : "Edit", SwingConstants.LEFT);
                lbl.setFont(AppTheme.FONT_SMALL);
                lbl.setForeground(AppTheme.PRIMARY);
                lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
                lbl.setOpaque(true);
                lbl.setBackground(sel ? AppTheme.PRIMARY_LIGHT
                    : (row % 2 == 0 ? AppTheme.BG_CARD : new Color(248, 248, 250)));
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, ROW_SEPARATOR),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)
                ));
                return lbl;
            }
        };

        // Approve renderer (col 8) — only visible for Pending rows
        TableCellRenderer approveRenderer = new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                Color bg = sel ? AppTheme.PRIMARY_LIGHT
                    : (row % 2 == 0 ? AppTheme.BG_CARD : new Color(248, 248, 250));
                String status = (String) tableModel.getValueAt(row, 5);
                boolean isPending = "Pending".equalsIgnoreCase(status);
                boolean hovered   = (row == hoveredRow[0] && hoveredCol[0] == 8);
                JLabel lbl = new JLabel();
                if (isPending) {
                    lbl.setText(hovered ? "<html><u>Approve</u></html>" : "Approve");
                    lbl.setForeground(AppTheme.SUCCESS);
                    lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
                lbl.setFont(AppTheme.FONT_SMALL);
                lbl.setOpaque(true);
                lbl.setBackground(bg);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, ROW_SEPARATOR),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)
                ));
                return lbl;
            }
        };

        table.getColumnModel().getColumn(7).setCellRenderer(editRenderer);
        table.getColumnModel().getColumn(8).setCellRenderer(approveRenderer);

        // Click handler for Edit (col 7) and Approve (col 8)
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row < 0) return;

                if (col == 7) {
                    // ── Edit ──────────────────────────────────────────────────
                    String customerId = (String) tableModel.getValueAt(row, 1);
                    String username   = (String) tableModel.getValueAt(row, 2);
                    String email      = (String) tableModel.getValueAt(row, 3);
                    String contact    = (String) tableModel.getValueAt(row, 4);
                    String status     = (String) tableModel.getValueAt(row, 5);
                    showEditCustomerDialog(customerId, username, email, contact, status);

                } else if (col == 8) {
                    // ── Approve ───────────────────────────────────────────────
                    String status = (String) tableModel.getValueAt(row, 5);
                    if (!"Pending".equalsIgnoreCase(status)) return;

                    String customerId = (String) tableModel.getValueAt(row, 1);
                    String username   = (String) tableModel.getValueAt(row, 2);

                    int confirm = JOptionPane.showConfirmDialog(
                        CounterStaffRegisterCustomer.this,
                        "Approve customer \"" + username + "\" (" + customerId + ")?",
                        "Confirm Approve", JOptionPane.YES_NO_OPTION);
                    if (confirm != JOptionPane.YES_OPTION) return;

                    boolean success = customerDAO.approveCustomer(customerId);
                    if (success) {
                        JOptionPane.showMessageDialog(
                            CounterStaffRegisterCustomer.this,
                            "Customer \"" + username + "\" approved successfully!",
                            "Approved", JOptionPane.INFORMATION_MESSAGE);
                        loadTableData(searchField.getText().trim());
                    } else {
                        JOptionPane.showMessageDialog(
                            CounterStaffRegisterCustomer.this,
                            "Failed to approve customer.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        // ── Scroll + rounded card ─────────────────────────────────────────────
        final int r = AppTheme.RADIUS_CARD * 2;
        javax.swing.UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(true);
        scroll.setBackground(AppTheme.BG_CARD);
        scroll.getViewport().setOpaque(true);
        scroll.getViewport().setBackground(AppTheme.BG_CARD);
        scroll.getViewport().setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

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
        wrapper.setBorder(BorderFactory.createEmptyBorder());
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    // ─── Load / filter table data ─────────────────────────────────────────────
    // customer.txt format: customer_id|password|customer_name|customer_email|customer_phone|status|register_date
    // indices:              0           1         2              3               4              5       6

    private void loadTableData(String query) {
        tableModel.setRowCount(0);
        List<String[]> customers = customerDAO.getAllCustomers();
        String q = query.toLowerCase().trim();

        for (String[] c : customers) {
            if (c.length < 7) continue;          // ✅ Fixed: was < 8 (only 7 fields exist)
            String id          = c[0].trim();
            String username    = c[2].trim();    // ✅ Fixed: customer_name  (was c[1] = password)
            String email       = c[3].trim();    // customer_email
            String contactNum  = c[4].trim();    // customer_phone
            String status      = c[5].trim();    // ✅ Fixed: status         (was c[6] = register_date)
            String createdDate = c[6].trim();    // ✅ Fixed: register_date  (was c[7] = out of bounds)

            boolean matches = q.isEmpty()
                || id.toLowerCase().contains(q)
                || username.toLowerCase().contains(q)
                || email.toLowerCase().contains(q);

            if (matches) {
                tableModel.addRow(new Object[]{
                    Boolean.FALSE, id, username, email, contactNum, status, createdDate, "", ""
                });
            }
        }
    }

    // ─── Delete selected ──────────────────────────────────────────────────────

    private void handleDeleteSelected() {
        List<String> toDelete = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (Boolean.TRUE.equals(tableModel.getValueAt(i, 0))) {
                toDelete.add((String) tableModel.getValueAt(i, 1));
            }
        }

        if (toDelete.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please select at least one customer to delete.",
                "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete " + toDelete.size() + " selected customer(s)?\nThis action cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        int failed = 0;
        for (String id : toDelete) {
            if (!customerDAO.deleteCustomerById(id)) failed++;
        }

        if (failed > 0) {
            JOptionPane.showMessageDialog(this,
                failed + " customer(s) could not be deleted.",
                "Partial Failure", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                toDelete.size() + " customer(s) deleted successfully.",
                "Deleted", JOptionPane.INFORMATION_MESSAGE);
        }

        searchField.setText("");
        loadTableData("");
    }

    // ─── Add Customer dialog ──────────────────────────────────────────────────

    private void showAddCustomerDialog() {
        Window ancestor = SwingUtilities.getWindowAncestor(this);
        Frame owner = (ancestor instanceof Frame) ? (Frame) ancestor : null;
        JDialog dialog = new JDialog(owner, "Add New Customer", true);
        dialog.setSize(480, 590);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(AppTheme.BG_PAGE);
        root.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JLabel dlgTitle = UIComponent.createPageTitle("Add New Customer");
        dlgTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        root.add(dlgTitle);

        JLabel dlgSubtitle = UIComponent.createCaptionLabel("Fill in the details below to register a new customer.");
        dlgSubtitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        root.add(dlgSubtitle);

        JLabel lblName    = UIComponent.createFieldLabel("USERNAME");
        JTextField tfName = UIComponent.createTextField("Full name");

        JLabel lblEmail    = UIComponent.createFieldLabel("EMAIL");
        JTextField tfEmail = UIComponent.createTextField("Email address");

        JLabel lblPhone    = UIComponent.createFieldLabel("CONTACT NO.");
        JTextField tfPhone = UIComponent.createTextField("e.g. 0123456789");

        JLabel lblPass         = UIComponent.createFieldLabel("PASSWORD");
        JPasswordField tfPass  = UIComponent.createPasswordField();

        JLabel lblConfirm          = UIComponent.createFieldLabel("CONFIRM PASSWORD");
        JPasswordField tfConfirm   = UIComponent.createPasswordField();

        JLabel lblStatus = UIComponent.createFieldLabel("STATUS");
        JComboBox<String> cbStatus = UIComponent.createComboBox(new String[]{"Pending", "Approved"});
        cbStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cbStatus.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel errLabel = new JLabel(" ");
        errLabel.setFont(AppTheme.FONT_SMALL);
        errLabel.setForeground(AppTheme.DANGER);
        errLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel[]     labels = { lblName, lblEmail, lblPhone, lblPass, lblConfirm, lblStatus };
        JComponent[] fields = { tfName,  tfEmail,  tfPhone,  tfPass,  tfConfirm,  cbStatus  };
        for (int i = 0; i < labels.length; i++) {
            root.add(labels[i]);
            root.add(Box.createRigidArea(new Dimension(0, 5)));
            root.add(fields[i]);
            root.add(Box.createRigidArea(new Dimension(0, 12)));
        }

        root.add(errLabel);
        root.add(Box.createRigidArea(new Dimension(0, 16)));

        JButton btnSave   = UIComponent.createPrimaryButton("Register Customer");
        JButton btnCancel = UIComponent.createOutlineButton("Cancel");
        btnSave.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSave.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnCancel.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnCancel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            String username = tfName.getText().trim();
            String email    = tfEmail.getText().trim();
            String contact  = tfPhone.getText().trim();
            String pass     = new String(tfPass.getPassword()).trim();
            String conf     = new String(tfConfirm.getPassword()).trim();
            String status   = (String) cbStatus.getSelectedItem();

            CustomerRegister reg = new CustomerRegister(username, contact, email, pass, conf);
            String validationErr = reg.validate();
            if (validationErr != null) { errLabel.setText(validationErr); return; }

            // registerCustomer(name, password, phone, email, status)
            String result = customerDAO.registerCustomer(username, pass, contact, email, status);
            if (result != null && result.toLowerCase().contains("success")) {
                JOptionPane.showMessageDialog(dialog,
                    "Customer \"" + username + "\" registered successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                searchField.setText("");
                loadTableData("");
            } else {
                errLabel.setText(result != null ? result : "Registration failed.");
            }
        });

        root.add(btnSave);
        root.add(Box.createRigidArea(new Dimension(0, 8)));
        root.add(btnCancel);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    // ─── Edit Customer dialog ─────────────────────────────────────────────────

    private void showEditCustomerDialog(String customerId, String currentUsername,
            String currentEmail, String currentContact, String currentStatus) {

        String existingPassword = customerDAO.getPasswordById(customerId);
        if (existingPassword == null) existingPassword = "";

        Window ancestor = SwingUtilities.getWindowAncestor(this);
        Frame owner = (ancestor instanceof Frame) ? (Frame) ancestor : null;
        JDialog dialog = new JDialog(owner, "Edit Customer", true);
        dialog.setSize(480, 660);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(AppTheme.BG_PAGE);
        root.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JLabel dlgTitle = UIComponent.createPageTitle("Edit Customer");
        dlgTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        root.add(dlgTitle);

        JLabel dlgSubtitle = UIComponent.createCaptionLabel("Customer ID: " + customerId + " (cannot be changed)");
        dlgSubtitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        root.add(dlgSubtitle);

        // Fields pre-filled with current values
        JLabel lblName    = UIComponent.createFieldLabel("USERNAME");
        JTextField tfName = UIComponent.createTextField("Full name");
        tfName.setText(currentUsername);

        JLabel lblEmail    = UIComponent.createFieldLabel("EMAIL");
        JTextField tfEmail = UIComponent.createTextField("Email address");
        tfEmail.setText(currentEmail);

        JLabel lblPhone    = UIComponent.createFieldLabel("CONTACT NO.");
        JTextField tfPhone = UIComponent.createTextField("e.g. 0123456789");
        tfPhone.setText(currentContact);

        // Password row with show/hide toggle
        JLabel lblPass     = UIComponent.createFieldLabel("PASSWORD");
        JPasswordField tfPass = UIComponent.createPasswordField();
        tfPass.setText(existingPassword);
        tfPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tfPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnToggle = new JButton("👁") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppTheme.RADIUS_BUTTON * 2, AppTheme.RADIUS_BUTTON * 2);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnToggle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        btnToggle.setFocusPainted(false);
        btnToggle.setBorderPainted(false);
        btnToggle.setContentAreaFilled(false);
        btnToggle.setOpaque(false);
        btnToggle.setBackground(AppTheme.BG_FIELD);
        btnToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnToggle.setPreferredSize(new Dimension(38, 38));
        btnToggle.setToolTipText("Show / hide password");

        final boolean[] showing = { false };
        tfPass.setEchoChar('•');
        btnToggle.addActionListener(e -> {
            showing[0] = !showing[0];
            tfPass.setEchoChar(showing[0] ? (char) 0 : '•');
            btnToggle.setText(showing[0] ? "🙈" : "👁");
        });

        JPanel passRow = new JPanel(new BorderLayout(6, 0));
        passRow.setOpaque(false);
        passRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        passRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        passRow.add(tfPass, BorderLayout.CENTER);
        passRow.add(btnToggle, BorderLayout.EAST);

        // Status dropdown
        JLabel lblStatus = UIComponent.createFieldLabel("STATUS");
        JComboBox<String> cbStatus = UIComponent.createComboBox(new String[]{"Pending", "Approved"});
        cbStatus.setSelectedItem(currentStatus);
        cbStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cbStatus.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel errLabel = new JLabel(" ");
        errLabel.setFont(AppTheme.FONT_SMALL);
        errLabel.setForeground(AppTheme.DANGER);
        errLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel[]     labels = { lblName, lblEmail, lblPhone, lblPass,   lblStatus };
        JComponent[] fields = { tfName,  tfEmail,  tfPhone,  passRow,   cbStatus  };
        for (int i = 0; i < labels.length; i++) {
            root.add(labels[i]);
            root.add(Box.createRigidArea(new Dimension(0, 5)));
            root.add(fields[i]);
            root.add(Box.createRigidArea(new Dimension(0, 12)));
        }

        root.add(errLabel);
        root.add(Box.createRigidArea(new Dimension(0, 16)));

        JButton btnSave   = UIComponent.createPrimaryButton("Save Changes");
        JButton btnCancel = UIComponent.createOutlineButton("Cancel");
        btnSave.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSave.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnCancel.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnCancel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            String username = tfName.getText().trim();
            String email    = tfEmail.getText().trim();
            String contact  = tfPhone.getText().trim();
            String password = new String(tfPass.getPassword()).trim();
            String status   = (String) cbStatus.getSelectedItem();

            // Basic validation
            if (username.isEmpty()) { errLabel.setText("Username cannot be empty."); return; }
            if (email.isEmpty())    { errLabel.setText("Email cannot be empty."); return; }
            if (contact.isEmpty())  { errLabel.setText("Contact number cannot be empty."); return; }
            if (password.isEmpty()) { errLabel.setText("Password cannot be empty."); return; }

            // Duplicate email check — allow the customer to keep their own email
            if (customerDAO.emailExistsExcluding(email, customerId)) {
                errLabel.setText("Another account with this email already exists.");
                return;
            }

            // updateCustomer(id, name, password, phone, email, status)
            boolean success = customerDAO.updateCustomer(customerId, username, password, contact, email, status);
            if (success) {
                JOptionPane.showMessageDialog(dialog,
                    "Customer updated successfully!", "Saved", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadTableData(searchField.getText().trim());
            } else {
                errLabel.setText("Update failed. Please try again.");
            }
        });

        root.add(btnSave);
        root.add(Box.createRigidArea(new Dimension(0, 8)));
        root.add(btnCancel);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    // ─── Card helper ──────────────────────────────────────────────────────────

    private JPanel createCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(),
                    AppTheme.RADIUS_CARD * 2, AppTheme.RADIUS_CARD * 2);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        return card;
    }
}