package views.technician;

import java.awt.CardLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import models.entities.User;
import views.MainFrame;
import views.shared.AppTheme;
import views.shared.BaseDashboard;
import views.shared.UIComponent;

public class TechnicianDashboard extends BaseDashboard {

    private static final String PAGE_PROFILE = "profile";
    private static final String PAGE_VIEW_APPOINTMENT = "viewAppointment";
    private static final String PAGE_SERVICE_TASK = "serviceTask";

    private JPanel contentPanel;

    private JButton btnProfile;
    private JButton btnViewAppointment;
    private JButton btnServiceTask;

    private final TechnicianProfilePage profilePage;

    public TechnicianDashboard(MainFrame frame) {
        super(frame);

        profilePage = new TechnicianProfilePage(frame);

        contentPanel.add(profilePage, PAGE_PROFILE);

        // Later when you create these pages, uncomment/add them:
        // contentPanel.add(new TechnicianViewAppointment(frame), PAGE_VIEW_APPOINTMENT);
        // contentPanel.add(new TechnicianServiceTask(frame), PAGE_SERVICE_TASK);
    }

    @Override
    protected JPanel createSidebar() {
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setOpaque(false);

        btnProfile = UIComponent.createNavButton("Profile");
        btnViewAppointment = UIComponent.createNavButton("View Appointment");
        btnServiceTask = UIComponent.createNavButton("Service Task");

        btnProfile.addActionListener(e -> navigate(btnProfile, PAGE_PROFILE));

        // Temporary: these still go profile until pages exist
        btnViewAppointment.addActionListener(e -> navigate(btnProfile, PAGE_PROFILE));
        btnServiceTask.addActionListener(e -> navigate(btnProfile, PAGE_PROFILE));

        navPanel.add(btnProfile);
        navPanel.add(btnViewAppointment);
        navPanel.add(btnServiceTask);

        return buildSidebarShell("Technician", navPanel);
    }

    @Override
    protected JPanel createContentPanel() {
        contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(AppTheme.BG_CONTENT);
        return contentPanel;
    }

    @Override
    public void refreshUser() {
        User user = frame.getLoggedInUser();

        if (profilePage != null && user != null) {
            profilePage.loadUser(user);
        }

        navigate(btnProfile, PAGE_PROFILE);
    }

    private void navigate(JButton btn, String page) {
        setActiveNav(btn);
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, page);
    }
}