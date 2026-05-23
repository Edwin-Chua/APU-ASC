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

    private static final String[] COLS = {
        "", "Customer ID", "Username", "Email", "Contact No.", "Status", "Registered Date", "Action", ""
    };

    public CounterStaffRegisterCustomer(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(0, 0));
        setBackground(AppTheme.BG_PAGE);
        setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_PAGE, AppTheme.PADDING_PAGE,
            AppTheme.PADDING_PAGE, AppTheme.PADDING_PAGE));
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
            @Override public Class<?> getColumnClass(int col) { return col == 0 ? Boolean.class : String.class; }
            @Override public boolean isCellEditable(int row, int col) { return col == 0; }
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
                if (!isRowSelected(row) && col != 0 && col != 5 && col != 7 && col != 8) {
                    c.setBackground(AppTheme.BG_CARD);
                    c.setForeground(col == 1 ? AppTheme.TEXT_SECONDARY : AppTheme.TEXT_PRIMARY);
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
                lbl.setHorizontalAlignment(col == 7 || col == 8 ? SwingConstants.CENTER : SwingConstants.LEFT);
                return lbl;
            }
        });
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);

        TableColumnModel cm = table.getColumnModel();
        cm.getColumn(0).setMinWidth(38);  cm.getColumn(0).setMaxWidth(38);
        cm.getColumn(1).setPreferredWidth(110);
        cm.getColumn(2).setPreferredWidth(140);
        cm.getColumn(3).setPreferredWidth(190);
        cm.getColumn(4).setPreferredWidth(130);
        cm.getColumn(5).setMinWidth(130); cm.getColumn(5).setPreferredWidth(140);
        cm.getColumn(6).setPreferredWidth(130);
        cm.getColumn(7).setMinWidth(60);  cm.getColumn(7).setMaxWidth(60);
        cm.getColumn(8).setMinWidth(90);  cm.getColumn(8).setMaxWidth(90);

        DefaultTableCellRenderer paddedRenderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 8));
                if (!sel) {
                    setBackground(AppTheme.BG_CARD);
                    setForeground(col == 1 ? AppTheme.TEXT_SECONDARY : AppTheme.TEXT_PRIMARY);
                }
                setFont(col == 1 ? AppTheme.FONT_LABEL : AppTheme.FONT_SMALL);
                return this;
            }
        };
        cm.getColumn(1).setCellRenderer(paddedRenderer);
        cm.getColumn(2).setCellRenderer(paddedRenderer);
        cm.getColumn(3).setCellRenderer(paddedRenderer);
        cm.getColumn(4).setCellRenderer(paddedRenderer);
        cm.getColumn(6).setCellRenderer(paddedRenderer);

        cm.getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                String status = val == null ? "" : val.toString();
                Color pillBg, pillFg;
                switch (status.toLowerCase()) {
                    case "approved": pillBg = new Color(0xE1F5EE); pillFg = new Color(0x085041); break;
                    case "pending":  pillBg = new Color(0xFAEEDA); pillFg = new Color(0x633806); break;
                    default:         pillBg = AppTheme.BG_PAGE;    pillFg = AppTheme.TEXT_MUTED;
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

        int[] hoveredRow = { -1 }, hoveredCol = { -1 };
        table.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int r = table.rowAtPoint(e.getPoint()), c = table.columnAtPoint(e.getPoint());
                if (r != hoveredRow[0] || c != hoveredCol[0]) {
                    hoveredRow[0] = r; hoveredCol[0] = c;
                    if (r >= 0) { table.repaint(table.getCellRect(r, 7, true)); table.repaint(table.getCellRect(r, 8, true)); }
                }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) { hoveredRow[0] = -1; hoveredCol[0] = -1; table.repaint(); }
        });

        cm.getColumn(7).setCellRenderer((t, val, sel, focus, row, col) -> {
            boolean hovered = (row == hoveredRow[0] && hoveredCol[0] == 7);
            JLabel lbl = new JLabel(hovered ? "<html><u>Edit</u></html>" : "Edit", SwingConstants.LEFT);
            lbl.setFont(AppTheme.FONT_SMALL); lbl.setForeground(AppTheme.PRIMARY);
            lbl.setCursor(new Cursor(Cursor.HAND_CURSOR)); lbl.setOpaque(true);
            lbl.setBackground(sel ? AppTheme.PRIMARY_LIGHT : AppTheme.BG_CARD);
            lbl.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            return lbl;
        });

        cm.getColumn(8).setCellRenderer((t, val, sel, focus, row, col) -> {
            String status = (String) tableModel.getValueAt(row, 5);
            boolean isPending = "Pending".equalsIgnoreCase(status);
            boolean hovered   = (row == hoveredRow[0] && hoveredCol[0] == 8);
            JLabel lbl = new JLabel();
            if (isPending) { lbl.setText(hovered ? "<html><u>Approve</u></html>" : "Approve"); lbl.setForeground(AppTheme.SUCCESS); lbl.setCursor(new Cursor(Cursor.HAND_CURSOR)); }
            lbl.setFont(AppTheme.FONT_SMALL); lbl.setOpaque(true);
            lbl.setBackground(sel ? AppTheme.PRIMARY_LIGHT : AppTheme.BG_CARD);
            lbl.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            return lbl;
        });

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint()), col = table.columnAtPoint(e.getPoint());
                if (row < 0) return;
                if (col == 7) {
                    showEditCustomerDialog(
                        (String) tableModel.getValueAt(row, 1), (String) tableModel.getValueAt(row, 2),
                        (String) tableModel.getValueAt(row, 3), (String) tableModel.getValueAt(row, 4),
                        (String) tableModel.getValueAt(row, 5));
                } else if (col == 8) {
                    String status = (String) tableModel.getValueAt(row, 5);
                    if (!"Pending".equalsIgnoreCase(status)) return;
                    String customerId = (String) tableModel.getValueAt(row, 1);
                    String username   = (String) tableModel.getValueAt(row, 2);
                    UIComponent.showDialog(CounterStaffRegisterCustomer.this, "Confirm Approval",
                        UIComponent.DialogType.WARNING,
                        "Approve customer \"" + username + "\" (" + customerId + ")?",
                        null, new UIComponent.DialogAction[]{
                            new UIComponent.DialogAction("Approve", UIComponent.DialogActionStyle.PRIMARY, () -> {
                                if (customerDAO.approveCustomer(customerId)) {
                                    UIComponent.showDialog(CounterStaffRegisterCustomer.this, "Approved",
                                        UIComponent.DialogType.SUCCESS,
                                        "Customer \"" + username + "\" has been approved.", null, null);
                                    loadTableData(searchField.getText().trim());
                                } else {
                                    UIComponent.showDialog(CounterStaffRegisterCustomer.this, "Error",
                                        UIComponent.DialogType.DANGER, "Failed to approve customer.", null, null);
                                }
                            }),
                            new UIComponent.DialogAction("Cancel", UIComponent.DialogActionStyle.OUTLINE, null)
                        });
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(true); scroll.setBackground(AppTheme.BG_CARD);
        scroll.getViewport().setOpaque(true); scroll.getViewport().setBackground(AppTheme.BG_CARD);
        scroll.getViewport().setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setOpaque(true); tableHeader.setBackground(AppTheme.BG_PAGE);
        scroll.setColumnHeader(null);

        JPanel inner = new JPanel(new BorderLayout(0, 0));
        inner.setOpaque(true); inner.setBackground(AppTheme.BG_CARD);
        inner.add(tableHeader, BorderLayout.NORTH);
        inner.add(scroll,      BorderLayout.CENTER);

        final int r = AppTheme.RADIUS_CARD * 2;
        LayerUI<JPanel> cardLayerUI = new LayerUI<JPanel>() {
            @Override public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.BG_CARD);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), r, r);
                g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, c.getWidth(), c.getHeight(), r, r));
                super.paint(g2, c);
                g2.setClip(null);
                g2.setColor(AppTheme.SEPARATOR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, r, r);
                g2.dispose();
            }
        };
        JLayer<JPanel> card = new JLayer<>(inner, cardLayerUI);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    // ─── Load table data ──────────────────────────────────────────────────────

    private void loadTableData(String query) {
        tableModel.setRowCount(0);
        String q = query.toLowerCase().trim();
        for (String[] c : customerDAO.getAllCustomers()) {
            if (c.length < 7) continue;
            String id = c[0].trim(), username = c[2].trim(), email = c[3].trim(),
                   contact = c[4].trim(), status = c[5].trim(), date = c[6].trim();
            if (q.isEmpty() || id.toLowerCase().contains(q)
                    || username.toLowerCase().contains(q) || email.toLowerCase().contains(q))
                tableModel.addRow(new Object[]{Boolean.FALSE, id, username, email, contact, status, date, "", ""});
        }
    }

    // ─── Delete selected ──────────────────────────────────────────────────────

    private void handleDeleteSelected() {
        List<String> toDelete = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++)
            if (Boolean.TRUE.equals(tableModel.getValueAt(i, 0)))
                toDelete.add((String) tableModel.getValueAt(i, 1));
        if (toDelete.isEmpty()) {
            UIComponent.showDialog(this, "No Selection", UIComponent.DialogType.INFO,
                "Please select at least one customer to delete.", null, null);
            return;
        }
        UIComponent.showDialog(this, "Confirm Delete", UIComponent.DialogType.DANGER,
            "Delete " + toDelete.size() + " selected customer(s)? This cannot be undone.",
            null, new UIComponent.DialogAction[]{
                new UIComponent.DialogAction("Delete", UIComponent.DialogActionStyle.DANGER, () -> {
                    int failed = 0;
                    for (String id : toDelete) if (!customerDAO.deleteCustomerById(id)) failed++;
                    if (failed > 0)
                        UIComponent.showDialog(this, "Partial Failure", UIComponent.DialogType.WARNING,
                            failed + " customer(s) could not be deleted.", null, null);
                    else
                        UIComponent.showDialog(this, "Deleted", UIComponent.DialogType.SUCCESS,
                            toDelete.size() + " customer(s) deleted successfully.", null, null);
                    searchField.setText(""); loadTableData("");
                }),
                new UIComponent.DialogAction("Cancel", UIComponent.DialogActionStyle.OUTLINE, null)
            });
    }

    // ─── Add Customer dialog ──────────────────────────────────────────────────

    private void showAddCustomerDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel root = buildDialogRootCard();
        root.setLayout(new BorderLayout(0, 10));
        root.setBorder(BorderFactory.createEmptyBorder(16, 14, 16, 14));
        root.add(buildDialogHeader("Add New Customer", dialog), BorderLayout.NORTH);

        // ── 2-column GridBag form ─────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        int[] r = {0};

        addSpan(form, UIComponent.createCaptionLabel("Fill in the details to register a new customer."), r, 10);

        JTextField     tfName  = addRow(form, "Username",         UIComponent.createTextField("Full name"), r);
        JTextField     tfEmail = addRow(form, "Email",            UIComponent.createTextField("Email address"), r);
        JTextField     tfPhone = addRow(form, "Contact No.",      UIComponent.createTextField("e.g. 0123456789"), r);
        JPasswordField tfPass  = addRow(form, "Password",         UIComponent.createPasswordField(), r);
        JPasswordField tfConf  = addRow(form, "Confirm Password", UIComponent.createPasswordField(), r);

        JComboBox<String> cbStatus = UIComponent.createComboBox(new String[]{"Pending", "Approved"});
        sizeField(cbStatus);
        addRow(form, "Status", cbStatus, r);

        JLabel errLabel = styledErrorLabel();
        addSpan(form, errLabel, r, 0);

        root.add(form, BorderLayout.CENTER);

        // ── Footer ────────────────────────────────────────────────────────────
        JButton btnCancel = UIComponent.createOutlineButton("Cancel");
        JButton btnSave   = UIComponent.createPrimaryButton("Register");
        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            String username = tfName.getText().trim(), email = tfEmail.getText().trim(),
                   contact  = tfPhone.getText().trim(), status = (String) cbStatus.getSelectedItem();
            String pass = new String(tfPass.getPassword()).trim(),
                   conf = new String(tfConf.getPassword()).trim();
            CustomerRegister reg = new CustomerRegister(username, contact, email, pass, conf);
            String err = reg.validate();
            if (err != null) { errLabel.setText(err); return; }
            String result = customerDAO.registerCustomer(username, pass, contact, email, status);
            if (result != null && result.toLowerCase().contains("success")) {
                dialog.dispose();
                UIComponent.showDialog(this, "Customer Registered", UIComponent.DialogType.SUCCESS,
                    "\"" + username + "\" has been registered successfully.", null, null);
                searchField.setText(""); loadTableData("");
            } else { errLabel.setText(result != null ? result : "Registration failed."); }
        });
        root.add(buildDialogFooter(btnCancel, btnSave), BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(300, dialog.getHeight()));
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    // ─── Edit Customer dialog ─────────────────────────────────────────────────

    private void showEditCustomerDialog(String customerId, String currentUsername,
            String currentEmail, String currentContact, String currentStatus) {

        String existingPassword = customerDAO.getPasswordById(customerId);
        if (existingPassword == null) existingPassword = "";

        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel root = buildDialogRootCard();
        root.setLayout(new BorderLayout(0, 10));
        root.setBorder(BorderFactory.createEmptyBorder(16, 14, 16, 14));
        root.add(buildDialogHeader("Edit Customer", dialog), BorderLayout.NORTH);

        // ── 2-column GridBag form ─────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        int[] r = {0};

        // Read-only ID badge row
        JPanel idRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        idRow.setOpaque(false);
        JLabel idBadge = new JLabel(customerId) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xE6F1FB));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        idBadge.setOpaque(false);
        idBadge.setFont(AppTheme.FONT_LABEL);
        idBadge.setForeground(new Color(0x0C447C));
        idBadge.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        idRow.add(idBadge);
        idRow.add(UIComponent.createCaptionLabel("  · ID cannot be changed"));
        addSpan(form, idRow, r, 10);

        JTextField tfName  = addRow(form, "Username",   UIComponent.createTextField("Full name"), r);
        JTextField tfEmail = addRow(form, "Email",      UIComponent.createTextField("Email address"), r);
        JTextField tfPhone = addRow(form, "Contact No.", UIComponent.createTextField("e.g. 0123456789"), r);
        tfName.setText(currentUsername);
        tfEmail.setText(currentEmail);
        tfPhone.setText(currentContact);

        // Password row with show/hide toggle
        JPasswordField tfPass = UIComponent.createPasswordField();
        tfPass.setText(existingPassword);
        tfPass.setEchoChar('•');
        JButton btnToggle = buildPasswordToggle(tfPass);
        btnToggle.setPreferredSize(new Dimension(34, 34));
        JPanel passPanel = new JPanel(new BorderLayout(4, 0));
        passPanel.setOpaque(false);
        passPanel.setPreferredSize(new Dimension(200, 34));
        passPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        passPanel.add(tfPass,    BorderLayout.CENTER);
        passPanel.add(btnToggle, BorderLayout.EAST);
        addRow(form, "Password", passPanel, r);

        JComboBox<String> cbStatus = UIComponent.createComboBox(new String[]{"Pending", "Approved"});
        cbStatus.setSelectedItem(currentStatus);
        sizeField(cbStatus);
        addRow(form, "Status", cbStatus, r);

        JLabel errLabel = styledErrorLabel();
        addSpan(form, errLabel, r, 0);

        root.add(form, BorderLayout.CENTER);

        // ── Footer ────────────────────────────────────────────────────────────
        JButton btnCancel = UIComponent.createOutlineButton("Cancel");
        JButton btnSave   = UIComponent.createPrimaryButton("Save Changes");
        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            String username = tfName.getText().trim(), email = tfEmail.getText().trim(),
                   contact  = tfPhone.getText().trim(), status = (String) cbStatus.getSelectedItem(),
                   password = new String(tfPass.getPassword()).trim();
            if (username.isEmpty()) { errLabel.setText("Username cannot be empty."); return; }
            if (email.isEmpty())    { errLabel.setText("Email cannot be empty."); return; }
            if (contact.isEmpty())  { errLabel.setText("Contact number cannot be empty."); return; }
            if (password.isEmpty()) { errLabel.setText("Password cannot be empty."); return; }
            if (customerDAO.emailExistsExcluding(email, customerId)) {
                errLabel.setText("Another account with this email already exists."); return;
            }
            if (customerDAO.updateCustomer(customerId, username, password, contact, email, status)) {
                dialog.dispose();
                UIComponent.showDialog(this, "Changes Saved", UIComponent.DialogType.SUCCESS,
                    "Customer \"" + username + "\" updated successfully.", null, null);
                loadTableData(searchField.getText().trim());
            } else { errLabel.setText("Update failed. Please try again."); }
        });
        root.add(buildDialogFooter(btnCancel, btnSave), BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(300, dialog.getHeight()));
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    // ─── Dialog helpers ───────────────────────────────────────────────────────

    private JPanel buildDialogRootCard() {
        JPanel root = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppTheme.RADIUS_CARD * 2, AppTheme.RADIUS_CARD * 2);
                g2.setColor(AppTheme.BORDER_CARD);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, AppTheme.RADIUS_CARD * 2, AppTheme.RADIUS_CARD * 2);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        return root;
    }

    private JPanel buildDialogHeader(String title, JDialog dialog) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.SEPARATOR),
            BorderFactory.createEmptyBorder(0, 0, 8, 0)));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(AppTheme.FONT_BODY_BOLD);
        titleLbl.setForeground(AppTheme.TEXT_PRIMARY);
        JButton btnX = new JButton("✕");
        btnX.setFont(AppTheme.FONT_SMALL_BOLD);
        btnX.setForeground(AppTheme.TEXT_MUTED);
        btnX.setContentAreaFilled(false);
        btnX.setBorderPainted(false);
        btnX.setFocusPainted(false);
        btnX.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnX.addActionListener(e -> dialog.dispose());
        btnX.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnX.setForeground(AppTheme.DANGER); }
            @Override public void mouseExited(MouseEvent e)  { btnX.setForeground(AppTheme.TEXT_MUTED); }
        });
        header.add(titleLbl, BorderLayout.WEST);
        header.add(btnX,     BorderLayout.EAST);
        return header;
    }

    private JPanel buildDialogFooter(JButton cancel, JButton primary) {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.SEPARATOR),
            BorderFactory.createEmptyBorder(8, 0, 0, 0)));
        footer.add(cancel);
        footer.add(primary);
        return footer;
    }

    /**
     * Adds a label + field as a two-column GridBag row.
     * Label is fixed-width on the left; field expands to fill the right.
     */
    private <T extends JComponent> T addRow(JPanel form, String label, T field, int[] rowIdx) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(AppTheme.FONT_SMALL);
        lbl.setForeground(AppTheme.TEXT_SECONDARY);
        lbl.setPreferredSize(new Dimension(90, 20));

        sizeField(field);

        GridBagConstraints gbcL = new GridBagConstraints();
        gbcL.gridx = 0; gbcL.gridy = rowIdx[0];
        gbcL.anchor = GridBagConstraints.WEST;
        gbcL.insets = new Insets(0, 0, 10, 14);

        GridBagConstraints gbcF = new GridBagConstraints();
        gbcF.gridx = 1; gbcF.gridy = rowIdx[0]++;
        gbcF.fill = GridBagConstraints.HORIZONTAL;
        gbcF.weightx = 1.0;
        gbcF.insets = new Insets(0, 0, 10, 0);

        form.add(lbl,   gbcL);
        form.add(field, gbcF);
        return field;
    }

    /** Adds a full-width component spanning both columns (subtitle, error). */
    private void addSpan(JPanel form, JComponent comp, int[] rowIdx, int bottomInset) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = rowIdx[0]++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, bottomInset, 0);
        form.add(comp, gbc);
    }

    /** Applies a compact uniform height to any input field. */
    private void sizeField(JComponent field) {
        field.setPreferredSize(new Dimension(160, 34));
        field.setMinimumSize(new Dimension(0,   34));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
    }

    private JLabel styledErrorLabel() {
        JLabel lbl = new JLabel(" ");
        lbl.setFont(AppTheme.FONT_SMALL);
        lbl.setForeground(AppTheme.DANGER);
        return lbl;
    }

    private JButton buildPasswordToggle(JPasswordField field) {
        JButton btn = UIComponent.createSmallButton("Show");
  
        btn.setPreferredSize(new Dimension(60, 34));
        btn.setToolTipText("Show / hide password");
        final boolean[] showing = {false};
        field.setEchoChar('•');
        btn.addActionListener(e -> {
            showing[0] = !showing[0];
            field.setEchoChar(showing[0] ? (char) 0 : '•');
            btn.setText(showing[0] ? "Hide" : "Show");
        });
        return btn;
    }

    private JPanel createCard() {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppTheme.RADIUS_CARD * 2, AppTheme.RADIUS_CARD * 2);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        return card;
    }
}