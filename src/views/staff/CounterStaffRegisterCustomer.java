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

    // File 1 columns (original content)
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

    // ─── Top bar: title + search + action buttons (File 2 layout) ─────────────

    private JPanel buildTopBar() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);

        // Page title
        JLabel title = UIComponent.createPageTitle("Register Customer");
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(title);

        // Action row
        JPanel actions = new JPanel(new BorderLayout(8, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        // ── Left: search + search button ──────────────────────────────────────
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

        // ── Right: Refresh | Delete Selected | + Add Customer ────────────────
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

        btnRefresh.addActionListener(e -> {
            searchField.setText("");
            loadTableData("");
        });
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

    // ─── Table card (File 2 card style + File 1 columns & renderers) ──────────

    private JPanel buildTableCard() {
        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public Class<?> getColumnClass(int col) {
                return col == 0 ? Boolean.class : String.class;
            }
            @Override public boolean isCellEditable(int row, int col) {
                return col == 0; // only checkbox column is editable
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

        // ── Header styling ─────────────────────────────────────────────────────
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

        // ── Column widths (File 1 proportions) ────────────────────────────────
        TableColumnModel cm = table.getColumnModel();
        cm.getColumn(0).setMinWidth(38);
        cm.getColumn(0).setMaxWidth(38);
        cm.getColumn(1).setPreferredWidth(110);
        cm.getColumn(2).setPreferredWidth(140);
        cm.getColumn(3).setPreferredWidth(180);
        cm.getColumn(4).setPreferredWidth(130);
        cm.getColumn(5).setPreferredWidth(100);
        cm.getColumn(6).setPreferredWidth(130);
        cm.getColumn(7).setPreferredWidth(60);
        cm.getColumn(7).setMinWidth(60);
        cm.getColumn(7).setMaxWidth(60);
        cm.getColumn(8).setPreferredWidth(90);
        cm.getColumn(8).setMinWidth(90);
        cm.getColumn(8).setMaxWidth(90);
        
		// Separator color — visible on both white and grey rows
		Color ROW_SEPARATOR = new Color(220, 220, 225);
		
		// ── Alternating row renderer ───────────────────────────────────────────
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
		        if (sel) {
		            lbl.setBackground(AppTheme.PRIMARY_LIGHT);
		        } else {
		            lbl.setBackground(row % 2 == 0 ? AppTheme.BG_CARD : new Color(248, 248, 250));
		        }
		        String status = val != null ? val.toString().toLowerCase() : "";
		        switch (status) {
		            case "approved": lbl.setForeground(AppTheme.STATUS_COMPLETED_FG); break;
		            case "pending":  lbl.setForeground(AppTheme.STATUS_PENDING_FG);   break;
		            default:         lbl.setForeground(AppTheme.TEXT_SECONDARY);
		        }
		        return lbl;
		    }
		};
		
		// Apply renderers — col 5 = Status, col 7 = Action button
		for (int i = 1; i < COLS.length; i++) {
		    if (i == 7) continue; // action column uses custom renderer/editor below
		    table.getColumnModel().getColumn(i).setCellRenderer(
		        i == 5 ? statusRenderer : rowRenderer
		    );
		}

		// ── Edit button renderer ──────────────────────────────────────────────
		// Track hovered cell — only the Action column (col 7) gets the underline
		int[] hoveredRow = { -1 };
		int[] hoveredCol = { -1 };
		table.addMouseMotionListener(new MouseAdapter() {
		    @Override public void mouseMoved(MouseEvent e) {
		        int row = table.rowAtPoint(e.getPoint());
		        int col = table.columnAtPoint(e.getPoint());
		        if (row != hoveredRow[0] || col != hoveredCol[0]) {
		            hoveredRow[0] = row;
		            hoveredCol[0] = col;
		            // Only repaint action columns to avoid full-table flicker
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

		TableCellRenderer editRenderer = new TableCellRenderer() {
		    @Override
		    public Component getTableCellRendererComponent(
		            JTable t, Object val, boolean sel, boolean focus, int row, int col) {
		        boolean hovered = (row == hoveredRow[0] && hoveredCol[0] == 7);
		        String labelText = hovered ? "<html><u>Edit</u></html>" : "Edit";
		        JLabel lbl = new JLabel(labelText, SwingConstants.LEFT);
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

		// ── Approve renderer — only shows for Pending customers ───────────────
		TableCellRenderer approveRenderer = new TableCellRenderer() {
		    @Override
		    public Component getTableCellRendererComponent(
		            JTable t, Object val, boolean sel, boolean focus, int row, int col) {
		        Color bg = sel ? AppTheme.PRIMARY_LIGHT
		            : (row % 2 == 0 ? AppTheme.BG_CARD : new Color(248, 248, 250));
		        String status = (String) tableModel.getValueAt(row, 5);
		        boolean isPending = "Pending".equalsIgnoreCase(status);
		        boolean hovered = (row == hoveredRow[0] && hoveredCol[0] == 8);
		        JLabel lbl = new JLabel();
		        if (isPending) {
			        String labelText = hovered ? "<html><u>Approve</u></html>" : "Approve";
			        lbl.setText(labelText);
		            lbl.setForeground(AppTheme.SUCCESS);
		            lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
		        } else {
		            lbl.setText("");
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

		// ── Single-click on col 7 = Edit, col 8 = Approve ──────────────────
		table.addMouseListener(new MouseAdapter() {
		    @Override public void mouseClicked(MouseEvent e) {
		        int row = table.rowAtPoint(e.getPoint());
		        int col = table.columnAtPoint(e.getPoint());
		        if (row < 0) return;
		        if (col == 7) {
		            String customerId = (String) tableModel.getValueAt(row, 1);
		            String username   = (String) tableModel.getValueAt(row, 2);
		            String email      = (String) tableModel.getValueAt(row, 3);
		            String contact    = (String) tableModel.getValueAt(row, 4);
		            String status     = (String) tableModel.getValueAt(row, 5);
		            showEditCustomerDialog(customerId, username, email, contact, status);
		        } else if (col == 8) {
		            String status = (String) tableModel.getValueAt(row, 5);
		            if (!"Pending".equalsIgnoreCase(status)) return;
		            String customerId = (String) tableModel.getValueAt(row, 1);
		            String username   = (String) tableModel.getValueAt(row, 2);
		            String contact    = (String) tableModel.getValueAt(row, 4);
		            String email      = (String) tableModel.getValueAt(row, 3);
		            int confirm = JOptionPane.showConfirmDialog(
		                CounterStaffRegisterCustomer.this,
		                "Approve customer " + username + " (" + customerId + ")?",
		                "Confirm Approve", JOptionPane.YES_NO_OPTION);
		            if (confirm != JOptionPane.YES_OPTION) return;
		            boolean success = customerDAO.approveCustomer(customerId);
		            if (success) {
		                JOptionPane.showMessageDialog(
		                    CounterStaffRegisterCustomer.this,
		                    "Customer" + username + " approved successfully!",
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

		table.getColumnModel().getColumn(7).setCellRenderer(editRenderer);
		table.getColumnModel().getColumn(8).setCellRenderer(approveRenderer);

        final int r = AppTheme.RADIUS_CARD * 2;

        // Remove the UIManager default border that JScrollPane always re-applies
        javax.swing.UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());

        // ── Scroll pane: purely for scrolling, no decoration ──────────────────
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(true);
        scroll.setBackground(AppTheme.BG_CARD);
        scroll.getViewport().setOpaque(true);
        scroll.getViewport().setBackground(AppTheme.BG_CARD);
        scroll.getViewport().setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Pull the header out so the card clips both header and body together
        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setOpaque(true);
        tableHeader.setBackground(AppTheme.BG_CARD);
        scroll.setColumnHeader(null);

        // ── Inner panel: holds header + scroll body, both white ───────────────
        JPanel inner = new JPanel(new BorderLayout(0, 0));
        inner.setOpaque(true);
        inner.setBackground(AppTheme.BG_CARD);
        inner.setBorder(BorderFactory.createEmptyBorder());
        inner.add(tableHeader, BorderLayout.NORTH);
        inner.add(scroll, BorderLayout.CENTER);

        // ── Card via JLayer: clips inner (header + body) to rounded shape ─────
        // JLayer is the only approach that reliably clips opaque Swing children
        // because its LayerUI.paint() wraps the entire component tree.
        LayerUI<JPanel> cardLayerUI = new LayerUI<JPanel>() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Paint white rounded background first
                g2.setColor(AppTheme.BG_CARD);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), r, r);
                // Clip to same rounded shape before painting children
                g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, c.getWidth(), c.getHeight(), r, r));
                super.paint(g2, c);
                // Draw rounded border in separator color on top
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

    // ─── Load / filter table data (File 1 format) ─────────────────────────────
    // customer.txt: UserID|username|password|contactNum|email|role|status|createdDate

    private void loadTableData(String query) {
        tableModel.setRowCount(0);
        List<String[]> customers = customerDAO.getAllCustomers();
        String q = query.toLowerCase().trim();

        for (String[] c : customers) {
            if (c.length < 8) continue;
            String id          = c[0];
            String username    = c[1];
            String contactNum  = c[3];
            String email       = c[4];
            String status      = c[6];
            String createdDate = c[7];

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

    // ─── Delete selected (File 1 logic, File 2 style) ─────────────────────────

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

    // ─── Add Customer dialog (File 2 form style) ──────────────────────────────

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

        // Title + subtitle
        JLabel dlgTitle = UIComponent.createPageTitle("Add New Customer");
        dlgTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        root.add(dlgTitle);

        JLabel dlgSubtitle = UIComponent.createCaptionLabel("Fill in the details below to register a new customer.");
        dlgSubtitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        root.add(dlgSubtitle);

        // Form fields
        JLabel lblName    = UIComponent.createFieldLabel("USERNAME");
        JTextField tfName = UIComponent.createTextField("Full name");

        JLabel lblEmail    = UIComponent.createFieldLabel("EMAIL");
        JTextField tfEmail = UIComponent.createTextField("Email address");

        JLabel lblPhone    = UIComponent.createFieldLabel("CONTACT NO.");
        JTextField tfPhone = UIComponent.createTextField("e.g. 0123456789");

        JLabel lblPass     = UIComponent.createFieldLabel("PASSWORD");
        JPasswordField tfPass = UIComponent.createPasswordField();

        JLabel lblConfirm  = UIComponent.createFieldLabel("CONFIRM PASSWORD");
        JPasswordField tfConfirm = UIComponent.createPasswordField();

        // Status dropdown (kept from File 1)
        JLabel lblStatus = UIComponent.createFieldLabel("STATUS");
        JComboBox<String> cbStatus = UIComponent.createComboBox(new String[]{"Pending", "Approved"});
        cbStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cbStatus.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Error label
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

        // Buttons
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
            if (validationErr != null) {
                errLabel.setText(validationErr);
                return;
            }

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
        // Fetch current password from file upfront
        String existingPassword = customerDAO.getPasswordById(customerId);
        if (existingPassword == null) existingPassword = "";
        final String[] passwordHolder = { existingPassword }; // mutable ref for lambda

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

        // Title + subtitle
        JLabel dlgTitle = UIComponent.createPageTitle("Edit Customer");
        dlgTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        root.add(dlgTitle);

        JLabel dlgSubtitle = UIComponent.createCaptionLabel("Customer ID: " + customerId + " (cannot be changed)");
        dlgSubtitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        root.add(dlgSubtitle);

        // ── Basic fields ──────────────────────────────────────────────────────
        JLabel lblName    = UIComponent.createFieldLabel("USERNAME");
        JTextField tfName = UIComponent.createTextField("Full name");
        tfName.setText(currentUsername);

        JLabel lblEmail    = UIComponent.createFieldLabel("EMAIL");
        JTextField tfEmail = UIComponent.createTextField("Email address");
        tfEmail.setText(currentEmail);

        JLabel lblPhone    = UIComponent.createFieldLabel("CONTACT NO.");
        JTextField tfPhone = UIComponent.createTextField("e.g. 0123456789");
        tfPhone.setText(currentContact);

        // ── Password row: text field (editable) + show/hide toggle ────────────
        JLabel lblPass = UIComponent.createFieldLabel("PASSWORD");

        // Use a JTextField so we can show the real value; swap echo char to hide
        JPasswordField tfPass = UIComponent.createPasswordField();
        tfPass.setText(existingPassword);  // pre-fill with current (hashed) value
        tfPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tfPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Show/hide toggle button (eye icon via unicode)
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
        // Start hidden
        tfPass.setEchoChar('•');
        btnToggle.addActionListener(e -> {
            showing[0] = !showing[0];
            tfPass.setEchoChar(showing[0] ? (char) 0 : '•');
            btnToggle.setText(showing[0] ? "🙈" : "👁");
        });

        // Password row panel
        JPanel passRow = new JPanel(new BorderLayout(6, 0));
        passRow.setOpaque(false);
        passRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        passRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        passRow.add(tfPass, BorderLayout.CENTER);
        passRow.add(btnToggle, BorderLayout.EAST);

        // ── Status dropdown ───────────────────────────────────────────────────
        JLabel lblStatus = UIComponent.createFieldLabel("STATUS");
        JComboBox<String> cbStatus = UIComponent.createComboBox(new String[]{"Pending", "Approved"});
        cbStatus.setSelectedItem(currentStatus);
        cbStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cbStatus.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Error label
        JLabel errLabel = new JLabel(" ");
        errLabel.setFont(AppTheme.FONT_SMALL);
        errLabel.setForeground(AppTheme.DANGER);
        errLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Add all fields
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

        // Buttons
        JButton btnSave   = UIComponent.createPrimaryButton("Save Changes");
        JButton btnCancel = UIComponent.createOutlineButton("Cancel");

        btnSave.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSave.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnCancel.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnCancel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            String username    = tfName.getText().trim();
            String email       = tfEmail.getText().trim();
            String contact     = tfPhone.getText().trim();
            String password    = new String(tfPass.getPassword()).trim();
            String status      = (String) cbStatus.getSelectedItem();

            if (username.isEmpty()) { errLabel.setText("Username cannot be empty."); return; }
            if (email.isEmpty())    { errLabel.setText("Email cannot be empty."); return; }
            if (contact.isEmpty())  { errLabel.setText("Contact number cannot be empty."); return; }
            if (password.isEmpty()) { errLabel.setText("Password cannot be empty."); return; }

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