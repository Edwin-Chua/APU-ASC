package application;

import java.awt.Cursor;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.SwingUtilities;
import views.MainFrame;
import views.shared.AppTheme;

public class MainApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame f = new MainFrame();
            f.setVisible(true);
        });
    }
}

