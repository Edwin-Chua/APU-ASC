package application;

import javax.swing.SwingUtilities;
import views.MainFrame;

public class MainApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame f = new MainFrame();
            f.setVisible(true);
        });
    }
}