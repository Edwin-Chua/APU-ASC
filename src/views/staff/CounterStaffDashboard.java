package views.staff;

import java.awt.CardLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import models.entities.User;
import views.MainFrame;
import views.shared.AppTheme;
import views.shared.BaseDashboard;
import views.shared.UIComponent;

public class CounterStaffDashboard extends BaseDashboard {

    private static final String PAGE_REGISTER_CUSTOMER  = "registerCustomer";
    private static final String PAGE_VIEW_APPOINTMENT   = "viewAppointment";
    private static final String PAGE_CREATE_APPOINTMENT = "createAppointment";
    private static final String PAGE_BILLING            = "billing";
    private static final String PAGE_PROFILE            = "profile";

    private JPanel contentPanel;

    private JButton btnRegisterCustomer;
    private JButton btnViewAppointment;
    private JButton btnCreateAppointment;
    private JButton btnBilling;
    private JButton btnProfile;

    // Sub-pages
    private final CounterStaffRegisterCustomer  registerCustomerPage;
    private final CounterStaffViewAppointment   viewAppointmentPage;
    private final CounterStaffCreateAppointment createAppointmentPage;
    //private final CounterStaffBilling           billingPage;
    private final CounterStaffProfile           profilePage;

    public CounterStaffDashboard(MainFrame frame) {
        super(frame);
        registerCustomerPage  = new CounterStaffRegisterCustomer(frame);
        viewAppointmentPage   = new CounterStaffViewAppointment(frame);
        createAppointmentPage = new CounterStaffCreateAppointment(frame);
        //billingPage           = new CounterStaffBilling(frame);
        profilePage           = new CounterStaffProfile(frame);

        contentPanel.add(registerCustomerPage,  PAGE_REGISTER_CUSTOMER);
        contentPanel.add(viewAppointmentPage,   PAGE_VIEW_APPOINTMENT);
        contentPanel.add(createAppointmentPage, PAGE_CREATE_APPOINTMENT);
        //contentPanel.add(billingPage,           PAGE_BILLING);
        contentPanel.add(profilePage,           PAGE_PROFILE);
    }

    @Override
    protected JPanel createSidebar() {
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setOpaque(false);

        btnRegisterCustomer  = UIComponent.createNavButton("Register Customer");
        btnViewAppointment   = UIComponent.createNavButton("View Appointment");
        btnCreateAppointment = UIComponent.createNavButton("Create Appointment");
        btnBilling           = UIComponent.createNavButton("Billing");
        btnProfile           = UIComponent.createNavButton("Profile");

        btnRegisterCustomer.addActionListener(e  -> navigate(btnRegisterCustomer,  PAGE_REGISTER_CUSTOMER));
        btnViewAppointment.addActionListener(e   -> navigate(btnViewAppointment,   PAGE_VIEW_APPOINTMENT));
        btnCreateAppointment.addActionListener(e -> navigate(btnCreateAppointment, PAGE_CREATE_APPOINTMENT));
        btnBilling.addActionListener(e           -> navigate(btnBilling,           PAGE_BILLING));
        btnProfile.addActionListener(e           -> navigate(btnProfile,           PAGE_PROFILE));

        navPanel.add(btnRegisterCustomer);
        navPanel.add(btnViewAppointment);
        navPanel.add(btnCreateAppointment);
        navPanel.add(btnBilling);
        navPanel.add(btnProfile);

        return buildSidebarShell("Counter Staff", navPanel);
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
        navigate(btnRegisterCustomer, PAGE_REGISTER_CUSTOMER);
    }

    private void navigate(JButton btn, String page) {
        setActiveNav(btn);
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, page);
    }
}