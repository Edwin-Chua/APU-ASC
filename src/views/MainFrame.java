package views;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import models.entities.User;
import views.customer.CustomerPortal;
import views.customer.CustomerRegisterPage;
import views.manager.ManagerDashboard;
import views.shared.AppTheme;
import views.shared.BaseDashboard;

public class MainFrame extends JFrame {

    public static final String PAGE_LOGIN        = "login";
    public static final String PAGE_MANAGER      = "manager";
    public static final String PAGE_COUNTERSTAFF = "counterstaff";
    public static final String PAGE_TECHNICIAN   = "technician";
    public static final String PAGE_REGISTER     = "register";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);
    private final Map<String, JPanel> pages = new HashMap<>();
    private User loggedInUser;

    public MainFrame() {
        setTitle("APU Automotive Service Centre");
        setSize(1200, 800);
        setMinimumSize(new Dimension(1000, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(AppTheme.BG_PAGE);
        mainPanel.setBackground(AppTheme.BG_PAGE);

        addPage(PAGE_REGISTER,     new CustomerRegisterPage(this));
        addPage(PAGE_LOGIN,        new LoginPage(this));
        addPage(PAGE_MANAGER,      new ManagerDashboard(this));
        addPage(PAGE_COUNTERSTAFF, new CounterStaffDashboard(this));
        addPage(PAGE_TECHNICIAN,   new TechnicianDashboard(this));

        add(mainPanel, BorderLayout.CENTER);
        showPage(PAGE_LOGIN);
    }

    public void showCustomerPage(String customerID) {
        new CustomerPortal(customerID);
    }

    private JPanel placeholder(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(AppTheme.BG_PAGE);
        JLabel lbl = new JLabel(title, SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 22));
        lbl.setForeground(AppTheme.TEXT_SECONDARY);
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    private void addPage(String key, JPanel page) {
        pages.put(key, page);
        mainPanel.add(page, key);
    }

    public void showPage(String page) {
        cardLayout.show(mainPanel, page);
        JPanel target = pages.get(page);
        if (target instanceof BaseDashboard) {
            ((BaseDashboard) target).refreshUser();
        }
    }

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }
}