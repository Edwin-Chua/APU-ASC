package views.shared;

import views.MainFrame;
import javax.swing.*;
import java.awt.*;

public abstract class BaseDashboard extends JPanel {

    protected final MainFrame frame;
    private JButton activeNavButton = null;

    public BaseDashboard(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_PAGE);
        add(createSidebar(), BorderLayout.WEST);
        add(createContentPanel(), BorderLayout.CENTER);
    }

    // ─── Subclasses must implement these ─────────────────────────────────────

    // Return the sidebar JPanel for this role (nav items differ per role)
    protected abstract JPanel createSidebar();

    // Return the CENTER content panel (inner CardLayout with sub-pages)
    protected abstract JPanel createContentPanel();

    // Called by MainFrame after showPage() — push logged-in user data into labels
    public abstract void refreshUser();

    // ─── Shared sidebar builder ───────────────────────────────────────────────
    // Subclasses can call this to get a pre-built sidebar shell,
    // then add their own nav buttons into the navPanel.

    protected JPanel buildSidebarShell(String roleLabel, JPanel navPanel) {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(AppTheme.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(AppTheme.SIDEBAR_WIDTH, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, AppTheme.SIDEBAR_DIVIDER));

        // Logo / app name block
        JPanel logoBlock = new JPanel();
        logoBlock.setLayout(new BoxLayout(logoBlock, BoxLayout.Y_AXIS));
        logoBlock.setBackground(AppTheme.SIDEBAR_BG);
        logoBlock.setMaximumSize(new Dimension(AppTheme.SIDEBAR_WIDTH, 80));
        logoBlock.setMinimumSize(new Dimension(AppTheme.SIDEBAR_WIDTH, 80));
        logoBlock.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.SIDEBAR_DIVIDER),
            new javax.swing.border.EmptyBorder(18, 18, 14, 0)
        ));

        JLabel appName = new JLabel("APU-ASC");
        appName.setFont(AppTheme.FONT_HEADING_3);
        appName.setForeground(AppTheme.PRIMARY);
        appName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel roleLbl = new JLabel(roleLabel);
        roleLbl.setFont(AppTheme.FONT_CAPTION);
        roleLbl.setForeground(AppTheme.TEXT_SECONDARY);
        roleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        logoBlock.add(appName);
        logoBlock.add(Box.createRigidArea(new Dimension(0, 4)));
        logoBlock.add(roleLbl);

        // Section label
        JLabel menuLabel = new JLabel("MENU");
        menuLabel.setFont(AppTheme.FONT_SIDEBAR_SECTION);
        menuLabel.setForeground(AppTheme.TEXT_MUTED);
        menuLabel.setMaximumSize(new Dimension(AppTheme.SIDEBAR_WIDTH, 24));
        menuLabel.setBorder(new javax.swing.border.EmptyBorder(0, 18, 6, 0));

        sidebar.add(logoBlock);
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
        sidebar.add(menuLabel);
        sidebar.add(navPanel);
        sidebar.add(Box.createVerticalGlue());

        // Divider + logout
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(AppTheme.SIDEBAR_WIDTH, 1));
        sep.setForeground(AppTheme.SIDEBAR_DIVIDER);
        sidebar.add(sep);
        sidebar.add(Box.createRigidArea(new Dimension(0, 6)));

        JButton logoutBtn = UIComponent.createNavButton("Logout");
        logoutBtn.setForeground(AppTheme.DANGER);
        logoutBtn.addActionListener(e -> handleLogout());
        sidebar.add(logoutBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));

        return sidebar;
    }

    // ─── Sidebar nav active state management ─────────────────────────────────

    protected void setActiveNav(JButton selected) {
        UIComponent.setNavActive(selected, activeNavButton);
        activeNavButton = selected;
    }

    // ─── Logout handler ───────────────────────────────────────────────────────

    private void handleLogout() {
        int choice = JOptionPane.showConfirmDialog(
            frame,
            "Are you sure you want to logout?",
            "Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) {
            frame.setLoggedInUser(null);
            frame.showPage("login");
        }
    }
}