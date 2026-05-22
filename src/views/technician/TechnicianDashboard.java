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

    private static final String PAGE_PROFILE          = "profile";
    private static final String PAGE_VIEW_APPOINTMENT = "viewAppointment";
    private static final String PAGE_SERVICE_TASK     = "serviceTask";

    private JPanel contentPanel;

    private JButton btnProfile;
    private JButton btnViewAppointment;
    private JButton btnServiceTask;

    private final TechnicianProfilePage profilePage;
    private final TechnicianViewAppointmentPage viewAppointmentPage;
    private final TechnicianServiceTaskPage serviceTaskPage;
;
    public TechnicianDashboard(MainFrame frame) {
        super(frame);

        profilePage         = new TechnicianProfilePage(frame);
        viewAppointmentPage = new TechnicianViewAppointmentPage(frame);
        serviceTaskPage = new TechnicianServiceTaskPage(frame);
        contentPanel.add(profilePage,         PAGE_PROFILE);
        contentPanel.add(viewAppointmentPage, PAGE_VIEW_APPOINTMENT);
        contentPanel.add(serviceTaskPage, PAGE_SERVICE_TASK);
        // Uncomment when ready:
        // contentPanel.add(new TechnicianServiceTask(frame), PAGE_SERVICE_TASK);
    }

    @Override
    protected JPanel createSidebar() {
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setOpaque(false);

        btnProfile          = UIComponent.createNavButton("Profile");
        btnViewAppointment  = UIComponent.createNavButton("View Appointment");
        btnServiceTask      = UIComponent.createNavButton("Service Task");



        btnViewAppointment.addActionListener(e ->
            navigate(btnViewAppointment, PAGE_VIEW_APPOINTMENT));

        btnServiceTask.addActionListener(e ->
        	navigate(btnServiceTask, PAGE_SERVICE_TASK)); // swap to PAGE_SERVICE_TASK when ready
        
        btnProfile.addActionListener(e ->
        navigate(btnProfile, PAGE_PROFILE));

        navPanel.add(btnViewAppointment);
        navPanel.add(btnServiceTask);
        navPanel.add(btnProfile);

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

        if (user != null) {
            profilePage.loadUser(user);
            viewAppointmentPage.loadUser(user);
            serviceTaskPage.loadUser(user);
        }
    }

    private void navigate(JButton btn, String page) {
        setActiveNav(btn);
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, page);
    }
}