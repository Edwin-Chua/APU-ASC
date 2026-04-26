package views.manager;

import models.entities.User;
import views.MainFrame;
import views.shared.AppTheme;
import views.shared.BaseDashboard;
import views.shared.UIComponent;

import javax.swing.*;
import java.awt.*;

public class ManagerDashboard extends BaseDashboard {

    private static final String PAGE_EMPLOYEES = "employees";
    private static final String PAGE_SERVICES  = "services";
    private static final String PAGE_FEEDBACKS = "feedbacks";
    private static final String PAGE_REPORTS   = "reports";
    private static final String PAGE_PROFILE   = "profile";

    // private final CardLayout innerCardLayout = new CardLayout();
    private JPanel contentPanel;

    private JButton btnEmployees;
    private JButton btnServices;
    private JButton btnFeedbacks;
    private JButton btnReports;
    private JButton btnProfile;

    // Sub-pages
    private final ManageEmployeesPage employeesPage;
    private final ServicePricesPage   servicesPage;
    private final ViewFeedbacksPage   feedbacksPage;
    private final MonthlyReportsPage  reportsPage;
    private final ManagerProfilePage  profilePage;

    public ManagerDashboard(MainFrame frame) {
        super(frame);
        employeesPage = new ManageEmployeesPage(frame);
        servicesPage  = new ServicePricesPage(frame);
        feedbacksPage = new ViewFeedbacksPage(frame);
        reportsPage   = new MonthlyReportsPage(frame);
        profilePage   = new ManagerProfilePage(frame);

        contentPanel.add(employeesPage, PAGE_EMPLOYEES);
        contentPanel.add(servicesPage,  PAGE_SERVICES);
        contentPanel.add(feedbacksPage, PAGE_FEEDBACKS);
        contentPanel.add(reportsPage,   PAGE_REPORTS);
        contentPanel.add(profilePage,   PAGE_PROFILE);

    }

    @Override
    protected JPanel createSidebar() {
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setOpaque(false);

        btnEmployees = UIComponent.createNavButton("Manage Employees");
        btnServices  = UIComponent.createNavButton("Service Prices");
        btnFeedbacks = UIComponent.createNavButton("View Feedbacks");
        btnReports   = UIComponent.createNavButton("Monthly Reports");
        btnProfile   = UIComponent.createNavButton("Profile");

        btnEmployees.addActionListener(e -> navigate(btnEmployees, PAGE_EMPLOYEES));
        btnServices.addActionListener(e  -> navigate(btnServices,  PAGE_SERVICES));
        btnFeedbacks.addActionListener(e -> navigate(btnFeedbacks, PAGE_FEEDBACKS));
        btnReports.addActionListener(e   -> navigate(btnReports,   PAGE_REPORTS));
        btnProfile.addActionListener(e   -> navigate(btnProfile,   PAGE_PROFILE));

        navPanel.add(btnEmployees);
        navPanel.add(btnServices);
        navPanel.add(btnFeedbacks);
        navPanel.add(btnReports);
        navPanel.add(btnProfile);

        return buildSidebarShell("Manager", navPanel);
    }

    @Override
    protected JPanel createContentPanel() {
        contentPanel = new JPanel(new CardLayout());   // ← new CardLayout() inline
        contentPanel.setBackground(AppTheme.BG_CONTENT);
        return contentPanel;
    }

    @Override
    public void refreshUser() {
        User user = frame.getLoggedInUser();
        if (profilePage != null && user != null) {
            profilePage.loadUser(user);
        }
        navigate(btnEmployees, PAGE_EMPLOYEES);

        employeesPage.loadData();
    }

    private void navigate(JButton btn, String page) {
        setActiveNav(btn);
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, page);
    }
}